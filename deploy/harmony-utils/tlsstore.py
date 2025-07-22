import os
import re
import shutil
import socket
import ssl
import subprocess
import tempfile
from cryptography import x509
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from urllib.parse import urlparse

from config import TLS_TRUSTSTORE_PASSWORD, logger
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


def extract_tls_certificate_to_pem(url: str, output_filename: str) -> bool:
    try:
        parsed_url = urlparse(url)
        hostname = parsed_url.hostname
        port = parsed_url.port or (443 if parsed_url.scheme == "https" else 80)

        if not hostname or parsed_url.scheme != "https":
            logger.warn(f"Invalid HTTPS URL for TLS certificate extraction: {url}")
            logger.info("Extracting certificates from the truststore instead")

            os.makedirs("tmp_tls_dir", exist_ok=True)
            extracted_tls_certificates: dict[str, str] = download_and_extract_tls_truststore_certs(url, "tmp_tls_dir")

            if len(extracted_tls_certificates) > 1:
                logger.info("certificates have been configured. Doing nothing")
                return True
            
            for alias, cert_path in extracted_tls_certificates.items():
                shutil.copy2(cert_path, output_filename)
                logger.info(f"Successfully extracted and saved TLS certificate to {output_filename}")

            return True

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
    url = f"{harmony_host}/rest/tlstruststore"
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


def extract_certificates_from_tls_truststore(p12_data, output_dir):
    extracted_certs = {}
    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix=".p12") as temp_p12:
            temp_p12.write(p12_data)
            p12_path = temp_p12.name

        cmd = [
            "keytool",
            "-list",
            "-keystore",
            p12_path,
            "-storepass",
            TLS_TRUSTSTORE_PASSWORD,
            "-storetype",
            "PKCS12",
            "-v",
        ]
        output = subprocess.check_output(
            cmd, stderr=subprocess.STDOUT, text=True)

        entries = re.split(r"Alias name: ", output)[1:]

        for i, entry in enumerate(entries):
            alias = entry.split("\n")[0].strip()

            cert_path = os.path.join(output_dir, f"{alias}_tls_cert.pem")

            export_cmd = [
                "keytool",
                "-exportcert",
                "-keystore",
                p12_path,
                "-storepass",
                TLS_TRUSTSTORE_PASSWORD,
                "-alias",
                alias,
                "-rfc",
                "-file",
                cert_path,
            ]
            subprocess.check_call(export_cmd, stderr=subprocess.STDOUT)

            extracted_certs[alias] = cert_path
            logger.debug(f"Extracted TLS certificate: {alias}")

        os.unlink(p12_path)
        return extracted_certs

    except Exception as e:
        logger.error(
            f"Failed to extract certificates from TLS truststore: {e}")
        return {}


def download_and_extract_tls_truststore_certs(harmony_host: str, output_dir: str):
    try:
        temp_file_path = os.path.join(output_dir, "tls.p12")

        p12_data = download_tls_truststore(harmony_host, temp_file_path)
        if not p12_data:
            logger.warning(
                f"Failed to download TLS truststore from {harmony_host}")
            return {}

        return extract_certificates_from_tls_truststore(p12_data, output_dir)
    except Exception as e:
        logger.error(
            f"Error in download_and_extract_tls_truststore_certs: {e}")
        return {}
