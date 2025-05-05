from config import TRUSTSTORE_PASSWORD, logger
from session import get_session, DEFAULT_VERIFY
import os
import subprocess
import tempfile
import re


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


def extract_certificates_from_truststore(p12_data, output_dir):
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
            TRUSTSTORE_PASSWORD,
            "-storetype",
            "PKCS12",
            "-v",
        ]

        output = subprocess.check_output(
            cmd, stderr=subprocess.STDOUT, text=True)

        entries = re.split(r"Alias name: ", output)[1:]

        for entry in entries:
            alias = entry.split("\n")[0].strip()

            cert_path = os.path.join(
                output_dir, f"{alias}_truststore_cert.pem")
            export_cmd = [
                "keytool",
                "-exportcert",
                "-keystore",
                p12_path,
                "-storepass",
                TRUSTSTORE_PASSWORD,
                "-alias",
                alias,
                "-rfc",
                "-file",
                cert_path,
            ]

            subprocess.check_call(export_cmd, stderr=subprocess.STDOUT)

            extracted_certs[alias] = cert_path
            logger.debug(f"Extracted certificate with alias: {alias}")

        os.unlink(p12_path)

        return extracted_certs
    except Exception as e:
        logger.error(f"Failed to extract certificates from truststore: {e}")
        return {}


def download_and_extract_truststore_certs(harmony_host: str, output_dir: str):
    try:
        temp_file_path = os.path.join(output_dir, "ts.p12")

        p12_data = download_truststore(harmony_host, temp_file_path)
        if not p12_data:
            logger.warning(
                f"Failed to download truststore from {harmony_host}")
            return {}

        return extract_certificates_from_truststore(p12_data, output_dir)
    except Exception as e:
        logger.error(f"Error in download_and_extract_truststore_certs: {e}")
        return {}
