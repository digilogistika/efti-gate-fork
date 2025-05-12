from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.serialization import pkcs12

from config import KEYSTORE_PASSWORD, INTERNAL_TRUSTSTORE_CERT_FILENAME, logger
from session import get_session, DEFAULT_VERIFY


def download_keystore_extract_cert_pem(
    harmony_host: str, output_filename: str = INTERNAL_TRUSTSTORE_CERT_FILENAME
) -> bool:
    session = get_session(harmony_host)
    url = f"{harmony_host}/rest/keystore/download"
    logger.info(f"Downloading keystore from {harmony_host}")

    try:
        response = session.get(url, verify=DEFAULT_VERIFY, timeout=30)
        response.raise_for_status()
        logger.info(f"Keystore downloaded successfully from {harmony_host}")
        p12_data = response.content
    except Exception as e:
        logger.error(f"Failed to download keystore from {harmony_host}: {e}")
        return False

    try:
        key, certificate, additional_certificates = pkcs12.load_key_and_certificates(
            p12_data, KEYSTORE_PASSWORD.encode(), backend=default_backend()
        )
        if certificate:
            cert_to_save = certificate
            logger.info("Extracted certificate (and key) from keystore.")
        elif additional_certificates:
            cert_to_save = additional_certificates[0]
            logger.info(
                "Extracted first additional certificate from keystore (no private key found)."
            )
        else:
            logger.warning(
                "No primary or additional certificate found in keystore P12."
            )
            certs_only = pkcs12.load_pkcs12(
                p12_data, KEYSTORE_PASSWORD.encode(), backend=default_backend()
            )
            if certs_only.cert and certs_only.cert.certificate:
                cert_to_save = certs_only.cert.certificate
                logger.info("Successfully loaded keystore as certificate-only store.")
            else:
                logger.error(
                    "No certificate found in keystore even after trying certs-only load."
                )
                return False

    except ValueError as e:
        logger.warning(
            f"Failed to load key and cert (maybe no key?): {e}. Trying cert-only load."
        )
        try:
            certs_only = pkcs12.load_pkcs12(
                p12_data, KEYSTORE_PASSWORD.encode(), backend=default_backend()
            )
            if certs_only.cert and certs_only.cert.certificate:
                cert_to_save = certs_only.cert.certificate
                logger.info("Successfully loaded keystore as certificate-only store.")
            else:
                logger.error(
                    "No certificate found in keystore even after trying certs-only load."
                )
                return False
        except Exception as e_inner:
            logger.error(
                f"Failed to extract certificate from keystore (both methods): {e_inner}"
            )
            return False
    except Exception as e:
        logger.error(f"Failed to extract certificate from keystore: {e}")
        return False

    try:
        with open(output_filename, "wb") as f:
            f.write(cert_to_save.public_bytes(serialization.Encoding.PEM))
        logger.info(
            f"""Successfully extracted and saved keystore certificate to {
                output_filename
            }"""
        )
        return True
    except Exception as e:
        logger.error(f"Failed to save certificate PEM to {output_filename}: {e}")
        return False
