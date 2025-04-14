import pytest
import time
import requests
import os
from config import *


def authenticate_simulator():
    url = KC_SIMULATOR_URL
    payload = {
        "grant_type": "client_credentials",
        "client_id": "simulator",
        "client_secret": KC_SIMULATOR_SECRET,
    }
    headers = {"Content-Type": "application/x-www-form-urlencoded"}

    response = requests.post(
        url, data=payload, headers=headers, timeout=AUTH_TIMEOUT)
    response.raise_for_status()
    data = response.json()
    return data.get("access_token")


def upload_file_to_simulator(token, file_path):
    url = f"{SIMULATOR_URL}/identifiers/upload/file"
    headers = {"Authorization": f"Bearer {token}"}

    with open(file_path, "rb") as f:
        files = {"file": (os.path.basename(file_path), f, "application/xml")}
        response = requests.post(
            url, headers=headers, files=files, timeout=UPLOAD_TIMEOUT
        )

    response.raise_for_status()
    return response


def upload_identifiers_to_simulator(
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

    response = requests.post(
        url, headers=headers, json=payload, timeout=CONTROL_TIMEOUT
    )
    response.raise_for_status()
    return response


def authenticate_gate():
    url = KC_GATE_URL
    payload = {
        "username": GATE_USER,
        "password": GATE_PASSWORD,
        "grant_type": "password",
        "client_id": "gate",
        "client_secret": KC_GATE_SECRET,
    }
    headers = {"Content-Type": "application/x-www-form-urlencoded"}

    response = requests.post(
        url, data=payload, headers=headers, timeout=AUTH_TIMEOUT)
    response.raise_for_status()
    data = response.json()
    return data.get("access_token")


def post_uil_control(token, dataset_id):
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

    response = requests.post(
        url, headers=headers, json=payload, timeout=CONTROL_TIMEOUT
    )
    print("HERERERERE", response.text)
    response.raise_for_status()
    return response.json()


def get_uil_control(token, request_id):
    url = f"{GATE_URL}/v1/control/uil"
    headers = {"Authorization": f"Bearer {token}"}
    params = {"requestId": request_id}

    response = requests.get(
        url, headers=headers, params=params, timeout=CONTROL_TIMEOUT
    )
    response.raise_for_status()
    return response.json()


def post_identifiers_control(token, identifier):
    url = f"{GATE_URL}/v1/control/identifiers"
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
    }
    payload = {"identifier": identifier, "eftiGateIndicator": [GATE_INDICATOR]}

    response = requests.post(
        url, headers=headers, json=payload, timeout=CONTROL_TIMEOUT
    )
    response.raise_for_status()
    return response.json()


def get_identifiers_control(token, request_id):
    url = f"{GATE_URL}/v1/control/identifiers"
    headers = {"Authorization": f"Bearer {token}"}
    params = {"requestId": request_id}

    response = requests.get(
        url, headers=headers, params=params, timeout=CONTROL_TIMEOUT
    )
    response.raise_for_status()
    return response.json()


def wait_for_uil_status(token, request_id, target_statuses=None, max_attempts=12):
    if target_statuses is None:
        target_statuses = ["TIMEOUT", "COMPLETE", "ERROR"]

    for _ in range(max_attempts):
        time.sleep(POLLING_INTERVAL)
        response = get_uil_control(token, request_id)
        status = response.get("status")
        if status in target_statuses:
            return status

    return status


@pytest.fixture(scope="module")
def simulator_token():
    token = authenticate_simulator()
    assert token is not None, "Failed to obtain simulator token"
    return token


@pytest.fixture(scope="module")
def gate_token():
    token = authenticate_gate()
    assert token is not None, "Failed to obtain gate token"
    return token


