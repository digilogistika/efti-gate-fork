import os

# Harmony settings
HARMONY_USERNAME = os.environ.get("HARMONY_USERNAME", "harmony")
HARMONY_PASSWORD = os.environ.get("HARMONY_PASSWORD", "Secret")

# get ACCESS_POINT_ prefixed env var keys
keys = [key for key in os.environ.keys() if key.startswith("ACCESS_POINT_")]

# for each key get the host name for it.
ACCESS_POINT_HOSTS = {}
for key in keys:
    # get the host name
    host = os.environ.get(key)
    # remove the prefix from key
    key = key.replace("ACCESS_POINT_", "").lower()
    # set the value in the dict
    ACCESS_POINT_HOSTS[key] = host

ACCESS_POINTS = {
    "borduria": "https://harmony-borduria:8443",
    "listenbourg": "https://harmony-listenbourg:8443",
} if not ACCESS_POINT_HOSTS else ACCESS_POINT_HOSTS

PLUGIN_USER_PASSWORD = os.environ.get("PLUGIN_USER_PASSWORD", "Azerty59*1234567")

# certificate passwords
TRUSTSTORE_PASSWORD = os.environ.get("TRUSTSTORE_PASSWORD", "changeit")
TLS_TRUSTSTORE_PASSWORD = os.environ.get("TLS_TRUSTSTORE_PASSWORD", "changeit")
KEYSTORE_PASSWORD = os.environ.get("KEYSTORE_PASSWORD", "changeit")
