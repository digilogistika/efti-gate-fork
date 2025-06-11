import logging
import os
import urllib3

logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

HARMONY_USERNAME = os.environ.get("HARMONY_USERNAME", "harmony")
HARMONY_PASSWORD = os.environ.get("HARMONY_PASSWORD", "Secret")

HARMONY_GATE_SERVICE_NAME = os.environ.get("HARMONY_GATE_SERVICE_NAME", "harmony")
HARMONY_SERVICE_PORT = os.environ.get("HARMONY_SERVICE_PORT", "8443")

HARMONY_GATE_URL = f"http://{HARMONY_GATE_SERVICE_NAME}:{HARMONY_SERVICE_PORT}"

PLUGIN_USER_PASSWORD = os.environ.get("PLUGIN_USER_PASSWORD", "changeit")
KEYSTORE_PASSWORD = os.environ.get("KEYSTORE_PASSWORD", "changeit")
TRUSTSTORE_PASSWORD = os.environ.get("TRUSTSTORE_PASSWORD", "changeit")
TLS_KEYSTORE_PASSWORD = os.environ.get("TLS_KEYSTORE_PASSWORD", "changeit")
TLS_TRUSTSTORE_PASSWORD = os.environ.get("TLS_TRUSTSTORE_PASSWORD", "changeit")

HARMONY_GATE_PARTY_NAME = os.environ.get(
    "HARMONY_GATE_PARTY_NAME", HARMONY_GATE_SERVICE_NAME
)

INTERNAL_TRUSTSTORE_CERT_FILENAME = "truststore_cert.pem"
INTERNAL_TLS_CERT_FILENAME = "tls_cert.pem"
COMBINED_TRUSTSTORE_FILENAME = "combined_truststore.p12"
COMBINED_TLS_TRUSTSTORE_FILENAME = "combined_tls_truststore.p12"
CURRENT_PMODE_FILENAME = "current_pmode.xml"
UPDATED_PMODE_FILENAME = "updated_pmode.xml"

MASTER_PMODE_FILE_PATH = "/app/master_pmode.xml"

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

logger.info(f"Harmony Gate URL (internal): {HARMONY_GATE_URL}")
