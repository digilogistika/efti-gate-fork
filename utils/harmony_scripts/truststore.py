from config import TRUSTSTORE_PASSWORD
from session import get_session


def upload_truststore(harmony_host, file_name):
    session = get_session(harmony_host)
    url = harmony_host + "/rest/truststore/save"

    with open(file_name, 'rb') as file:
        files = {
            'file': ("truststore.p12", file, 'application/x-pkcs12')
        }

        data = {
            'password': TRUSTSTORE_PASSWORD
        }

        response = session.post(url, files=files, data=data, verify=False)
        response.raise_for_status()
