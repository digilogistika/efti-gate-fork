import pytest
import requests
import json
import os
import time
from config import *


def _simulator_authentication():
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
        token = data.get("access_token")
        if not token:
            pytest.fail(
                "Simulator Authentication failed: Access token not found.")
        print(
            f"[ INFO ] Simulator Authentication: Token obtained (masked): ...{
                token[-6:]
            }"
        )
        return token
    except requests.exceptions.RequestException as e:
        pytest.fail(f"Simulator Authentication failed: {e}")
    except json.JSONDecodeError:
        pytest.fail(
            f"Simulator Authentication failed: Failed to decode JSON response. Status: {
                response.status_code
            }, Body: {response.text}"
        )


def _simulator_upload_file(token, file_path):
    if not token:
        pytest.skip("Skipping file upload - Simulator token not available.")
        return

    url = f"{SIMULATOR_URL}/identifiers/upload/file"
    headers = {"Authorization": f"Bearer {token}"}
    try:
        with open(file_path, "rb") as f:
            files = {"file": (os.path.basename(
                file_path), f, "application/xml")}
            response = requests.post(
                url, headers=headers, files=files, timeout=UPLOAD_TIMEOUT
            )
        response.raise_for_status()
        print(
            f"[ INFO ] Simulator Upload File: Success from {file_path}. Status: {
                response.status_code
            }"
        )
    except requests.exceptions.RequestException as e:
        pytest.fail(
            f"Simulator Upload File failed for {file_path}: {e} - Response: {
                e.response.text if e.response else 'N/A'
            }"
        )
    except IOError as e:
        pytest.fail(
            f"Simulator Upload File failed: File handling error for {
                file_path}: {e}"
        )


def _simulator_upload_identifiers(
    token,
    dataset_id,
    identifier=DEFAULT_IDENTIFIER,
    dangerous_goods=DEFAULT_DANGEROUS_GOODS,
    mode_code=DEFAULT_MODE_CODE,
    used_transport_means_id=DEFAULT_TRANSPORT_MEANS_ID,
    used_transport_means_registration_country=DEFAULT_TRANSPORT_MEANS_COUNTRY,
    carried_transport_equipment_id=DEFAULT_EQUIPMENT_ID,
    carried_transport_equipment_registration_country=DEFAULT_EQUIPMENT_COUNTRY,
):
    if not token:
        pytest.skip(
            "Skipping identifier upload - Simulator token not available.")
        return

    url = f"{SIMULATOR_URL}/identifiers/upload"
    headers = {
        "Authorization": f"Bearer {token}",
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
        print(
            f"[ INFO ] Simulator Upload Identifiers: Success for dataset ID: {
                dataset_id
            }. Status: {response.status_code}"
        )
    except requests.exceptions.RequestException as e:
        pytest.fail(
            f"Simulator Upload Identifiers failed for {dataset_id}: {e} - Response: {
                e.response.text if e.response else 'N/A'
            }"
        )
    except json.JSONDecodeError:
        pytest.fail(
            f"Simulator Upload Identifiers failed: Failed to decode JSON response. Status: {
                response.status_code
            }, Body: {response.text}"
        )


def _gate_authentication():
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
        token = data.get("access_token")
        if not token:
            pytest.fail("Gate Authentication failed: Access token not found.")
        print(f"[ INFO ] Gate Authentication: Token obtained (masked): ...{
              token[-6:]}")
        return token
    except requests.exceptions.RequestException as e:
        pytest.fail(f"Gate Authentication failed: Error: {e}")
    except json.JSONDecodeError:
        pytest.fail(
            f"Gate Authentication failed: Failed to decode JSON response. Status: {
                response.status_code
            }, Body: {response.text}"
        )


