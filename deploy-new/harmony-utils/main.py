import argparse
import os
import shutil
import time
import requests
from requests import RequestException

from truststore import download_and_extract_truststore_certs, upload_truststore
from tlsstore import (
    download_and_extract_tls_truststore_certs,
    upload_tls_truststore,
    extract_tls_certificate_to_pem,
)
from pmode import get_existing_party_endpoints, update_pmode_with_parties, upload_pmode
from plugin_user import set_plugin_user
from keystore import download_keystore_extract_cert_pem
from util import create_combined_p12
from session import DEFAULT_VERIFY
from config import (
    logger,
    HARMONY_GATE_URL,
    HARMONY_GATE_PARTY_NAME,
    TRUSTSTORE_PASSWORD,
    TLS_TRUSTSTORE_PASSWORD,
    MASTER_PMODE_FILE_PATH,
)


def setup_single_party(party_name, party_url):
    temp_dir = f"/tmp/harmony_setup_{party_name.replace('-', '_')}_{int(time.time())}"
    os.makedirs(temp_dir, exist_ok=True)

    try:
        local_cert_path = os.path.join(
            temp_dir, f"{party_name.replace('-', '_')}_cert.pem"
        )
        if not download_keystore_extract_cert_pem(party_url, local_cert_path):
            raise ConnectionError(
                f"Failed to extract keystore certificate for {party_name}"
            )

        truststore_path = os.path.join(temp_dir, "truststore.p12")
        if not create_combined_p12(
            {party_name: local_cert_path}, truststore_path, TRUSTSTORE_PASSWORD
        ):
            raise RuntimeError("Failed to create Truststore P12")

        upload_truststore(party_url, truststore_path)

        endpoint = f"{party_url}/services/msh?domain={party_name}"
        parties_for_pmode = {party_name: endpoint}

        pmode_path = os.path.join(temp_dir, f"{party_name.replace('-', '_')}_pmode.xml")
        if not update_pmode_with_parties(parties_for_pmode, party_name, pmode_path):
            raise RuntimeError(f"Failed to generate PMode XML for {party_name}")

        upload_pmode(party_url, pmode_path, f"Configured single party: {party_name}")

        shutil.copy2(pmode_path, MASTER_PMODE_FILE_PATH)

        logger.info("Single party setup completed successfully.")
        return True
    except Exception as e:
        logger.error(f"Failed during single party setup: {e}")
        return False
    finally:
        if os.path.exists(temp_dir):
            shutil.rmtree(temp_dir)


def do_initial_setup():
    logger.info("--- Starting Initial Setup ---")
    success = True

    logger.info("Waiting for Gate harmony to be ready...")
    while True:
        try:
            response = requests.get(HARMONY_GATE_URL, verify=DEFAULT_VERIFY)
            if response.status_code == 200:
                logger.info("Gate harmony is ready.")
                break
        except RequestException:
            pass
        logger.info("Retrying in 5 seconds...")
        time.sleep(5)

    try:
        logger.info(
            f"""Setting plugin user for Gate ({HARMONY_GATE_PARTY_NAME}) at {
                HARMONY_GATE_URL
            }"""
        )
        set_plugin_user(HARMONY_GATE_URL, HARMONY_GATE_PARTY_NAME)
    except Exception as e:
        logger.error(f"Failed to set plugin user for Gate: {e}")
        success = False

    if not setup_single_party(HARMONY_GATE_PARTY_NAME, HARMONY_GATE_URL):
        success = False

    logger.info(
        f"--- Initial Setup Finished {'Successfully' if success else 'with Errors'} ---"
    )
    if not success:
        exit(1)


def do_export(output_dir: str):
    logger.info(f"--- Starting Certificate Export for Gate ({HARMONY_GATE_URL}) ---")
    os.makedirs(output_dir, exist_ok=True)

    truststore_cert_path = os.path.join(
        output_dir, f"{HARMONY_GATE_PARTY_NAME}_truststore_cert.pem"
    )
    tls_cert_path = os.path.join(output_dir, f"{HARMONY_GATE_PARTY_NAME}_tls_cert.pem")

    logger.info("Exporting Gate Truststore certificate...")
    success_ts = download_keystore_extract_cert_pem(
        HARMONY_GATE_URL, truststore_cert_path
    )

    logger.info("Exporting Gate TLS certificate...")
    success_tls = extract_tls_certificate_to_pem(HARMONY_GATE_URL, tls_cert_path)

    if success_ts and success_tls:
        logger.info(
            f"--- Export completed successfully. Certificates saved in {output_dir} ---"
        )
    else:
        logger.error("--- Export failed. Check logs for details. ---")
        if not success_ts and os.path.exists(truststore_cert_path):
            os.remove(truststore_cert_path)
        exit(1)


