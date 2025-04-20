import logging
import socket
import ssl

from cryptography import x509
from cryptography.hazmat.backends import default_backend

from config import TLS_TRUSTSTORE_PASSWORD
from session import get_session

logger = logging.getLogger(__name__)


def upload_tls_truststore(harmony_host, file_name):
    session = get_session(harmony_host)
    url = harmony_host + "/rest/tlstruststore"

    with open(file_name, 'rb') as file:
        files = {
            'file': ("tls.p12", file, 'application/x-pkcs12')
        }

        data = {
            'password': TLS_TRUSTSTORE_PASSWORD
        }

        response = session.post(url, files=files, data=data, verify=False)
        response.raise_for_status()


def extract_tls_certificate(url):
    try:
        # Parse the URL to get hostname and port
        if url.startswith("https://"):
            url = url[8:]  # Remove 'https://'
        elif url.startswith("http://"):
            logger.error("Cannot extract TLS certificate from HTTP URL (no TLS): %s", url)
            return None

        # Handle URL with path
        if "/" in url:
            url = url.split("/")[0]

        # Handle port specification
        if ":" in url:
            hostname, port = url.split(":")
            port = int(port)
        else:
            hostname = url
            port = 443  # Default HTTPS port

        logger.info(f"Extracting TLS certificate from {hostname}:{port}")

        # Create an SSL context
        context = ssl.create_default_context()
        context.check_hostname = False
        context.verify_mode = ssl.CERT_NONE  # Don't validate the certificate

        # Connect and get certificate
        with socket.create_connection((hostname, port)) as sock:
            with context.wrap_socket(sock, server_hostname=hostname) as ssock:
                der_cert = ssock.getpeercert(binary_form=True)
                if not der_cert:
                    logger.error("No certificate returned from server")
                    return None

                # Parse the certificate
                cert = x509.load_der_x509_certificate(der_cert, default_backend())
                return cert

    except Exception as e:
        logger.error(f"Failed to extract TLS certificate from {url}: {e}")
        return None


if __name__ == "__main__":
    cert = extract_tls_certificate("https://harmony.borduria.efti.pikker.dev:8443")