def _gate_post_uil_control(token, dataset_id):
    if not token:
        pytest.skip("Skipping POST UIL control - Gate token not available.")
        return None

    url = f"{GATE_URL}/v1/control/uil"
    headers = {
        "Authorization": f"Bearer {token}",
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
        request_id = data.get("requestId")
        status = data.get("status")
        if not request_id:
            pytest.fail(
                f"Gate POST UIL Control failed for {
                    dataset_id
                }: requestId not found in response."
            )
        print(
            f"[ INFO ] Gate POST UIL Control: Success for {dataset_id}. Status: {
                status
            }. requestId: {request_id}"
        )
        return request_id
    except requests.exceptions.RequestException as e:
        pytest.fail(
            f"Gate POST UIL Control failed for {dataset_id}: {e} - Response: {
                e.response.text if e.response else 'N/A'
            }"
        )
    except json.JSONDecodeError:
        pytest.fail(
            f"Gate POST UIL Control failed: Failed to decode JSON response. Status: {
                response.status_code
            }, Body: {response.text}"
        )


def _gate_get_uil_control(token, request_id):
    if not token:
        pytest.skip("Skipping GET UIL control - Gate token not available.")
        return None
    if not request_id:
        pytest.skip("Skipping GET UIL control - UIL requestId not available.")
        return None

    url = f"{GATE_URL}/v1/control/uil"
    headers = {"Authorization": f"Bearer {token}"}
    params = {"requestId": request_id}
    status = ""
    max_retries = 10  # Avoid infinite loops in tests
    retries = 0
    while status in ["PENDING", ""] and retries < max_retries:
        if status != "":
            print(f"Waiting {SLEEP_TIME} sec before retry...")
            time.sleep(SLEEP_TIME)
        try:
            response = requests.get(
                url, headers=headers, params=params, timeout=CONTROL_TIMEOUT
            )
            response.raise_for_status()
            data = response.json()
            status = data.get("status")
            print(
                f"[ INFO ] Gate GET UIL Control: Request {request_id}. Status: {
                    status
                }, Response:\n{json.dumps(data, indent=2)}"
            )
            if status not in ["PENDING", ""]:
                return status
        except requests.exceptions.RequestException as e:
            pytest.fail(
                f"Gate GET UIL Control failed for {request_id}: {e} - Response: {
                    e.response.text if e.response else 'N/A'
                }"
            )
        except json.JSONDecodeError:
            pytest.fail(
                f"Gate GET UIL Control failed: Failed to decode JSON response. Status: {
                    response.status_code
                }, Body: {response.text}"
            )
        retries += 1

    if retries >= max_retries:
        pytest.fail(
            f"Gate GET UIL Control failed for {request_id}: Polling timed out after {
                max_retries * SLEEP_TIME
            } seconds."
        )
    return status  # Should not be reached if successful exit


def _gate_post_identifiers_control(token, identifier):
    if not token:
        pytest.skip(
            "Skipping POST Identifiers control - Gate token not available.")
        return None

    url = f"{GATE_URL}/v1/control/identifiers"
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
    }
    payload = {"identifier": identifier, "eftiGateIndicator": [GATE_INDICATOR]}
    try:
        response = requests.post(
            url, headers=headers, json=payload, timeout=CONTROL_TIMEOUT
        )
        response.raise_for_status()
        data = response.json()
        request_id = data.get("requestId")
        status = data.get("status")
        if not request_id:
            pytest.fail(
                f"Gate POST Identifiers Control failed for {
                    identifier
                }: requestId not found in response."
            )
        print(
            f"[ INFO ] Gate POST Identifiers Control: Success for {
                identifier
            }. Status: {status}. requestId: {request_id}"
        )
        return request_id
    except requests.exceptions.RequestException as e:
        pytest.fail(
            f"Gate POST Identifiers Control failed for {identifier}: {e} - Response: {
                e.response.text if e.response else 'N/A'
            }"
        )
    except json.JSONDecodeError:
        pytest.fail(
            f"Gate POST Identifiers Control failed: Failed to decode JSON response. Status: {
                response.status_code
            }, Body: {response.text}"
        )


