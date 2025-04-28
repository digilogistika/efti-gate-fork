import socket
import ssl
import os
from urllib.parse import urlparse

from cryptography import x509
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.serialization import pkcs12

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
            logger.info(f"""TLS truststore uploaded successfully to {
                        harmony_host}""")
        except Exception as e:
            logger.error(f"""Failed to upload TLS truststore to {
                         harmony_host}: {e}""")
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
            logger.error(
                f"Invalid HTTPS URL for TLS certificate extraction: {url}")
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
                        f"""No TLS certificate returned from {
                            hostname}:{port}"""
                    )
                    return False

                cert = x509.load_der_x509_certificate(
                    der_cert, default_backend())

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


def download_tls_truststore(harmony_host: str, output_path: str):
    session = get_session(harmony_host)
    url = f"{harmony_host}/rest/tlstruststore/download"
    logger.info(f"Downloading TLS truststore from {harmony_host}")

    try:
        response = session.get(url, verify=DEFAULT_VERIFY, timeout=30)
        response.raise_for_status()

        with open(output_path, "wb") as f:
            f.write(response.content)

        logger.info(f"""TLS truststore downloaded successfully from {
                    harmony_host}""")
        return response.content
    except Exception as e:
        logger.error(f"""Failed to download TLS truststore from {
                     harmony_host}: {e}""")
        if hasattr(e, "response") and e.response is not None:
            logger.error(
                f"""Response status: {e.response.status_code}, body: {
                    e.response.text
                }"""
            )
        return None


def extract_certificates_from_tls_truststore(
    p12_data, output_dir, prefix="extracted_tls_cert"
):
    extracted_certs = {}

    try:
        _, _, additional_certificates = pkcs12.load_key_and_certificates(
            p12_data, TLS_TRUSTSTORE_PASSWORD.encode(), backend=default_backend()
        )

        for i, cert in enumerate(additional_certificates):
            cert_name = f"{prefix}_{i}"
            cert_path = os.path.join(output_dir, f"{cert_name}.pem")
            with open(cert_path, "wb") as f:
                f.write(cert.public_bytes(serialization.Encoding.PEM))
            extracted_certs[cert_name] = cert_path
            logger.debug(f"Extracted TLS certificate: {cert_name}")

        return extracted_certs
    except Exception as e:
        logger.error(
            f"Failed to extract certificates from TLS truststore: {e}")
        return {}


def download_and_extract_tls_truststore_certs(
    harmony_host: str, output_dir: str, prefix="tls"
):
    try:
        temp_file_path = os.path.join(
            output_dir, f"temp_tls_truststore_{prefix}.p12")

        p12_data = download_tls_truststore(harmony_host, temp_file_path)
        if not p12_data:
            logger.warning(
                f"Failed to download TLS truststore from {harmony_host}")
            return {}

        return extract_certificates_from_tls_truststore(p12_data, output_dir, prefix)
    except Exception as e:
        logger.error(
            f"Error in download_and_extract_tls_truststore_certs: {e}")
        return {}
