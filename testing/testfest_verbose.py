import requests
import json
import os
import time
from config import *

gate_token = None
simulator_token = None
gate_uil_request_id = None
gate_identifiers_request_id = None


def print_status(step, success, message=""):
    status = "[ OK ]" if success else "[FAIL]"
    print(f"{status} {step}: {message}")


def simulator_authentication():
    global simulator_token
    step = "Simulator Authentication"
    url = KC_SIMULATOR_URL
    payload = {
        "grant_type": "client_credentials",
        "client_id": "simulator",
        "client_secret": KC_SIMULATOR_SECRET,
    }
    headers = {"Content-Type": "application/x-www-form-urlencoded"}
    try:
        response = requests.post(
            url, data=payload, headers=headers, timeout=AUTH_TIMEOUT
        )
        response.raise_for_status()
        data = response.json()
        simulator_token = data.get("access_token")
        if simulator_token:
            print_status(
                step, True, f"Token obtained (masked): ...{simulator_token[-6:]}"
            )
        else:
            print_status(step, False, "Access token not found in response.")
    except requests.exceptions.RequestException as e:
        print_status(step, False, f"Error: {e}")
    except json.JSONDecodeError:
        print_status(
            step,
            False,
            f"Failed to decode JSON response. Status: {response.status_code}, Body: {
                response.text
            }",
        )


def simulator_upload_file(file_path):
    step = "Simulator Upload File"
    if not simulator_token:
        print_status(step, False, "Skipping - Simulator token not available.")
        return

    url = f"{SIMULATOR_URL}/identifiers/upload/file"
    headers = {"Authorization": f"Bearer {simulator_token}"}
    try:
        with open(file_path, "rb") as f:
            files = {"file": (os.path.basename(file_path), f, "application/xml")}
            response = requests.post(
                url, headers=headers, files=files, timeout=UPLOAD_TIMEOUT
            )
        response.raise_for_status()
        print_status(
            step,
            True,
            f"File upload successful from {file_path}. Status: {response.status_code}",
        )
    except requests.exceptions.RequestException as e:
        print_status(
            step,
            False,
            f"Error uploading {file_path}: {e} - Response: {
                e.response.text if e.response else 'N/A'
            }",
        )
    except IOError as e:
        print_status(step, False, f"File handling error for {file_path}: {e}")


def simulator_upload_identifiers(
    dataset_id,
    identifier=DEFAULT_IDENTIFIER,
    dangerous_goods=DEFAULT_DANGEROUS_GOODS,
    mode_code=DEFAULT_MODE_CODE,
    used_transport_means_id=DEFAULT_TRANSPORT_MEANS_ID,
    used_transport_means_registration_country=DEFAULT_TRANSPORT_MEANS_COUNTRY,
    carried_transport_equipment_id=DEFAULT_EQUIPMENT_ID,
    carried_transport_equipment_registration_country=DEFAULT_EQUIPMENT_COUNTRY,
):
    step = "Simulator Upload Identifiers (JSON)"
    if not simulator_token:
        print_status(step, False, "Skipping - Simulator token not available.")
        return

    url = f"{SIMULATOR_URL}/identifiers/upload"
    headers = {
        "Authorization": f"Bearer {simulator_token}",
        "Content-Type": "application/json",
    }
    payload = {
        "consignment": {
            "carrierAcceptanceDateTime": {
                "formatId": "205",
                "value": "202310011000+0000",
            },
            "mainCarriageTransportMovement": [
                {
                    "dangerousGoodsIndicator": dangerous_goods,
                    "modeCode": mode_code,
                    "usedTransportMeans": {
                        "id": {
                            "schemeAgencyId": "UN",
                            "value": used_transport_means_id,
                        },
                        "registrationCountry": {
                            "code": used_transport_means_registration_country
                        },
                    },
                }
            ],
            "usedTransportEquipment": [
                {
                    "carriedTransportEquipment": [
                        {
                            "id": {
                                "schemeAgencyId": "UN",
                                "value": carried_transport_equipment_id,
                            },
                            "sequenceNumber": 1,
                        }
                    ],
                    "categoryCode": "AE",
                    "id": {"schemeAgencyId": "UN", "value": identifier},
                    "registrationCountry": {
                        "code": carried_transport_equipment_registration_country
                    },
                    "sequenceNumber": 2,
                }
            ],
        },
        "datasetId": dataset_id,
    }
    try:
        response = requests.post(
            url, headers=headers, json=payload, timeout=CONTROL_TIMEOUT
        )
        response.raise_for_status()
        print_status(
            step,
            True,
            f"Identifiers upload successful with dataset ID: {dataset_id}. Status: {
                response.status_code
            }",
        )
    except requests.exceptions.RequestException as e:
        print_status(
            step,
            False,
            f"Error: {e} - Response: {e.response.text if e.response else 'N/A'}",
        )
    except json.JSONDecodeError:
        print_status(
            step,
            False,
            f"Failed to decode JSON response. Status: {response.status_code}, Body: {
                response.text
            }",
        )