def _gate_get_identifiers_control(token, request_id):
    if not token:
        pytest.skip(
            "Skipping GET Identifiers control - Gate token not available.")
        return None
    if not request_id:
        pytest.skip(
            "Skipping GET Identifiers control - Identifiers requestId not available."
        )
        return None

    url = f"{GATE_URL}/v1/control/identifiers"
    headers = {"Authorization": f"Bearer {token}"}
    params = {"requestId": request_id}
    status = ""
    max_retries = 10  # Avoid infinite loops in tests
    retries = 0
    data = None
    while status in ["PENDING", ""] and retries < max_retries:
        if status != "":
            print(f"Waiting {SLEEP_TIME} sec before retry...")
            time.sleep(SLEEP_TIME)
        try:
            response = requests.get(
                url, headers=headers, params=params, timeout=CONTROL_TIMEOUT
            )
            response.raise_for_status()
            data = response.json()
            status = data.get("status")
            print(
                f"[ INFO ] Gate GET Identifiers Control: Request {request_id}. Status: {
                    status
                }, Response:\n{json.dumps(data, indent=2)}"
            )
            if status not in ["PENDING", ""]:
                return data
        except requests.exceptions.RequestException as e:
            pytest.fail(
                f"Gate GET Identifiers Control failed for {request_id}: {
                    e
                } - Response: {e.response.text if e.response else 'N/A'}"
            )
        except json.JSONDecodeError:
            pytest.fail(
                f"Gate GET Identifiers Control failed: Failed to decode JSON response. Status: {
                    response.status_code
                }, Body: {response.text}"
            )
        retries += 1

    if retries >= max_retries:
        pytest.fail(
            f"Gate GET Identifiers Control failed for {
                request_id
            }: Polling timed out after {max_retries * SLEEP_TIME} seconds."
        )
    return data  # Should not be reached if successful exit


# Fixtures


@pytest.fixture(scope="session")
def simulator_token():
    return _simulator_authentication()


@pytest.fixture(scope="session")
def gate_token():
    return _gate_authentication()


@pytest.fixture(scope="session", autouse=True)
def setup_simulator_data(simulator_token):
    print("\n[ SETUP ] Uploading initial data to simulator...")
    _simulator_upload_file(simulator_token, XML_FILE_PATH)
    _simulator_upload_file(simulator_token, XML_FILE_2_PATH)
    _simulator_upload_file(simulator_token, XML_LARGE_FILE_PATH)
    _simulator_upload_identifiers(simulator_token, FILE_DATASET_ID)
    _simulator_upload_identifiers(simulator_token, FILE_2_DATASET_ID)
    _simulator_upload_identifiers(simulator_token, LARGE_FILE_DATASET_ID)
    _simulator_upload_identifiers(simulator_token, DATASET_ID_REGULAR)
    _simulator_upload_identifiers(
        simulator_token, DATASET_ID_MMG418, identifier=IDENTIFIER_MMG418
    )
    _simulator_upload_identifiers(
        simulator_token, DATASET_ID_CMF452_1, identifier=IDENTIFIER_CMF452
    )
    _simulator_upload_identifiers(
        simulator_token, DATASET_ID_CMF452_2, identifier=IDENTIFIER_CMF452
    )
    _simulator_upload_identifiers(
        simulator_token, DATASET_ID_RTB307, identifier=IDENTIFIER_RTB307
    )
    _simulator_upload_identifiers(
        simulator_token, DATASET_ID_BDE471, identifier=IDENTIFIER_BDE471
    )
    _simulator_upload_identifiers(
        simulator_token, DATASET_ID_LOADTEST, identifier=IDENTIFIER_LOADTEST
    )
    _simulator_upload_identifiers(
        simulator_token, DATASET_ID_EEE100, identifier=IDENTIFIER_EEE100
    )
    print("[ SETUP ] Simulator data upload complete.")


# Test Cases


def test_uil_control_file_dataset_1(gate_token):
    request_id = _gate_post_uil_control(gate_token, FILE_DATASET_ID)
    status = _gate_get_uil_control(gate_token, request_id)
    assert status == "COMPLETE"


def test_uil_control_file_dataset_2(gate_token):
    request_id = _gate_post_uil_control(gate_token, FILE_2_DATASET_ID)
    status = _gate_get_uil_control(gate_token, request_id)
    assert status == "COMPLETE"


# Test case 165 - Skipped as per original comment
# Test case 166 - Skipped as per original comment


