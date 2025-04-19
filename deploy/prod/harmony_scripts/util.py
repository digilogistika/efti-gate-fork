import logging
import subprocess
import tempfile
from pathlib import Path

from cryptography.hazmat.primitives import serialization
from cryptography.x509 import Certificate

from config import KEYSTORE_PASSWORD

logger = logging.getLogger(__name__)


def create_combined_p12(certificates: dict[str, Certificate], output_path="combined.p12"):
    """
    Create a combined PKCS12 keystore with just certificates (no keys).

    Args:
        certificates: Dictionary mapping aliases to certificates
        output_path: Path where the combined keystore will be saved
    """
    if not certificates:
        logger.error("No certificates provided")
        return False

    try:
        with tempfile.TemporaryDirectory() as tmpdir:
            tmp_path = Path(tmpdir)
            p12_files = []

            # Process each certificate
            for alias, cert in certificates.items():
                if not cert:
                    logger.warning(f"Skipping empty certificate for alias: {alias}")
                    continue

                # Save certificate
                cert_path = tmp_path / f"{alias}_cert.pem"
                with open(cert_path, "wb") as f:
                    f.write(cert.public_bytes(serialization.Encoding.PEM))

                # Create PKCS12 file with just the certificate (no private key)
                p12_path = tmp_path / f"{alias}.p12"
                p12_files.append(p12_path)

                try:
                    # Using keytool to directly import the certificate
                    subprocess.run([
                        "keytool", "-importcert",
                        "-file", str(cert_path),
                        "-alias", alias,
                        "-keystore", str(p12_path),
                        "-storetype", "PKCS12",
                        "-storepass", KEYSTORE_PASSWORD,
                        "-noprompt"
                    ], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                except subprocess.CalledProcessError as e:
                    logger.error(f"Failed to create certificate store for {alias}: {e}")
                    return False

            # Create an empty keystore first
            try:
                # Create an empty keystore
                subprocess.run([
                    "keytool", "-genkeypair",
                    "-keystore", output_path,
                    "-storepass", KEYSTORE_PASSWORD,
                    "-keypass", KEYSTORE_PASSWORD,
                    "-alias", "dummy",
                    "-dname", "CN=dummy",
                    "-keyalg", "RSA",
                    "-keysize", "2048",
                    "-storetype", "PKCS12"
                ], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

                # Delete the dummy entry
                subprocess.run([
                    "keytool", "-delete",
                    "-alias", "dummy",
                    "-keystore", output_path,
                    "-storepass", KEYSTORE_PASSWORD
                ], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            except subprocess.CalledProcessError as e:
                logger.error(f"Failed to create base keystore: {e}")
                return False

            # Import all certificates
            for p12_path in p12_files:
                try:
                    subprocess.run([
                        "keytool", "-importkeystore",
                        "-srckeystore", str(p12_path),
                        "-srcstoretype", "PKCS12",
                        "-srcstorepass", KEYSTORE_PASSWORD,
                        "-destkeystore", output_path,
                        "-deststoretype", "PKCS12",
                        "-deststorepass", KEYSTORE_PASSWORD,
                        "-noprompt"
                    ], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                except subprocess.CalledProcessError as e:
                    logger.error(f"Failed to import certificate from {p12_path}: {e}")
                    return False

            logger.info(f"Certificate store created successfully at: {output_path}")
            return True

    except Exception as e:
        logger.error(f"Error creating certificate store: {e}")
        return False
