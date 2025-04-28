from config import TRUSTSTORE_PASSWORD, logger
from session import get_session, DEFAULT_VERIFY


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
