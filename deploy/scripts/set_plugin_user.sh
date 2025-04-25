#!/usr/bin/env bash

set -e
set -o pipefail

usage() {
  echo "Usage: $0 <harmony_host>"
  exit 1
}

CMD_HARMONY_HOST=$1
CONFIG_FILE=config.env

if [[ -z "$CMD_HARMONY_HOST" ]]; then
  echo "Error: Harmony host is required."
  usage
fi

if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "Error: Configuration file not found: $CONFIG_FILE"
    exit 1
fi
if [[ ! -r "$CONFIG_FILE" ]]; then
    echo "Error: Configuration file not readable: $CONFIG_FILE"
    exit 1
fi

source <(grep -E -v '^\s*(#|$)' "$CONFIG_FILE")
HARMONY_HOST="$CMD_HARMONY_HOST"

HARMONY_HOST="${HARMONY_HOST%/}"

: "${HARMONY_HOST?Error: HARMONY_HOST is required.}"
: "${HARMONY_USERNAME?Error: HARMONY_USERNAME not set in config file '$CONFIG_FILE'.}"
: "${HARMONY_PASSWORD?Error: HARMONY_PASSWORD not set in config file '$CONFIG_FILE'.}"
: "${PLUGIN_USER_PASSWORD?Error: PLUGIN_USER_PASSWORD not set in config file '$CONFIG_FILE'.}"

PLUGIN_USERNAME="service_account"

COOKIE_JAR=$(mktemp)
trap 'rm -f "$COOKIE_JAR"' EXIT

echo "--- Starting Harmony Plugin User Setup ---"
echo "Target Host: ${HARMONY_HOST}"
echo "Plugin Username: ${PLUGIN_USERNAME}"

# --- Step 1: Initial GET to get XSRF Token ---
AUTH_URL="${HARMONY_HOST}/rest/security/authentication"
echo "Attempting initial GET from ${AUTH_URL}..."

initial_get_response_headers=$(curl -k -s -c "$COOKIE_JAR" -D - "${AUTH_URL}" -o /dev/null)
XSRF_TOKEN=$(awk '/XSRF-TOKEN/ {print $7}' "$COOKIE_JAR")

if [[ -z "$XSRF_TOKEN" ]]; then
  echo "Error: Could not retrieve XSRF-TOKEN."
  echo "Headers received:"
  echo "$initial_get_response_headers"
  echo "Cookie Jar content:"
  cat "$COOKIE_JAR"
  exit 1
fi
echo "Successfully retrieved XSRF-TOKEN."

# --- Step 2: POST to Authenticate ---
echo "Attempting authentication..."

auth_response=$(curl -k -s -f -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: ${XSRF_TOKEN}" \
  -X POST \
  -d "{\"username\":\"${HARMONY_USERNAME}\",\"password\":\"${HARMONY_PASSWORD}\"}" \
  "${AUTH_URL}" \
  -o /dev/null -w "%{http_code}")

if [[ "$auth_response" -lt 200 || "$auth_response" -ge 300 ]]; then
    echo "Error: Authentication failed. HTTP status code: $auth_response"
    exit 1
fi
echo "Authentication successful (HTTP Status: ${auth_response}). Session cookies stored."

# --- Step 3: PUT to Set Plugin User ---
PLUGIN_URL="${HARMONY_HOST}/rest/plugin/users"
echo "Attempting to set plugin user '${PLUGIN_USERNAME}' via PUT to ${PLUGIN_URL}..."

JSON_PAYLOAD=$(jq -n --arg un "$PLUGIN_USERNAME" --arg pw "$PLUGIN_USER_PASSWORD" '
[
  {
    "status": "NEW",
    "userName": $un,
    "active": true,
    "suspended": false,
    "authenticationType": "BASIC",
    "authRoles": "ROLE_ADMIN",
    "password": $pw
  }
]
')

put_response=$(curl -k -s -f -b "$COOKIE_JAR" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: ${XSRF_TOKEN}" \
  -X PUT \
  -d "$JSON_PAYLOAD" \
  "${PLUGIN_URL}" \
  -o /dev/null -w "%{http_code}")

if [[ "$put_response" -lt 200 || "$put_response" -ge 300 ]]; then
    echo "Error: Failed to set plugin user. HTTP status code: $put_response"
    exit 1
fi

echo "Successfully set plugin user '${PLUGIN_USERNAME}' (HTTP Status: ${put_response})."
echo "--- Harmony Plugin User Setup Complete ---"

exit 0
