import subprocess
import os

from cryptography import x509
from cryptography.hazmat.backends import default_backend

from config import logger


def load_pem_cert(pem_path: str) -> x509.Certificate | None:
    try:
        with open(pem_path, "rb") as f:
            cert = x509.load_pem_x509_certificate(f.read(), default_backend())
        return cert
    except FileNotFoundError:
        logger.error(f"Certificate file not found: {pem_path}")
        return None
    except Exception as e:
        logger.error(f"Failed to load PEM certificate from {pem_path}: {e}")
        return None


def create_combined_p12(
    certificates_map: dict[str, str],
    output_p12_path: str,
    keystore_password: str,
) -> bool:
    if not certificates_map:
        logger.error("No certificates provided to create combined P12.")
        return False

    logger.info(
        f"""Creating combined P12 store at {output_p12_path} with aliases: {
            list(certificates_map.keys())
        }"""
    )

    output_dir = os.path.dirname(output_p12_path)
    if output_dir:
        os.makedirs(output_dir, exist_ok=True)

    keytool_cmd_base = [
        "keytool",
        "-importcert",
        "-noprompt",
        "-keystore",
        output_p12_path,
        "-storetype",
        "PKCS12",
        "-storepass",
        keystore_password,
    ]

    first_import = True
    for alias, cert_path in certificates_map.items():
        if not os.path.exists(cert_path):
            logger.error(
                f"""Certificate file for alias '{alias}' not found: {
                    cert_path
                }. Skipping."""
            )
            continue

        logger.debug(f"""Importing certificate from {
                     cert_path} with alias '{alias}'""")
        cmd = keytool_cmd_base + ["-alias", alias, "-file", cert_path]

        try:
            result = subprocess.run(
                cmd, check=True, capture_output=True, text=True, timeout=15
            )
            logger.debug(f"""Keytool output for alias '{
                         alias}': {result.stdout}""")
            if result.stderr:
                logger.warning(
                    f"""Keytool stderr for alias '{alias}': {result.stderr}"""
                )
            first_import = False

        except FileNotFoundError:
            logger.error(
                "`keytool` command not found. Is Java (JDK/JRE) installed and in PATH?"
            )
            return False
        except subprocess.CalledProcessError as e:
            logger.error(
                f"""Keytool failed to import certificate for alias '{alias}' from {
                    cert_path
                }"""
            )
            logger.error(f"Command: {' '.join(e.cmd)}")
            logger.error(f"Return code: {e.returncode}")
            logger.error(f"Stderr: {e.stderr}")
            logger.error(f"Stdout: {e.stdout}")
            if not first_import and os.path.exists(output_p12_path):
                try:
                    os.remove(output_p12_path)
                except OSError:
                    pass
            return False
        except subprocess.TimeoutExpired:
            logger.error(f"Keytool command timed out for alias '{alias}'")
            return False

    if first_import:
        logger.error("No valid certificates were found or imported.")
        return False

    logger.info(f"""Combined P12 store created successfully at: {
                output_p12_path}""")
    return True
