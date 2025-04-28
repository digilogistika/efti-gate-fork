import socket
import ssl
from urllib.parse import urlparse

from cryptography import x509
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization

from config import TLS_TRUSTSTORE_PASSWORD, INTERNAL_TLS_CERT_FILENAME, logger
from session import get_session, DEFAULT_VERIFY


def upload_tls_truststore(harmony_host: str, p12_file_path: str):
    session = get_session(harmony_host)
    url = f"{harmony_host}/rest/tlstruststore"
    logger.info(f"Uploading TLS truststore {p12_file_path} to {harmony_host}")

    with open(p12_file_path, "rb") as file:
        files = {"file": ("tls.p12", file, "application/x-pkcs12")}
        data = {"password": TLS_TRUSTSTORE_PASSWORD}

        try:
            response = session.post(
                url, files=files, data=data, verify=DEFAULT_VERIFY, timeout=30
            )
            response.raise_for_status()
            logger.info(f"""TLS truststore uploaded successfully to {harmony_host}""")
        except Exception as e:
            logger.error(f"""Failed to upload TLS truststore to {harmony_host}: {e}""")
            if hasattr(e, "response") and e.response is not None:
                logger.error(
                    f"""Response status: {e.response.status_code}, body: {
                        e.response.text
                    }"""
                )
            raise


def extract_tls_certificate_to_pem(
    url: str, output_filename: str = INTERNAL_TLS_CERT_FILENAME
) -> bool:
    try:
        parsed_url = urlparse(url)
        hostname = parsed_url.hostname
        port = parsed_url.port or (443 if parsed_url.scheme == "https" else 80)

        if not hostname or parsed_url.scheme != "https":
            logger.error(f"Invalid HTTPS URL for TLS certificate extraction: {url}")
            return False

        logger.info(f"Extracting TLS certificate from {hostname}:{port}")

        context = ssl.create_default_context()
        context.check_hostname = False
        context.verify_mode = ssl.CERT_NONE

        with socket.create_connection((hostname, port), timeout=10) as sock:
            with context.wrap_socket(sock, server_hostname=hostname) as ssock:
                der_cert = ssock.getpeercert(binary_form=True)
                if not der_cert:
                    logger.error(
                        f"""No TLS certificate returned from {hostname}:{port}"""
                    )
                    return False

                cert = x509.load_der_x509_certificate(der_cert, default_backend())

                with open(output_filename, "wb") as f:
                    f.write(cert.public_bytes(serialization.Encoding.PEM))
                logger.info(
                    f"""Successfully extracted and saved TLS certificate to {
                        output_filename
                    }"""
                )
                return True

    except socket.gaierror as e:
        logger.error(f"Hostname resolution failed for {url}: {e}")
        return False
    except ssl.SSLError as e:
        logger.error(f"SSL error connecting to {url}: {e}")
        return False
    except Exception as e:
        logger.error(f"Failed to extract TLS certificate from {url}: {e}")
        return False