@pytest.fixture(scope="module")
def setup_data(simulator_token):
    upload_file_to_simulator(simulator_token, XML_FILE_PATH)
    upload_file_to_simulator(simulator_token, XML_FILE_2_PATH)
    upload_file_to_simulator(simulator_token, XML_LARGE_FILE_PATH)

    upload_identifiers_to_simulator(simulator_token, FILE_DATASET_ID)
    upload_identifiers_to_simulator(simulator_token, FILE_2_DATASET_ID)
    upload_identifiers_to_simulator(simulator_token, LARGE_FILE_DATASET_ID)
    upload_identifiers_to_simulator(simulator_token, DATASET_ID_REGULAR)
    upload_identifiers_to_simulator(
        simulator_token, DATASET_ID_MMG418, identifier=IDENTIFIER_MMG418
    )
    upload_identifiers_to_simulator(
        simulator_token, DATASET_ID_CMF452_1, identifier=IDENTIFIER_CMF452
    )
    upload_identifiers_to_simulator(
        simulator_token, DATASET_ID_CMF452_2, identifier=IDENTIFIER_CMF452
    )
    upload_identifiers_to_simulator(
        simulator_token, DATASET_ID_RTB307, identifier=IDENTIFIER_RTB307
    )
    upload_identifiers_to_simulator(
        simulator_token, DATASET_ID_BDE471, identifier=IDENTIFIER_BDE471
    )
    upload_identifiers_to_simulator(
        simulator_token, DATASET_ID_LOADTEST, identifier=IDENTIFIER_LOADTEST
    )
    upload_identifiers_to_simulator(
        simulator_token, DATASET_ID_EEE100, identifier=IDENTIFIER_EEE100
    )

    return True


# Test Case 30
@pytest.mark.dependency()
def test_uil_control_first_file(gate_token, setup_data):
    response = post_uil_control(gate_token, FILE_DATASET_ID)
    request_id = response.get("requestId")
    assert request_id is not None, "Failed to get requestId"

    status = wait_for_uil_status(gate_token, request_id)
    assert status == "COMPLETE", f"Expected status COMPLETE, got {status}"


@pytest.mark.dependency(depends=["test_uil_control_first_file"])
def test_uil_control_second_file(gate_token):
    response = post_uil_control(gate_token, FILE_2_DATASET_ID)
    request_id = response.get("requestId")
    assert request_id is not None, "Failed to get requestId"

    status = wait_for_uil_status(gate_token, request_id)
    assert status == "COMPLETE", f"Expected status COMPLETE, got {status}"


@pytest.mark.dependency(depends=["test_uil_control_second_file"])
def test_identifiers_mmg418(gate_token):
    response = post_identifiers_control(gate_token, IDENTIFIER_MMG418)
    request_id = response.get("requestId")
    assert request_id is not None, "Failed to get requestId"

    time.sleep(POLLING_INTERVAL)
    data = get_identifiers_control(gate_token, request_id)

    consignments = data["identifiers"][0]["consignments"]
    assert len(consignments) == 1, f"Expected 1 consignment, got {
        len(consignments)}"
    assert consignments[0]["datasetId"] == DATASET_ID_MMG418, (
        "Wrong datasetId for MMG418"
    )


@pytest.mark.dependency(depends=["test_identifiers_mmg418"])
def test_identifiers_cmf452(gate_token):
    response = post_identifiers_control(gate_token, IDENTIFIER_CMF452)
    request_id = response.get("requestId")
    assert request_id is not None, "Failed to get requestId"

    time.sleep(POLLING_INTERVAL)
    data = get_identifiers_control(gate_token, request_id)

    consignments = data["identifiers"][0]["consignments"]
    assert len(consignments) == 2, f"Expected 2 consignments, got {
        len(consignments)}"

    dataset_ids = {consignments[0]["datasetId"], consignments[1]["datasetId"]}
    expected_ids = {DATASET_ID_CMF452_1, DATASET_ID_CMF452_2}
    assert dataset_ids == expected_ids, (
        f"Expected datasets {expected_ids}, got {dataset_ids}"
    )


