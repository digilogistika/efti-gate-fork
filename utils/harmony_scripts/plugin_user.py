from config import PLUGIN_USER_PASSWORD
from session import get_session


def set_plugin_user(harony_host, party_name):
    session = get_session(harony_host)

    url = harony_host + "/rest/plugin/users"
    payload = [
        {
            "status": "NEW",
            "userName": f"{party_name}_service_account",
            "active": True,
            "suspended": False,
            "authenticationType": "BASIC",
            "authRoles": "ROLE_ADMIN",
            "password": PLUGIN_USER_PASSWORD
        }
    ]

    response = session.put(url, verify=False, json=payload)
    response.raise_for_status()
