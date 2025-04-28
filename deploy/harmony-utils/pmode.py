import xml.etree.ElementTree as ET
from io import BytesIO
import os

from config import logger, UPDATED_PMODE_FILENAME
from session import get_session, DEFAULT_VERIFY

PMODE_NAMESPACE = "http://domibus.eu/configuration"
ET.register_namespace("db", PMODE_NAMESPACE)

MASTER_PMODE_FILE_PATH = "/app/master_pmode.xml"


def upload_pmode(
    harmony_host: str,
    pmode_file_path: str,
    description: str = "PMode updated by setup script",
):
    session = get_session(harmony_host)
    url = f"{harmony_host}/rest/pmode"
    logger.info(
        f"""Uploading generated PMode from {
            pmode_file_path} to {harmony_host}"""
    )

    if not os.path.exists(pmode_file_path):
        logger.error(f"PMode file to upload not found: {pmode_file_path}")
        raise FileNotFoundError(
            f"PMode file to upload not found: {pmode_file_path}")

    with open(pmode_file_path, "rb") as file_content:
        files = {"file": ("master_pmode.xml", file_content, "text/xml")}
        data = {"description": description}

        try:
            response = session.post(
                url, files=files, data=data, verify=DEFAULT_VERIFY, timeout=30
            )
            response.raise_for_status()
            logger.info(f"PMode uploaded successfully to {harmony_host}")
        except Exception as e:
            logger.error(f"Failed to upload PMode to {harmony_host}: {e}")
            if hasattr(e, "response") and e.response is not None:
                logger.error(
                    f"""Response status: {e.response.status_code}, body: {
                        e.response.text
                    }"""
                )
            raise


def update_pmode_with_parties(
    parties_to_add: dict[str, str],
    initiating_party_name: str,
    output_xml_path: str = UPDATED_PMODE_FILENAME,
) -> bool:
    logger.info(
        f"""Generating PMode from master ({
            MASTER_PMODE_FILE_PATH
        }) for initiating party {initiating_party_name}"""
    )
    logger.info(f"Replacing parties with: {list(parties_to_add.keys())}")

    if not os.path.exists(MASTER_PMODE_FILE_PATH):
        logger.error(f"""Master PMode file not found at: {
                     MASTER_PMODE_FILE_PATH}""")
        return False

    try:
        tree = ET.parse(MASTER_PMODE_FILE_PATH)
        root = tree.getroot()
        ns = {"db": PMODE_NAMESPACE}

        root.set("party", initiating_party_name)
        logger.debug(f"Set root party attribute to '{initiating_party_name}'")

        parties_elem = root.find(".//db:parties", ns)
        if parties_elem is None:
            parties_elem = root.find(".//parties")
        if parties_elem is None:
            logger.error(
                "Could not find <parties> section in Master PMode XML.")
            return False

        process_elem = root.find(".//db:process", ns)
        if process_elem is None:
            process_elem = root.find(".//process")
        if process_elem is None:
            logger.error(
                "Could not find <process> section in Master PMode XML.")
            return False

        initiator_parties_elem = process_elem.find("./db:initiatorParties", ns)
        if initiator_parties_elem is None:
            initiator_parties_elem = process_elem.find("./initiatorParties")
        if initiator_parties_elem is None:
            initiator_parties_elem = ET.SubElement(
                process_elem, "initiatorParties")
            logger.warning("Created missing <initiatorParties> element.")

        responder_parties_elem = process_elem.find("./db:responderParties", ns)
        if responder_parties_elem is None:
            responder_parties_elem = process_elem.find("./responderParties")
        if responder_parties_elem is None:
            responder_parties_elem = ET.SubElement(
                process_elem, "responderParties")
            logger.warning("Created missing <responderParties> element.")

        logger.debug(
            "Removing existing <party> elements from <parties> section...")
        for party in parties_elem.findall("./db:party", ns):
            parties_elem.remove(party)
        for party in parties_elem.findall("./party"):
            if party.tag != "partyIdTypes":
                parties_elem.remove(party)

        logger.debug("Removing existing <initiatorParty> elements...")
        for iparty in initiator_parties_elem.findall("./db:initiatorParty", ns):
            initiator_parties_elem.remove(iparty)
        for iparty in initiator_parties_elem.findall("./initiatorParty"):
            initiator_parties_elem.remove(iparty)

        logger.debug("Removing existing <responderParty> elements...")
        for rparty in responder_parties_elem.findall("./db:responderParty", ns):
            responder_parties_elem.remove(rparty)
        for rparty in responder_parties_elem.findall("./responderParty"):
            responder_parties_elem.remove(rparty)

        party_id_type = "partyTypeUrn"
        try:
            party_id_type_elem = parties_elem.find(".//db:partyIdType", ns)
            if party_id_type_elem is not None:
                party_id_type = party_id_type_elem.get("name", party_id_type)
        except Exception:
            logger.warning(
                "Could not dynamically find partyIdType name, using default 'partyTypeUrn'."
            )

        logger.debug("Adding new party definitions...")
        for party_name, endpoint_url in parties_to_add.items():
            party_elem = ET.SubElement(
                parties_elem, "party", name=party_name, endpoint=endpoint_url
            )
            ET.SubElement(
                party_elem, "identifier", partyId=party_name, partyIdType=party_id_type
            )
            ET.SubElement(initiator_parties_elem,
                          "initiatorParty", name=party_name)
            ET.SubElement(responder_parties_elem,
                          "responderParty", name=party_name)

        xml_buffer = BytesIO()
        tree.write(xml_buffer, encoding="utf-8",
                   xml_declaration=True, method="xml")
        xml_content = xml_buffer.getvalue().decode("utf-8")
        if "xmlns:db" not in xml_content[:200]:
            logger.warning(
                "Namespace prefix 'db:' might not have been used correctly by ElementTree. Check output XML."
            )

        with open(output_xml_path, "w", encoding="utf-8") as f:
            f.write(xml_content)

        logger.info(
            f"""PMode configuration generated from master and saved to {
                output_xml_path
            }"""
        )
        return True

    except ET.ParseError as e:
        logger.error(
            f"""Failed to parse Master PMode XML file {
                MASTER_PMODE_FILE_PATH}: {e}"""
        )
        return False
    except Exception as e:
        logger.error(
            f"""An unexpected error occurred while generating PMode from master: {
                e}"""
        )
        return False