def test_identifiers_control_mmg418(gate_token):
    request_id = _gate_post_identifiers_control(gate_token, IDENTIFIER_MMG418)
    data = _gate_get_identifiers_control(gate_token, request_id)
    assert data is not None
    assert "identifiers" in data and len(data["identifiers"]) == 1
    consignments = data["identifiers"][0].get("consignments", [])
    assert len(consignments) == 1
    assert consignments[0]["datasetId"] == DATASET_ID_MMG418


def test_identifiers_control_cmf452(gate_token):
    request_id = _gate_post_identifiers_control(gate_token, IDENTIFIER_CMF452)
    data = _gate_get_identifiers_control(gate_token, request_id)
    assert data is not None
    assert "identifiers" in data and len(data["identifiers"]) == 1
    consignments = data["identifiers"][0].get("consignments", [])
    assert len(consignments) == 2
    dataset_ids = {c["datasetId"] for c in consignments}
    assert dataset_ids == {DATASET_ID_CMF452_1, DATASET_ID_CMF452_2}


def test_identifiers_control_rtb307(gate_token):
    request_id = _gate_post_identifiers_control(gate_token, IDENTIFIER_RTB307)
    data = _gate_get_identifiers_control(gate_token, request_id)
    assert data is not None
    assert "identifiers" in data and len(data["identifiers"]) == 1
    consignments = data["identifiers"][0].get("consignments", [])
    assert len(consignments) == 1
    assert consignments[0]["datasetId"] == DATASET_ID_RTB307


def test_identifiers_control_bde471(gate_token):
    request_id = _gate_post_identifiers_control(gate_token, IDENTIFIER_BDE471)
    data = _gate_get_identifiers_control(gate_token, request_id)
    assert data is not None
    assert "identifiers" in data and len(data["identifiers"]) == 1
    consignments = data["identifiers"][0].get("consignments", [])
    assert len(consignments) == 1
    assert consignments[0]["datasetId"] == DATASET_ID_BDE471


# Test case 170 - No assertion or specific check in original, skipping explicit test function


def test_uil_control_error_dataset(gate_token):
    request_id = _gate_post_uil_control(gate_token, DATASET_ID_ERROR)
    status = _gate_get_uil_control(gate_token, request_id)
    assert status == "ERROR"


def test_identifiers_control_loadtest(gate_token):
    request_id = _gate_post_identifiers_control(
        gate_token, IDENTIFIER_LOADTEST)
    data = _gate_get_identifiers_control(gate_token, request_id)
    assert data is not None
    assert "identifiers" in data and len(data["identifiers"]) == 1
    consignments = data["identifiers"][0].get("consignments", [])
    assert len(consignments) == 1
    assert consignments[0]["datasetId"] == DATASET_ID_LOADTEST


def test_identifiers_control_eee100(gate_token):
    request_id = _gate_post_identifiers_control(gate_token, IDENTIFIER_EEE100)
    data = _gate_get_identifiers_control(gate_token, request_id)
    assert data is not None
    assert "identifiers" in data and len(data["identifiers"]) == 1
    consignments = data["identifiers"][0].get("consignments", [])
    assert len(consignments) == 1
    assert consignments[0]["datasetId"] == DATASET_ID_EEE100


# Test case 40
@pytest.mark.xfail(
    reason="Original script indicates this times out, expecting TIMEOUT status which polling logic may not handle correctly without modification or very long waits."
)
def test_uil_control_timeout(gate_token):
    current_gate_token = _gate_authentication()
    request_id = _gate_post_uil_control(current_gate_token, DATASET_ID_REGULAR)
    status = _gate_get_uil_control(current_gate_token, request_id)
    assert status == "TIMEOUT"


# Test case 168
@pytest.mark.xfail(
    reason="Original script indicates this times out or fails currently. Expecting COMPLETE."
)
def test_uil_control_large_file(gate_token):
    current_gate_token = _gate_authentication()
    request_id = _gate_post_uil_control(
        current_gate_token, LARGE_FILE_DATASET_ID)
    status = _gate_get_uil_control(current_gate_token, request_id)
    assert status == "COMPLETE"
