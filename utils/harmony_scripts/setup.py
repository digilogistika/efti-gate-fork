import logging
import time

import urllib3
from requests import RequestException

from config import ACCESS_POINTS
from keystore import download_and_extract_keystore_cert
from plugin_user import set_plugin_user
from pmode import update_pmode_config, upload_pmode
from tlsstore import upload_tls_truststore, extract_tls_certificate
from truststore import upload_truststore
from util import create_combined_p12

logger = logging.getLogger(__name__)

import requests


def setup():
    logger.info("Waiting for Harmony Access Points to be ready...")
    for name, url in ACCESS_POINTS.items():
        logger.info(f"Waiting for {name} to be ready...")
        while True:
            try:
                response = requests.get(url, verify=False)
                if response.status_code == 200:
                    logger.info(f"{name} is ready.")
                    break
            except RequestException as e:
                logger.error(f"Error connecting to {name}: {e}")
            logger.info("Retrying in 5 seconds...")
            time.sleep(5)

    # -------------------------------------------------------

    logger.info("Setting up plugin users for Harmony Access Points...")
    for name, url in ACCESS_POINTS.items():
        logger.info(f"Setting up plugin user for {name}...")
        try:
            set_plugin_user(url, name)
        except Exception as e:
            logger.error(f"Error setting up plugin user for {name}: {e}")
            continue

    # KEYSTORE ------------------------------------------------

    logger.info("Gathering keystore certificates for each Harmony Access Point...")
    keystore_certificates = {}
    for name, url in ACCESS_POINTS.items():
        logger.info(f"Gathering certificate for {name}...")
        try:
            cert = download_and_extract_keystore_cert(url)
            if not cert: continue
            keystore_certificates[name] = cert
        except Exception as e:
            logger.error(f"Error gathering certificate for {name}: {e}")
            continue

    # -------------------------------------------------------

    logger.info("Constructing keystore p12 file used by all Harmony Access Points...")
    truststore_filename = "truststore.p12"
    try:
        create_combined_p12(keystore_certificates, truststore_filename)
        logger.info("Combined keystore created successfully.")
    except Exception as e:
        logger.error(f"Error creating combined keystore: {e}. Exiting...")

    # -------------------------------------------------------

    logger.info("Uploading truststore p12 file to Harmony Access Points...")
    for name, url in ACCESS_POINTS.items():
        logger.info(f"Uploading truststore to {name}...")
        try:
            upload_truststore(url, truststore_filename)
        except Exception as e:
            logger.error(f"Error uploading keystore to {name}: {e}")
            continue

    # TLS ---------------------------------------------------

    logger.info("Gathering TLS certificates for each Harmony Access Point...")
    tls_certs = {}
    for name, url in ACCESS_POINTS.items():
        logger.info(f"Gathering TLS certificate for {name}...")
        try:
            cert = extract_tls_certificate(url)
            if not cert: continue
            tls_certs[name] = cert
        except Exception as e:
            logger.error(f"Error gathering TLS truststore for {name}: {e}")
            continue

    # -------------------------------------------------------

    logger.info("Constructing TLS truststore p12 file used by all Harmony Access Points...")
    tls_truststore_filename = "tls.p12"
    try:
        create_combined_p12(tls_certs, tls_truststore_filename)
        logger.info("Combined TLS truststore created successfully.")
    except Exception as e:
        logger.error(f"Error creating combined TLS truststore: {e}. Exiting...")

    # -------------------------------------------------------

    logger.info("Uploading TLS truststore p12 file to Harmony Access Points...")
    for name, url in ACCESS_POINTS.items():
        logger.info(f"Uploading TLS truststore to {name}...")
        try:
            upload_tls_truststore(url, tls_truststore_filename)
        except Exception as e:
            logger.error(f"Error uploading TLS truststore to {name}: {e}")
            continue

    # --------------------------------------------------------

    logger.info("Constructing pmode file used by all Harmony Access Points...")
    pmode_filename = "pmode.xml"
    try:
        update_pmode_config(ACCESS_POINTS, output_file_path=pmode_filename)
        logger.info("Pmode file created successfully.")
    except Exception as e:
        logger.error(f"Error creating pmode file: {e}. Exiting...")

    # --------------------------------------------------------

    logger.info("Uploading pmode file to Harmony Access Points...")
    for name, url in ACCESS_POINTS.items():
        logger.info(f"Uploading pmode file to {name}...")
        try:
            upload_pmode(url, name, pmode_filename)
        except Exception as e:
            logger.error(f"Error uploading pmode file to {name}: {e}")
            continue

    logger.info("Setup completed successfully.")


if __name__ == "__main__":
    logging.basicConfig()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
    logging.root.setLevel(logging.INFO)
    setup()