@pytest.mark.dependency(depends=["test_identifiers_cmf452"])
def test_identifiers_rtb307(gate_token):
    response = post_identifiers_control(gate_token, IDENTIFIER_RTB307)
    request_id = response.get("requestId")
    assert request_id is not None, "Failed to get requestId"

    time.sleep(POLLING_INTERVAL)
    data = get_identifiers_control(gate_token, request_id)

    consignments = data["identifiers"][0]["consignments"]
    assert len(consignments) == 1, f"Expected 1 consignment, got {
        len(consignments)}"
    assert consignments[0]["datasetId"] == DATASET_ID_RTB307, (
        "Wrong datasetId for RTB307"
    )


@pytest.mark.dependency(depends=["test_identifiers_rtb307"])
def test_identifiers_bde471(gate_token):
    response = post_identifiers_control(gate_token, IDENTIFIER_BDE471)
    request_id = response.get("requestId")
    assert request_id is not None, "Failed to get requestId"

    time.sleep(POLLING_INTERVAL)
    data = get_identifiers_control(gate_token, request_id)

    consignments = data["identifiers"][0]["consignments"]
    assert len(consignments) == 1, f"Expected 1 consignment, got {
        len(consignments)}"
    assert consignments[0]["datasetId"] == DATASET_ID_BDE471, (
        "Wrong datasetId for BDE471"
    )


# Test case 39
@pytest.mark.dependency(depends=["test_identifiers_bde471"])
def test_uil_control_error(gate_token):
    response = post_uil_control(gate_token, DATASET_ID_ERROR)
    request_id = response.get("requestId")
    assert request_id is not None, "Failed to get requestId"

    status = wait_for_uil_status(gate_token, request_id)
    assert status == "ERROR", f"Expected status ERROR, got {status}"


# Test case 169
@pytest.mark.dependency(depends=["test_uil_control_error"])
def test_identifiers_loadtest(gate_token):
    response = post_identifiers_control(gate_token, IDENTIFIER_LOADTEST)
    request_id = response.get("requestId")
    assert request_id is not None, "Failed to get requestId"

    time.sleep(POLLING_INTERVAL)  # Wait for processing
    data = get_identifiers_control(gate_token, request_id)

    consignments = data["identifiers"][0]["consignments"]
    assert len(consignments) == 1, f"Expected 1 consignment, got {
        len(consignments)}"
    assert consignments[0]["datasetId"] == DATASET_ID_LOADTEST, (
        "Wrong datasetId for LOADTEST"
    )


# Test case 50
@pytest.mark.dependency(depends=["test_identifiers_loadtest"])
def test_identifiers_eee100(gate_token):
    response = post_identifiers_control(gate_token, IDENTIFIER_EEE100)
    request_id = response.get("requestId")
    assert request_id is not None, "Failed to get requestId"

    time.sleep(POLLING_INTERVAL)
    data = get_identifiers_control(gate_token, request_id)

    consignments = data["identifiers"][0]["consignments"]
    assert len(consignments) == 1, f"Expected 1 consignment, got {
        len(consignments)}"
    assert consignments[0]["datasetId"] == DATASET_ID_EEE100, (
        "Wrong datasetId for EEE100"
    )


# Test case 40
@pytest.mark.dependency(depends=["test_identifiers_eee100"])
def test_uil_control_timeout(gate_token):
    response = post_uil_control(gate_token, DATASET_ID_REGULAR)
    request_id = response.get("requestId")
    assert request_id is not None, "Failed to get requestId"

    status = wait_for_uil_status(gate_token, request_id, max_attempts=30)
    assert status == "TIMEOUT", f"Expected status TIMEOUT, got {status}"


# Test case 168
@pytest.mark.dependency(depends=["test_uil_control_timeout"])
def test_uil_control_large_file(gate_token):
    response = post_uil_control(gate_token, LARGE_FILE_DATASET_ID)
    request_id = response.get("requestId")
    assert request_id is not None, "Failed to get requestId"

    status = wait_for_uil_status(gate_token, request_id, max_attempts=30)
    assert status in ["TIMEOUT", "COMPLETE"], (
        f"Expected TIMEOUT or COMPLETE, got {status}"
    )
