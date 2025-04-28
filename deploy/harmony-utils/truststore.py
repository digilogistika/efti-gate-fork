from config import TRUSTSTORE_PASSWORD, logger
from session import get_session, DEFAULT_VERIFY
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.serialization import pkcs12
import os


def upload_truststore(harmony_host: str, p12_file_path: str):
    session = get_session(harmony_host)
    url = f"{harmony_host}/rest/truststore/save"
    logger.info(f"Uploading truststore {p12_file_path} to {harmony_host}")

    with open(p12_file_path, "rb") as file:
        files = {"file": ("truststore.p12", file, "application/x-pkcs12")}
        data = {"password": TRUSTSTORE_PASSWORD}

        try:
            response = session.post(
                url, files=files, data=data, verify=DEFAULT_VERIFY, timeout=30
            )
            response.raise_for_status()
            logger.info(f"Truststore uploaded successfully to {harmony_host}")
        except Exception as e:
            logger.error(f"Failed to upload truststore to {harmony_host}: {e}")
            if hasattr(e, "response") and e.response is not None:
                logger.error(
                    f"""Response status: {e.response.status_code}, body: {
                        e.response.text
                    }"""
                )
            raise


def download_truststore(harmony_host: str, output_path: str):
    session = get_session(harmony_host)
    url = f"{harmony_host}/rest/truststore/download"
    logger.info(f"Downloading truststore from {harmony_host}")

    try:
        response = session.get(url, verify=DEFAULT_VERIFY, timeout=30)
        response.raise_for_status()

        with open(output_path, "wb") as f:
            f.write(response.content)

        logger.info(f"Truststore downloaded successfully from {harmony_host}")
        return response.content
    except Exception as e:
        logger.error(f"Failed to download truststore from {harmony_host}: {e}")
        if hasattr(e, "response") and e.response is not None:
            logger.error(
                f"""Response status: {e.response.status_code}, body: {
                    e.response.text
                }"""
            )
        return None


def extract_certificates_from_truststore(p12_data, output_dir, prefix="extracted_cert"):
    extracted_certs = {}

    try:
        _, _, additional_certificates = pkcs12.load_key_and_certificates(
            p12_data, TRUSTSTORE_PASSWORD.encode(), backend=default_backend()
        )

        for i, cert in enumerate(additional_certificates):
            cert_name = f"{prefix}_{i}"
            cert_path = os.path.join(output_dir, f"{cert_name}.pem")
            with open(cert_path, "wb") as f:
                f.write(cert.public_bytes(serialization.Encoding.PEM))
            extracted_certs[cert_name] = cert_path
            logger.debug(f"Extracted certificate: {cert_name}")

        return extracted_certs
    except Exception as e:
        logger.error(f"Failed to extract certificates from truststore: {e}")
        return {}


def download_and_extract_truststore_certs(
    harmony_host: str, output_dir: str, prefix="ts"
):
    try:
        temp_file_path = os.path.join(
            output_dir, f"temp_truststore_{prefix}.p12")

        p12_data = download_truststore(harmony_host, temp_file_path)
        if not p12_data:
            logger.warning(
                f"Failed to download truststore from {harmony_host}")
            return {}

        return extract_certificates_from_truststore(p12_data, output_dir, prefix)
    except Exception as e:
        logger.error(f"Error in download_and_extract_truststore_certs: {e}")
        return {}
