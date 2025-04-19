import requests

from config import HARMONY_PASSWORD, HARMONY_USERNAME


def get_session(harmony_host):
    payload = {
        "password": HARMONY_PASSWORD,
        "username": HARMONY_USERNAME
    }

    url = harmony_host + "/rest/security/authentication"

    session = requests.Session()

    response = session.get(url, verify=False)

    xsrf_token = response.cookies.get("XSRF-TOKEN")

    session.headers.update({
        "X-XSRF-TOKEN": xsrf_token,
    })

    session.post(
        url,
        json=payload,
        verify=False,
    )

    return session