def download_pmode(harmony_host: str, output_path: str):
    session = get_session(harmony_host)
    url = f"{harmony_host}/rest/pmode"
    logger.info(f"Downloading PMode from {harmony_host}")

    try:
        response = session.get(url, verify=DEFAULT_VERIFY, timeout=30)
        response.raise_for_status()

        with open(output_path, "wb") as f:
            f.write(response.content)

        logger.info(f"PMode downloaded successfully from {harmony_host}")
        return True
    except Exception as e:
        logger.error(f"Failed to download PMode from {harmony_host}: {e}")
        if hasattr(e, "response") and e.response is not None:
            logger.error(
                f"""Response status: {e.response.status_code}, body: {
                    e.response.text
                }"""
            )
        return False


def extract_party_endpoints_from_pmode(pmode_file_path: str):
    parties_map = {}

    try:
        tree = ET.parse(pmode_file_path)
        root = tree.getroot()
        ns = {"db": PMODE_NAMESPACE}

        for party_elem in root.findall(".//db:party", ns):
            party_name = party_elem.get("name")
            party_endpoint = party_elem.get("endpoint")

            if party_name and party_endpoint:
                parties_map[party_name] = party_endpoint
                logger.debug(
                    f"""Found party in PMode with namespace: {party_name} -> {
                        party_endpoint
                    }"""
                )

        if not parties_map:
            for party_elem in root.findall(".//party"):
                party_name = party_elem.get("name")
                party_endpoint = party_elem.get("endpoint")

                if party_name and party_endpoint:
                    parties_map[party_name] = party_endpoint
                    logger.debug(
                        f"""Found party in PMode without namespace: {party_name} -> {
                            party_endpoint
                        }"""
                    )

        return parties_map
    except Exception as e:
        logger.error(f"Error extracting party endpoints from PMode: {e}")
        return {}


def get_existing_party_endpoints(harmony_host: str, temp_dir: str):
    temp_pmode_path = os.path.join(temp_dir, "current_pmode.xml")

    if download_pmode(harmony_host, temp_pmode_path):
        return extract_party_endpoints_from_pmode(temp_pmode_path)
    else:
        return {}
