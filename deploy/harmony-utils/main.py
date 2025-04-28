import argparse
import os
import shutil
import time
import requests
from requests import RequestException
from truststore import download_and_extract_truststore_certs
from tlsstore import download_and_extract_tls_truststore_certs
from pmode import get_existing_party_endpoints

from config import (
    logger,
    HARMONY_GATE_URL,
    HARMONY_PLATFORM_URL,
    HARMONY_GATE_PARTY_NAME,
    HARMONY_PLATFORM_PARTY_NAME,
    TRUSTSTORE_PASSWORD,
    TLS_TRUSTSTORE_PASSWORD,
)
from plugin_user import set_plugin_user
from keystore import download_keystore_extract_cert_pem
from tlsstore import extract_tls_certificate_to_pem, upload_tls_truststore
from truststore import upload_truststore
from util import create_combined_p12
from pmode import update_pmode_with_parties, upload_pmode
from session import DEFAULT_VERIFY


def check_service_reachability(url: str, timeout: int = 5) -> bool:
    try:
        response = requests.get(url, verify=DEFAULT_VERIFY, timeout=timeout)
        if response.status_code == 200:
            logger.info(f"Service at {url} is reachable.")
            return True
        else:
            logger.warning(
                f"""Service at {url} returned status {response.status_code}."""
            )
            return False
    except requests.exceptions.ConnectionError:
        logger.warning(f"Service at {url} is not reachable (connection error).")
        return False
    except requests.exceptions.Timeout:
        logger.warning(f"Service at {url} is not reachable (timeout).")
        return False
    except Exception as e:
        logger.error(f"Error checking reachability for {url}: {e}")
        return False


def do_initial_setup():
    logger.info("--- Starting Initial Setup ---")
    success = True

    logger.info("Waiting for gate-s harmony to be ready...")
    while True:
        try:
            response = requests.get(HARMONY_GATE_URL, verify=DEFAULT_VERIFY)
            if response.status_code == 200:
                logger.info("Gate-s harmony is ready.")
                break
        except RequestException as e:
            logger.error(f"Error connecting to gate-s harmony: {e}")
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

    platform_active = check_service_reachability(HARMONY_PLATFORM_URL)

    if platform_active:
        logger.info("Harmony Platform appears active. Proceeding with Platform setup.")
        try:
            logger.info(
                f"""Setting plugin user for Platform ({
                    HARMONY_PLATFORM_PARTY_NAME
                }) at {HARMONY_PLATFORM_URL}"""
            )
            set_plugin_user(HARMONY_PLATFORM_URL, HARMONY_PLATFORM_PARTY_NAME)
        except Exception as e:
            logger.error(f"Failed to set plugin user for Platform: {e}")
            success = False

        try:
            logger.info("Setting up mutual connection between Gate and Platform...")
            connect_two_parties(
                party1_url=HARMONY_GATE_URL,
                party1_name=HARMONY_GATE_PARTY_NAME,
                party2_url=HARMONY_PLATFORM_URL,
                party2_name=HARMONY_PLATFORM_PARTY_NAME,
                is_internal_setup=True,
            )
            logger.info("Gate <-> Platform connection setup completed.")
        except Exception as e:
            logger.error(
                f"""Failed to setup mutual connection between
                Gate and Platform: {e}"""
            )
            success = False
    else:
        logger.info(
            "Harmony Platform not detected or not reachable. Skipping Platform-specific setup."
        )

    logger.info(
        f"""--- Initial Setup Finished {
            "Successfully" if success else "with Errors"
        } ---"""
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
            f"""--- Export completed successfully.
            Certificates saved in {output_dir} ---"""
        )
    else:
        logger.error("--- Export failed. Check logs for details. ---")
        if not success_ts and os.path.exists(truststore_cert_path):
            os.remove(truststore_cert_path)
        if not success_tls and os.path.exists(tls_cert_path):
            os.remove(tls_cert_path)
        exit(1)


def do_connect(
    peer_name: str,
    peer_url: str,
    peer_truststore_cert_path: str,
    peer_tls_cert_path: str,
):
    logger.info(
        f"""--- Starting Connection Setup:
        Gate ({HARMONY_GATE_PARTY_NAME}) <-> Peer ({peer_name}) ---"""
    )
    logger.info(f"Local Gate URL: {HARMONY_GATE_URL}")
    logger.info(f"Peer Name: {peer_name}")
    logger.info(f"Peer URL: {peer_url}")
    logger.info(f"Peer Truststore Cert Path: {peer_truststore_cert_path}")
    logger.info(f"Peer TLS Cert Path: {peer_tls_cert_path}")

    try:
        connect_two_parties(
            party1_url=HARMONY_GATE_URL,
            party1_name=HARMONY_GATE_PARTY_NAME,
            party2_url=peer_url,
            party2_name=peer_name,
            party2_truststore_cert_path=peer_truststore_cert_path,
            party2_tls_cert_path=peer_tls_cert_path,
            is_internal_setup=False,
        )
        logger.info(
            f"""--- Connection setup Gate ({HARMONY_GATE_PARTY_NAME}) ->
                Peer ({peer_name}) completed successfully ---"""
        )
    except Exception as e:
        logger.error(
            f"""--- Connection setup Gate ({HARMONY_GATE_PARTY_NAME}) ->
                Peer ({peer_name}) failed: {e} ---"""
        )
        exit(1)