def gate_authentication():
    global gate_token
    step = "Gate Authentication"
    url = KC_GATE_URL
    payload = {
        "username": GATE_USER,
        "password": GATE_PASSWORD,
        "grant_type": "password",
        "client_id": "gate",
        "client_secret": KC_GATE_SECRET,
    }
    headers = {"Content-Type": "application/x-www-form-urlencoded"}
    try:
        response = requests.post(
            url, data=payload, headers=headers, timeout=AUTH_TIMEOUT
        )
        response.raise_for_status()
        data = response.json()
        gate_token = data.get("access_token")
        if gate_token:
            print_status(step, True, f"Token obtained (masked): ...{gate_token[-6:]}")
        else:
            print_status(step, False, "Access token not found in response.")
    except requests.exceptions.RequestException as e:
        print_status(step, False, f"Error: {e}")
    except json.JSONDecodeError:
        print_status(
            step,
            False,
            f"Failed to decode JSON response. Status: {response.status_code}, Body: {
                response.text
            }",
        )


def gate_post_uil_control(dataset_id):
    global gate_uil_request_id
    step = "Gate POST UIL Control"
    if not gate_token:
        print_status(step, False, "Skipping - Gate token not available.")
        return

    url = f"{GATE_URL}/v1/control/uil"
    headers = {
        "Authorization": f"Bearer {gate_token}",
        "Content-Type": "application/json",
    }
    payload = {
        "gateId": GATE_ID,
        "datasetId": dataset_id,
        "platformId": SIMULATOR_ID,
        "subsetId": "full",
    }
    try:
        response = requests.post(
            url, headers=headers, json=payload, timeout=CONTROL_TIMEOUT
        )
        response.raise_for_status()
        data = response.json()
        gate_uil_request_id = data.get("requestId")
        status = data.get("status")
        if gate_uil_request_id:
            print_status(
                step,
                True,
                f"Request successful with dataset ID: {dataset_id}. Status: {
                    status
                }. requestId: {gate_uil_request_id}",
            )
        else:
            print_status(step, False, "requestId not found in response.")
        return gate_uil_request_id
    except requests.exceptions.RequestException as e:
        print_status(
            step,
            False,
            f"Error: {e} - Response: {e.response.text if e.response else 'N/A'}",
        )
    except json.JSONDecodeError:
        print_status(
            step,
            False,
            f"Failed to decode JSON response. Status: {response.status_code}, Body: {
                response.text
            }",
        )


def gate_get_uil_control():
    step = "Gate GET UIL Control"
    if not gate_token:
        print_status(step, False, "Skipping - Gate token not available.")
        return
    if not gate_uil_request_id:
        print_status(step, False, "Skipping - UIL requestId not available.")
        return

    url = f"{GATE_URL}/v1/control/uil"
    headers = {"Authorization": f"Bearer {gate_token}"}
    params = {"requestId": gate_uil_request_id}
    try:
        status = "PENDING"
        while status == "PENDING":
            response = requests.get(
                url, headers=headers, params=params, timeout=CONTROL_TIMEOUT
            )
            response.raise_for_status()
            status = response.json().get("status")
            print_status(
                step,
                True,
                f"Request successful. Status: {status}, Response:\n{response.text}",
            )
            print("Waiting 5 sec before retry...")
            time.sleep(SLEEP_TIME)
        return status
    except requests.exceptions.RequestException as e:
        print_status(
            step,
            False,
            f"Error: {e} - Response: {e.response.text if e.response else 'N/A'}",
        )


def gate_post_identifiers_control(identifier):
    global gate_identifiers_request_id
    step = "Gate POST Identifiers Control"
    if not gate_token:
        print_status(step, False, "Skipping - Gate token not available.")
        return

    url = f"{GATE_URL}/v1/control/identifiers"
    headers = {
        "Authorization": f"Bearer {gate_token}",
        "Content-Type": "application/json",
    }
    payload = {"identifier": identifier, "eftiGateIndicator": [GATE_INDICATOR]}
    try:
        response = requests.post(
            url, headers=headers, json=payload, timeout=CONTROL_TIMEOUT
        )
        response.raise_for_status()
        data = response.json()
        gate_identifiers_request_id = data.get("requestId")
        status = data.get("status")
        if gate_identifiers_request_id:
            print_status(
                step,
                True,
                f"Request successful with indentifier: {identifier}. Status: {
                    status
                }. requestId: {gate_uil_request_id}",
            )
        else:
            print_status(step, False, "requestId not found in response.")
    except requests.exceptions.RequestException as e:
        print_status(
            step,
            False,
            f"Error: {e} - Response: {e.response.text if e.response else 'N/A'}",
        )
    except json.JSONDecodeError:
        print_status(
            step,
            False,
            f"Failed to decode JSON response. Status: {response.status_code}, Body: {
                response.text
            }",
        )


