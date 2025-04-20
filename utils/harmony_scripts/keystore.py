import logging

from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.serialization import pkcs12

from config import KEYSTORE_PASSWORD
from session import get_session

# Configure logger
logger = logging.getLogger(__name__)


def download_and_extract_keystore_cert(harmony_host):
    """
    Download a keystore from a Harmony host and extract the certificate.
    """
    # Download the keystore
    session = get_session(harmony_host)
    url = f"{harmony_host}/rest/keystore/download"

    try:
        response = session.get(url, verify=False)
        response.raise_for_status()
        logger.info(f"Keystore downloaded successfully from {harmony_host}")
        p12_data = response.content
    except Exception as e:
        logger.error(f"Failed to download keystore from {harmony_host}: {e}")
        return None

    # Extract the certificate
    try:
        _, certificate, _ = pkcs12.load_key_and_certificates(
            p12_data,
            KEYSTORE_PASSWORD.encode(),
            backend=default_backend()
        )

        if certificate:
            return certificate
        else:
            logger.error("No certificate found in keystore")
            return None
    except Exception as e:
        logger.error(f"Failed to extract certificate from keystore: {e}")
        return None