def connect_two_parties(
    party1_url: str,
    party1_name: str,
    party2_url: str,
    party2_name: str,
    party2_truststore_cert_path: str | None = None,
    party2_tls_cert_path: str | None = None,
    is_internal_setup: bool = False,
):
    logger.info(
        f"""Establishing connection between '{party1_name}' ({party1_url}) and '{
            party2_name
        }' ({party2_url})"""
    )
    logger.info(f"Internal setup mode: {is_internal_setup}")

    p1_name_sanitized = party1_name.replace("-", "_")
    p2_name_sanitized = party2_name.replace("-", "_")

    temp_dir = f"""/tmp/harmony_connect_{p1_name_sanitized}_{p2_name_sanitized}_{
        int(time.time())
    }"""
    os.makedirs(temp_dir, exist_ok=True)
    logger.debug(f"Using temporary directory: {temp_dir}")

    try:
        local_p1_ts_cert_path = os.path.join(temp_dir, f"{p1_name_sanitized}_ts.pem")
        local_p1_tls_cert_path = os.path.join(temp_dir, f"{p1_name_sanitized}_tls.pem")
        local_p2_ts_cert_path = os.path.join(temp_dir, f"{p2_name_sanitized}_ts.pem")
        local_p2_tls_cert_path = os.path.join(temp_dir, f"{p2_name_sanitized}_tls.pem")

        # ==== STEP 1: Get certificates of party1 (Gate) ====
        logger.info(f"Fetching Truststore cert for {party1_name}...")
        if not download_keystore_extract_cert_pem(party1_url, local_p1_ts_cert_path):
            raise ConnectionError(f"Failed to get Truststore cert for {party1_name}")
        logger.info(f"Fetching TLS cert for {party1_name}...")
        if not extract_tls_certificate_to_pem(party1_url, local_p1_tls_cert_path):
            raise ConnectionError(f"Failed to get TLS cert for {party1_name}")

        # ==== STEP 2: Get certificates of party2 (peer) ====
        if is_internal_setup:
            logger.info(f"Fetching Truststore cert for {party2_name}...")
            if not download_keystore_extract_cert_pem(
                party2_url, local_p2_ts_cert_path
            ):
                raise ConnectionError(
                    f"Failed to get Truststore cert for {party2_name}"
                )
            logger.info(f"Fetching TLS cert for {party2_name}...")
            if not extract_tls_certificate_to_pem(party2_url, local_p2_tls_cert_path):
                raise ConnectionError(f"Failed to get TLS cert for {party2_name}")
        else:
            logger.info(f"Using provided certificate paths for peer {party2_name}")
            if not party2_truststore_cert_path or not os.path.exists(
                party2_truststore_cert_path
            ):
                raise FileNotFoundError(
                    f"""Peer truststore certificate not found or path not provided: {
                        party2_truststore_cert_path
                    }"""
                )
            if not party2_tls_cert_path or not os.path.exists(party2_tls_cert_path):
                raise FileNotFoundError(
                    f"""Peer TLS certificate not found or path not provided: {
                        party2_tls_cert_path
                    }"""
                )
            shutil.copyfile(party2_truststore_cert_path, local_p2_ts_cert_path)
            shutil.copyfile(party2_tls_cert_path, local_p2_tls_cert_path)
            logger.debug(f"Copied provided peer certs to {temp_dir}")

        # ==== STEP 3: Extract existing certificates from current truststores ====
        existing_truststore_certs = {}
        existing_tls_certs = {}

        if not is_internal_setup:
            logger.info(
                "Downloading and extracting existing certificates from Gate truststores..."
            )

            try:
                existing_truststore_certs = download_and_extract_truststore_certs(
                    party1_url, temp_dir, "existing_ts"
                )
                logger.info(
                    f"""Extracted {
                        len(existing_truststore_certs)
                    } certificates from existing truststore"""
                )
            except Exception as e:
                logger.warning(f"Error getting existing truststore certificates: {e}")
            try:
                existing_tls_certs = download_and_extract_tls_truststore_certs(
                    party1_url, temp_dir, "existing_tls"
                )
                logger.info(
                    f"""Extracted {
                        len(existing_tls_certs)
                    } certificates from existing TLS truststore"""
                )
            except Exception as e:
                logger.warning(
                    f"Error getting existing TLS truststore certificates: {e}"
                )

        # ==== STEP 4: Combine all certificates (existing + new ones) ====
        all_truststore_certs = {
            party1_name: local_p1_ts_cert_path,
            party2_name: local_p2_ts_cert_path,
            **existing_truststore_certs,
        }
        all_tls_certs = {
            party1_name: local_p1_tls_cert_path,
            party2_name: local_p2_tls_cert_path,
            **existing_tls_certs,
        }

        combined_truststore_path = os.path.join(temp_dir, "combined_truststore.p12")
        combined_tls_truststore_path = os.path.join(
            temp_dir, "combined_tls_truststore.p12"
        )

        logger.info(
            f"""Creating combined Truststore for parties: {
                list(all_truststore_certs.keys())
            }..."""
        )
        if not create_combined_p12(
            all_truststore_certs, combined_truststore_path, TRUSTSTORE_PASSWORD
        ):
            raise RuntimeError("Failed to create combined Truststore P12")

        logger.info(
            f"""Creating combined TLS Truststore for parties: {
                list(all_tls_certs.keys())
            }..."""
        )
        if not create_combined_p12(
            all_tls_certs, combined_tls_truststore_path, TLS_TRUSTSTORE_PASSWORD
        ):
            raise RuntimeError("Failed to create combined TLS Truststore P12")

        parties_to_upload_to = []
        if is_internal_setup:
            parties_to_upload_to = [
                (party1_name, party1_url),
                (party2_name, party2_url),
            ]
        else:
            parties_to_upload_to = [(party1_name, party1_url)]

        logger.info(f"Uploading combined truststores to relevant parties...")
        for target_name, target_url in parties_to_upload_to:
            logger.info(f"""Uploading Truststore to {target_name} ({target_url})...""")
            upload_truststore(target_url, combined_truststore_path)
            logger.info(
                f"""Uploading TLS Truststore to {target_name} ({target_url})..."""
            )
            upload_tls_truststore(target_url, combined_tls_truststore_path)

        # ==== STEP 5: Update PModes ====
        party1_endpoint = f"{party1_url}/services/msh?domain={party1_name}"
        party2_endpoint = f"{party2_url}/services/msh?domain={party2_name}"
        parties_for_pmode = {party1_name: party1_endpoint, party2_name: party2_endpoint}

        if not is_internal_setup:
            logger.info("Retrieving current PMode configuration...")

            existing_parties = get_existing_party_endpoints(party1_url, temp_dir)

            if existing_parties:
                logger.info(f"Found {len(existing_parties)} existing parties in PMode")
                for name, endpoint in existing_parties.items():
                    if name != party1_name and name != party2_name:
                        parties_for_pmode[name] = endpoint

        p1_updated_pmode_path = os.path.join(temp_dir, f"{p1_name_sanitized}_pmode.xml")
        logger.info(
            f"""Generating updated PMode for {party1_name} with all parties..."""
        )
        if not update_pmode_with_parties(
            parties_for_pmode, party1_name, p1_updated_pmode_path
        ):
            raise RuntimeError(
                f"""Failed to generate PMode XML for {party1_name} with all parties"""
            )
        logger.info(f"Uploading generated PMode to {party1_name}...")
        upload_pmode(
            party1_url,
            p1_updated_pmode_path,
            f"Configured parties: {', '.join(parties_for_pmode.keys())}",
        )

        if is_internal_setup:
            p2_updated_pmode_path = os.path.join(
                temp_dir, f"{p2_name_sanitized}_pmode.xml"
            )
            logger.info(
                f"""Generating updated PMode for {party2_name} with all parties..."""
            )
            if not update_pmode_with_parties(
                parties_for_pmode, party2_name, p2_updated_pmode_path
            ):
                raise RuntimeError(
                    f"""Failed to generate PMode XML for {
                        party2_name
                    } with all parties"""
                )
            logger.info(f"Uploading generated PMode to {party2_name}...")
            upload_pmode(
                party2_url,
                p2_updated_pmode_path,
                f"Configured parties: {', '.join(parties_for_pmode.keys())}",
            )

        logger.info("Connection process completed.")

    except Exception as e:
        logger.error(f"Error during connection setup: {e}")
        raise
    finally:
        try:
            shutil.rmtree(temp_dir)
            logger.debug(f"Cleaned up temporary directory: {temp_dir}")
        except Exception as e:
            logger.warning(
                f"""Could not clean up temporary directory {temp_dir}: {e}"""
            )


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Harmony AP Setup Utility Script")
    subparsers = parser.add_subparsers(
        dest="command", required=True, help="Available commands"
    )

    parser_init = subparsers.add_parser(
        "initial-setup",
        help="Run initial setup (plugin users, internal gate<->platform connection)",
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
