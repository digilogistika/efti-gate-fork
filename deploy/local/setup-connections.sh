#!/bin/bash

set -e

ESTONIA_UTIL_CONTAINER="estonia-harmony-setup-util"
BORDURIA_UTIL_CONTAINER="borduria-harmony-setup-util"
OUTPUT_DIR="./certs"

log_error() {
    echo "[ERROR] $1" >&2
}

log_info() {
    echo "[INFO] $1"
}

log_success() {
    echo "[SUCCESS] $1"
}

mkdir -p "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR/estonia"
mkdir -p "$OUTPUT_DIR/borduria"

log_info "Exporting Estonia certificates..."
docker compose exec "$ESTONIA_UTIL_CONTAINER" rm -rf /export
docker compose exec "$ESTONIA_UTIL_CONTAINER" mkdir -p /export

if ! docker compose exec "$ESTONIA_UTIL_CONTAINER" python main.py export --output-dir /export; then
    log_error "Failed to export Estonia certificates"
    exit 1
fi

ESTONIA_CONTAINER_ID=$(docker compose ps -q "$ESTONIA_UTIL_CONTAINER")
if ! docker cp "$ESTONIA_CONTAINER_ID:/export/." "$OUTPUT_DIR/estonia/"; then
    log_error "Failed to copy Estonia certificates"
    exit 1
fi

docker compose exec "$ESTONIA_UTIL_CONTAINER" rm -rf /export

log_info "Exporting Borduria certificates..."
docker compose exec "$BORDURIA_UTIL_CONTAINER" rm -rf /export
docker compose exec "$BORDURIA_UTIL_CONTAINER" mkdir -p /export

if ! docker compose exec "$BORDURIA_UTIL_CONTAINER" python main.py export --output-dir /export; then
    log_error "Failed to export Borduria certificates"
    exit 1
fi

BORDURIA_CONTAINER_ID=$(docker compose ps -q "$BORDURIA_UTIL_CONTAINER")
if ! docker cp "$BORDURIA_CONTAINER_ID:/export/." "$OUTPUT_DIR/borduria/"; then
    log_error "Failed to copy Borduria certificates"
    exit 1
fi

docker compose exec "$BORDURIA_UTIL_CONTAINER" rm -rf /export

log_info "Connecting Estonia to Borduria..."
docker compose exec "$ESTONIA_UTIL_CONTAINER" mkdir -p /tmp/connect
docker cp "$OUTPUT_DIR/borduria/borduria_truststore_cert.pem" "$ESTONIA_CONTAINER_ID:/tmp/connect/peer_truststore.pem"
docker cp "$OUTPUT_DIR/borduria/borduria_tls_cert.pem" "$ESTONIA_CONTAINER_ID:/tmp/connect/peer_tls.pem"

if ! docker compose exec "$ESTONIA_UTIL_CONTAINER" python main.py connect \
    --peer-name borduria \
    --peer-url https://borduria-harmony-1:8443 \
    --peer-truststore-cert /tmp/connect/peer_truststore.pem \
    --peer-tls-cert /tmp/connect/peer_tls.pem; then
    log_error "Failed to connect Estonia to Borduria"
    exit 1
fi

docker compose exec "$ESTONIA_UTIL_CONTAINER" rm -rf /tmp/connect

# --- Connect Borduria to Estonia ---
log_info "Connecting Borduria to Estonia..."
docker compose exec "$BORDURIA_UTIL_CONTAINER" mkdir -p /tmp/connect
docker cp "$OUTPUT_DIR/estonia/estonia_truststore_cert.pem" "$BORDURIA_CONTAINER_ID:/tmp/connect/peer_truststore.pem"
docker cp "$OUTPUT_DIR/estonia/estonia_tls_cert.pem" "$BORDURIA_CONTAINER_ID:/tmp/connect/peer_tls.pem"

if ! docker compose exec "$BORDURIA_UTIL_CONTAINER" python main.py connect \
    --peer-name estonia \
    --peer-url https://estonia-harmony-1:8443 \
    --peer-truststore-cert /tmp/connect/peer_truststore.pem \
    --peer-tls-cert /tmp/connect/peer_tls.pem; then
    log_error "Failed to connect Borduria to Estonia"
    exit 1
fi

docker compose exec "$BORDURIA_UTIL_CONTAINER" rm -rf /tmp/connect

log_success "Successfully exported certificates and connected both environments!"
log_info "Certificates saved to:"
log_info "  - Estonia: $OUTPUT_DIR/estonia/"
log_info "  - Borduria: $OUTPUT_DIR/borduria/"

exit 0
