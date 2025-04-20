import xml.etree.ElementTree as ET
from io import BytesIO

from session import get_session


def upload_pmode(harmony_host, party_name, file_path):
    session = get_session(harmony_host)
    url = harmony_host + "/rest/pmode"

    # Parse XML and update the party attribute
    tree = ET.parse(file_path)
    root = tree.getroot()
    root.set('party', party_name)

    # Write XML to an in-memory file-like object
    xml_buffer = BytesIO()
    tree.write(xml_buffer, encoding='UTF-8', xml_declaration=True)
    xml_buffer.seek(0)

    files = {
        'file': ('master_pmode.xml', xml_buffer, 'text/xml')
    }

    data = {
        'description': 'Pmode file updated automatically by setup script.'
    }

    response = session.post(url, files=files, data=data, verify=False)
    response.raise_for_status()


def update_pmode_config(parties_dict, xml_file_path="master_pmode.xml", output_file_path=None):
    # Register the namespace
    ET.register_namespace("db", "http://domibus.eu/configuration")

    # Parse the existing XML file
    tree = ET.parse(xml_file_path)
    root = tree.getroot()

    # Set the first party as the default party in configuration attribute
    if parties_dict:
        root.set("party", list(parties_dict.keys())[0])

    # Find the parties section
    # We need to use {namespace}element format for finding elements with namespace
    parties_elem = root.find(".//{http://domibus.eu/configuration}configuration/businessProcesses/parties")
    if parties_elem is None:
        parties_elem = root.find(".//businessProcesses/parties")

    if parties_elem is None:
        print("Error: Could not find parties section in the XML file")
        return False

    # Remove existing party elements (but keep partyIdTypes)
    for party in parties_elem.findall("./party"):
        parties_elem.remove(party)

    # Add parties from the dictionary
    for party_name, party_endpoint in parties_dict.items():
        party = ET.SubElement(parties_elem, "party",
                              name=party_name,
                              endpoint=f"{party_endpoint}/services/msh?domain={party_name}")
        ET.SubElement(party, "identifier",
                      partyId=party_name,
                      partyIdType="partyTypeUrn")

    # Find the process element
    process = root.find(".//process")
    if process is None:
        print("Error: Could not find process section in the XML file")
        return False

    # Update initiator parties
    initiator_parties = process.find("./initiatorParties")
    if initiator_parties is None:
        initiator_parties = ET.SubElement(process, "initiatorParties")
    else:
        # Remove existing initiator parties
        for party in initiator_parties.findall("./initiatorParty"):
            initiator_parties.remove(party)

    # Add new initiator parties
    for party_name in parties_dict.keys():
        ET.SubElement(initiator_parties, "initiatorParty", name=party_name)

    # Update responder parties
    responder_parties = process.find("./responderParties")
    if responder_parties is None:
        responder_parties = ET.SubElement(process, "responderParties")
    else:
        # Remove existing responder parties
        for party in responder_parties.findall("./responderParty"):
            responder_parties.remove(party)

    # Add new responder parties
    for party_name in parties_dict.keys():
        ET.SubElement(responder_parties, "responderParty", name=party_name)

    # Write the updated XML to string
    xml_string = ET.tostring(root, encoding='utf-8', method='xml').decode('utf-8')

    # Replace the namespace prefix if it was changed
    xml_string = xml_string.replace("ns0:", "db:")

    # Fix the XML declaration if needed
    if not xml_string.startswith('<?xml'):
        xml_string = '<?xml version="1.0" encoding="UTF-8"?>\n' + xml_string

    # Write to file
    output_path = output_file_path if output_file_path else xml_file_path
    with open(output_path, 'w+', encoding='utf-8') as f:
        f.write(xml_string)

    print(f"Configuration updated and saved to {output_path}")
    return True