def do_connect(
    peer_name: str,
    peer_url: str,
    peer_truststore_cert_path: str,
    peer_tls_cert_path: str,
):
    logger.info(
        f"""--- Starting Connection Setup: Gate ({HARMONY_GATE_PARTY_NAME}) <-> Peer ({
            peer_name
        }) ---"""
    )

    try:
        temp_dir = f"""/tmp/harmony_connect_{
            HARMONY_GATE_PARTY_NAME.replace("-", "_")
        }_{peer_name.replace("-", "_")}_{int(time.time())}"""
        os.makedirs(temp_dir, exist_ok=True)

        existing_truststore_certs = download_and_extract_truststore_certs(
            HARMONY_GATE_URL, temp_dir
        )
        existing_tls_certs = download_and_extract_tls_truststore_certs(
            HARMONY_GATE_URL, temp_dir
        )

        all_truststore_certs = {
            peer_name: peer_truststore_cert_path,
            **existing_truststore_certs,
        }
        all_tls_certs = {peer_name: peer_tls_cert_path, **existing_tls_certs}

        combined_truststore_path = os.path.join(temp_dir, "combined_truststore.p12")
        combined_tls_truststore_path = os.path.join(
            temp_dir, "combined_tls_truststore.p12"
        )

        if not create_combined_p12(
            all_truststore_certs, combined_truststore_path, TRUSTSTORE_PASSWORD
        ):
            raise RuntimeError("Failed to create combined Truststore P12")

        if not create_combined_p12(
            all_tls_certs, combined_tls_truststore_path, TLS_TRUSTSTORE_PASSWORD
        ):
            raise RuntimeError("Failed to create combined TLS Truststore P12")

        upload_truststore(HARMONY_GATE_URL, combined_truststore_path)
        upload_tls_truststore(HARMONY_GATE_URL, combined_tls_truststore_path)

        existing_parties = get_existing_party_endpoints(HARMONY_GATE_URL, temp_dir)

        all_parties = {
            **existing_parties,
            HARMONY_GATE_PARTY_NAME: f"{HARMONY_GATE_URL}/services/msh?domain={HARMONY_GATE_PARTY_NAME}",
            peer_name: f"{peer_url}/services/msh?domain={peer_name}",
        }

        updated_pmode_path = os.path.join(
            temp_dir, f"{HARMONY_GATE_PARTY_NAME.replace('-', '_')}_pmode.xml"
        )

        if not update_pmode_with_parties(
            all_parties, HARMONY_GATE_PARTY_NAME, updated_pmode_path
        ):
            raise RuntimeError(
                f"""Failed to generate PMode XML for {
                    HARMONY_GATE_PARTY_NAME
                } with all parties"""
            )

        upload_pmode(
            HARMONY_GATE_URL,
            updated_pmode_path,
            f"Configured parties: {', '.join(all_parties.keys())}",
        )

        shutil.copy2(updated_pmode_path, MASTER_PMODE_FILE_PATH)

        logger.info(
            f"""--- Connection setup Gate ({HARMONY_GATE_PARTY_NAME}) -> Peer ({
                peer_name
            }) completed successfully ---"""
        )
    except Exception as e:
        logger.error(
            f"""--- Connection setup Gate ({HARMONY_GATE_PARTY_NAME}) -> Peer ({
                peer_name
            }) failed: {e} ---"""
        )
        exit(1)
    finally:
        if "temp_dir" in locals() and os.path.exists(temp_dir):
            shutil.rmtree(temp_dir)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Harmony AP Setup Utility Script")
    subparsers = parser.add_subparsers(
        dest="command", required=True, help="Available commands"
    )

    subparsers.add_parser(
        "initial-setup",
        help="Run initial setup (plugin user and PMODE)",
    )

    parser_export = subparsers.add_parser("export", help="Export Gate certificates")
    parser_export.add_argument(
        "--output-dir",
        default="/export",
        help="Directory inside the container to save exported certificates",
    )

    parser_connect = subparsers.add_parser(
        "connect", help="Connect Gate to an external peer"
    )
    parser_connect.add_argument(
        "--peer-name", required=True, help="Unique name/identifier for the peer"
    )
    parser_connect.add_argument(
        "--peer-url", required=True, help="HTTPS URL of the peer's Harmony AP"
    )
    parser_connect.add_argument(
        "--peer-truststore-cert",
        required=True,
        help="Path inside the container to the peer's exported Truststore certificate (PEM)",
    )
    parser_connect.add_argument(
        "--peer-tls-cert",
        required=True,
        help="Path inside the container to the peer's exported TLS certificate (PEM)",
    )

    args = parser.parse_args()

    if args.command == "initial-setup":
        do_initial_setup()
    elif args.command == "export":
        do_export(args.output_dir)
    elif args.command == "connect":
        do_connect(
            args.peer_name, args.peer_url, args.peer_truststore_cert, args.peer_tls_cert
        )
    else:
        logger.error(f"Unknown command: {args.command}")
        parser.print_help()
        exit(1)
