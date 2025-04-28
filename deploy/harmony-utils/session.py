import requests
import logging
from config import HARMONY_PASSWORD, HARMONY_USERNAME

logger = logging.getLogger(__name__)

DEFAULT_VERIFY = False


def get_session(harmony_host):
    payload = {"password": HARMONY_PASSWORD, "username": HARMONY_USERNAME}
    auth_url = f"{harmony_host}/rest/security/authentication"
    session = requests.Session()

    logger.debug(f"Attempting to get XSRF token from {auth_url}")
    session.get(auth_url, verify=DEFAULT_VERIFY, timeout=10)

    xsrf_token = session.cookies.get("XSRF-TOKEN")
    if not xsrf_token:
        logger.error(f"Could not get XSRF token from {harmony_host}")
        raise ConnectionError(f"Failed to get XSRF token from {harmony_host}")

    session.headers.update({"X-XSRF-TOKEN": xsrf_token})

    try:
        logger.debug(f"Attempting to authenticate to {auth_url}")
        response = session.post(
            auth_url, json=payload, verify=DEFAULT_VERIFY, timeout=10
        )
        response.raise_for_status()
        logger.info(f"Successfully authenticated to {harmony_host}")
        return session
    except requests.exceptions.RequestException as e:
        logger.error(f"Authentication failed for {harmony_host}: {e}")
        if e.response is not None:
            logger.error(
                f"""Response status: {e.response.status_code}, body: {
                    e.response.text
                }"""
            )
        raise ConnectionError(f"""Authentication failed for {harmony_host}""") from e
