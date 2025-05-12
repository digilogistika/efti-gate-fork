import xml.etree.ElementTree as ET
from io import BytesIO
import os

from config import logger, UPDATED_PMODE_FILENAME, MASTER_PMODE_FILE_PATH
from session import get_session, DEFAULT_VERIFY

PMODE_NAMESPACE = "http://domibus.eu/configuration"
ET.register_namespace("db", PMODE_NAMESPACE)


def upload_pmode(
    harmony_host: str,
    pmode_file_path: str,
    description: str = "PMode updated by setup script",
):
    session = get_session(harmony_host)
    url = f"{harmony_host}/rest/pmode"
    logger.info(
        f"""Uploading generated PMode from {pmode_file_path} to {harmony_host}"""
    )

    if not os.path.exists(pmode_file_path):
        logger.error(f"PMode file to upload not found: {pmode_file_path}")
        raise FileNotFoundError(f"PMode file to upload not found: {pmode_file_path}")

    with open(pmode_file_path, "rb") as file_content:
        files = {"file": ("master_pmode.xml", file_content, "text/xml")}
        data = {"description": description}

        try:
            response = session.post(
                url, files=files, data=data, verify=DEFAULT_VERIFY, timeout=30
            )
            response.raise_for_status()
            logger.info(f"PMode uploaded successfully to {harmony_host}")
            return True
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
    master_pmode_path: str = MASTER_PMODE_FILE_PATH,
    replace_existing: bool = True,
) -> bool:
    logger.info(
        f"""Generating PMode from master ({master_pmode_path}) for initiating party {
            initiating_party_name
        }"""
    )
    logger.info(f"Parties to include: {list(parties_to_add.keys())}")

    if not os.path.exists(master_pmode_path):
        logger.error(f"""Master PMode file not found at: {master_pmode_path}""")
        return False

    try:
        tree = ET.parse(master_pmode_path)
        root = tree.getroot()
        ns = {"db": PMODE_NAMESPACE}

        root.set("party", initiating_party_name)
        logger.debug(f"Set root party attribute to '{initiating_party_name}'")

        parties_elem = root.find(".//db:parties", ns)
        if parties_elem is None:
            parties_elem = root.find(".//parties")
        if parties_elem is None:
            logger.error("Could not find <parties> section in Master PMode XML.")
            return False

        process_elem = root.find(".//db:process", ns)
        if process_elem is None:
            process_elem = root.find(".//process")
        if process_elem is None:
            logger.error("Could not find <process> section in Master PMode XML.")
            return False

        initiator_parties_elem = process_elem.find("./db:initiatorParties", ns)
        if initiator_parties_elem is None:
            initiator_parties_elem = process_elem.find("./initiatorParties")
        if initiator_parties_elem is None:
            initiator_parties_elem = ET.SubElement(process_elem, "initiatorParties")
            logger.warning("Created missing <initiatorParties> element.")

        responder_parties_elem = process_elem.find("./db:responderParties", ns)
        if responder_parties_elem is None:
            responder_parties_elem = process_elem.find("./responderParties")
        if responder_parties_elem is None:
            responder_parties_elem = ET.SubElement(process_elem, "responderParties")
            logger.warning("Created missing <responderParties> element.")

        if replace_existing:
            logger.debug("Removing existing <party> elements from <parties> section...")
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
        else:
            existing_parties = set()
            for party in parties_elem.findall("./db:party", ns):
                existing_parties.add(party.get("name"))
            for party in parties_elem.findall("./party"):
                if party.get("name"):
                    existing_parties.add(party.get("name"))

            logger.debug(f"Existing parties found: {existing_parties}")

            for party_name in parties_to_add.keys():
                for party in parties_elem.findall(
                    f"./db:party[@name='{party_name}']", ns
                ):
                    parties_elem.remove(party)
                for party in parties_elem.findall(f"./party[@name='{party_name}']"):
                    parties_elem.remove(party)

                for iparty in initiator_parties_elem.findall(
                    f"./db:initiatorParty[@name='{party_name}']", ns
                ):
                    initiator_parties_elem.remove(iparty)
                for iparty in initiator_parties_elem.findall(
                    f"./initiatorParty[@name='{party_name}']"
                ):
                    initiator_parties_elem.remove(iparty)

                for rparty in responder_parties_elem.findall(
                    f"./db:responderParty[@name='{party_name}']", ns
                ):
                    responder_parties_elem.remove(rparty)
                for rparty in responder_parties_elem.findall(
                    f"./responderParty[@name='{party_name}']"
                ):
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

        logger.debug("Adding parties definitions...")
        for party_name, endpoint_url in parties_to_add.items():
            logger.debug(f"Adding party: {party_name} -> {endpoint_url}")
            party_elem = ET.SubElement(
                parties_elem, "party", name=party_name, endpoint=endpoint_url
            )
            ET.SubElement(
                party_elem, "identifier", partyId=party_name, partyIdType=party_id_type
            )
            ET.SubElement(initiator_parties_elem, "initiatorParty", name=party_name)
            ET.SubElement(responder_parties_elem, "responderParty", name=party_name)

        xml_buffer = BytesIO()
        tree.write(xml_buffer, encoding="utf-8", xml_declaration=True, method="xml")
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

        if output_xml_path != master_pmode_path:
            logger.info(
                f"Updating master PMode file with the generated configuration..."
            )
            with open(master_pmode_path, "w", encoding="utf-8") as f:
                f.write(xml_content)
            logger.info(f"Master PMode file updated at {master_pmode_path}")

        return True

    except ET.ParseError as e:
        logger.error(
            f"""Failed to parse Master PMode XML file {master_pmode_path}: {e}"""
        )
        return False
    except Exception as e:
        logger.error(
            f"""An unexpected error occurred while generating PMode from master: {e}"""
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
    logger.info(
        f"""Getting existing party endpoints from master PMode file: {
            MASTER_PMODE_FILE_PATH
        }"""
    )
    if os.path.exists(MASTER_PMODE_FILE_PATH):
        parties = extract_party_endpoints_from_pmode(MASTER_PMODE_FILE_PATH)
        filtered_parties = {k: v for k, v in parties.items() if k != "acme"}
        return filtered_parties
    else:
        logger.warning(
            "Master PMode file not found, no existing parties will be included"
        )
        return {}