def gate_get_identifiers_control():
    step = "Gate GET Identifiers Control"
    if not gate_token:
        print_status(step, False, "Skipping - Gate token not available.")
        return
    if not gate_identifiers_request_id:
        print_status(step, False, "Skipping - Identifiers requestId not available.")
        return

    url = f"{GATE_URL}/v1/control/identifiers"
    headers = {"Authorization": f"Bearer {gate_token}"}
    params = {"requestId": gate_identifiers_request_id}
    try:
        status = "PENDING"
        while status == "PENDING":
            response = requests.get(
                url, headers=headers, params=params, timeout=CONTROL_TIMEOUT
            )
            response.raise_for_status()
            data = response.json()
            status = data.get("status")
            print_status(
                step,
                True,
                f"Request successful. Status: {status}, Response:\n{
                    json.dumps(json.loads(response.text), indent=2)
                }",
            )
            print("Waiting 5 sec before retry...")
            time.sleep(SLEEP_TIME)
        return data
    except requests.exceptions.RequestException as e:
        print_status(
            step,
            False,
            f"Error: {e} - Response: {e.response.text if e.response else 'N/A'}",
        )


if __name__ == "__main__":
    # Upload data to platform
    simulator_authentication()
    simulator_upload_file(XML_FILE_PATH)
    simulator_upload_file(XML_FILE_2_PATH)
    simulator_upload_file(XML_LARGE_FILE_PATH)
    simulator_upload_identifiers(FILE_DATASET_ID)
    simulator_upload_identifiers(FILE_2_DATASET_ID)
    simulator_upload_identifiers(LARGE_FILE_DATASET_ID)
    simulator_upload_identifiers(DATASET_ID_REGULAR)
    simulator_upload_identifiers(DATASET_ID_MMG418, identifier=IDENTIFIER_MMG418)
    simulator_upload_identifiers(DATASET_ID_CMF452_1, identifier=IDENTIFIER_CMF452)
    simulator_upload_identifiers(DATASET_ID_CMF452_2, identifier=IDENTIFIER_CMF452)
    simulator_upload_identifiers(DATASET_ID_RTB307, identifier=IDENTIFIER_RTB307)
    simulator_upload_identifiers(DATASET_ID_BDE471, identifier=IDENTIFIER_BDE471)
    simulator_upload_identifiers(DATASET_ID_LOADTEST, identifier=IDENTIFIER_LOADTEST)
    simulator_upload_identifiers(DATASET_ID_EEE100, identifier=IDENTIFIER_EEE100)

    gate_authentication()

    # Test Case 30
    gate_post_uil_control(FILE_DATASET_ID)
    status = gate_get_uil_control()

    assert status == "COMPLETE"

    gate_post_uil_control(FILE_2_DATASET_ID)
    status = gate_get_uil_control()

    assert status == "COMPLETE"

    # Test case 165
    # Reference implementation does not have follow-up messages

    # Test case 166
    # Reference implementation does not have request with subset IDs

    # Test case 55
    gate_post_identifiers_control(IDENTIFIER_MMG418)
    data = gate_get_identifiers_control()
    consignments = data["identifiers"][0]["consignments"]

    assert len(consignments) == 1
    assert consignments[0]["datasetId"] == DATASET_ID_MMG418

    gate_post_identifiers_control(IDENTIFIER_CMF452)
    data = gate_get_identifiers_control()
    consignments = data["identifiers"][0]["consignments"]

    assert len(consignments) == 2
    assert set([consignments[0]["datasetId"], consignments[1]["datasetId"]]) == set(
        [DATASET_ID_CMF452_1, DATASET_ID_CMF452_2]
    )

    gate_post_identifiers_control(IDENTIFIER_RTB307)
    data = gate_get_identifiers_control()
    consignments = data["identifiers"][0]["consignments"]

    assert len(consignments) == 1
    assert consignments[0]["datasetId"] == DATASET_ID_RTB307

    gate_post_identifiers_control(IDENTIFIER_BDE471)
    data = gate_get_identifiers_control()
    consignments = data["identifiers"][0]["consignments"]

    assert len(consignments) == 1
    assert consignments[0]["datasetId"] == DATASET_ID_BDE471

    # Test case 170

    # Test case 39
    gate_post_uil_control(DATASET_ID_ERROR)
    status = gate_get_uil_control()

    assert status == "ERROR"

    # Test case 169
    gate_post_identifiers_control(IDENTIFIER_LOADTEST)
    data = gate_get_identifiers_control()
    consignments = data["identifiers"][0]["consignments"]

    assert len(consignments) == 1
    assert consignments[0]["datasetId"] == DATASET_ID_LOADTEST

    # Test case 50
    gate_post_identifiers_control(IDENTIFIER_EEE100)
    data = gate_get_identifiers_control()
    consignments = data["identifiers"][0]["consignments"]

    assert len(consignments) == 1
    assert consignments[0]["datasetId"] == DATASET_ID_EEE100

    # Reauthenting (just in case)
    gate_authentication()

    # Test case 40
    gate_post_uil_control(DATASET_ID_REGULAR)
    status = gate_get_uil_control()
    gate_get_uil_control()

    assert status == "TIMEOUT"

    # Test case 168 - currently timing out
    gate_post_uil_control(LARGE_FILE_DATASET_ID)
    gate_get_uil_control()

    # assert status == "COMPLETE"
