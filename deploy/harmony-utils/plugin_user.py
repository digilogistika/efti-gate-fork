from config import PLUGIN_USER_PASSWORD, logger
from session import get_session, DEFAULT_VERIFY


def set_plugin_user(harmony_host: str, party_name: str):
    """Creates or updates the BASIC auth plugin user for a given party."""
    session = get_session(harmony_host)
    url = f"{harmony_host}/rest/plugin/users"
    username = "service_account"
    logger.info(f"Ensuring plugin user '{username}' exists on {harmony_host}")

    payload = [
        {
            "status": "NEW",
            "userName": username,
            "active": True,
            "suspended": False,
            "authenticationType": "BASIC",
            "authRoles": "ROLE_ADMIN",
            "password": PLUGIN_USER_PASSWORD,
        }
    ]

    try:
        response = session.put(url, verify=DEFAULT_VERIFY, json=payload, timeout=20)
        response.raise_for_status()
        logger.info(
            f"""Plugin user '{username}' created or updated successfully on {
                harmony_host
            }."""
        )

    except Exception as e:
        logger.error(
            f"""Failed to set plugin user '{username}' on {harmony_host}: {e}"""
        )
        if hasattr(e, "response") and e.response is not None:
            logger.error(
                f"""Response status: {e.response.status_code}, body: {
                    e.response.text
                }"""
            )
        raise
