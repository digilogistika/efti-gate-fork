#!/bin/bash

set -e

# --- Configuration ---
UTIL_CONTAINER_SERVICE_NAME="harmony-setup-util"
HARMONY_CONTAINER_SERVICE_NAME="harmony-gate"
CONNECT_PATH_INSIDE_CONTAINER="/tmp/connect"

# --- Helper Functions ---
log_error() {
    echo "[ERROR] $1" >&2
}

log_info() {
    echo "[INFO] $1"
}

# --- Argument Parsing ---
PROJECT_NAME="$1"
PEER_URL="$2"
PEER_NAME="$3"
PEER_TRUSTSTORE_CERT_HOST_PATH="$4"
PEER_TLS_CERT_HOST_PATH="$5"

if [ -z "$PROJECT_NAME" ] || [ -z "$PEER_URL" ] || [ -z "$PEER_NAME" ] || [ -z "$PEER_TRUSTSTORE_CERT_HOST_PATH" ] || [ -z "$PEER_TLS_CERT_HOST_PATH" ]; then
    log_info "Usage: $0 <project_name> <peer_url> <peer_name> <peer_truststore_cert_path> <peer_tls_cert_path>"
    log_info "Example: $0 estonia https://borduria-gate.blah.com:8080 borduria ./borduria_certs/borduria_truststore_cert.pem ./borduria_certs/borduria_tls_cert.pem"
    exit 1
fi

# --- Validate Host File Paths ---
if [ ! -f "$PEER_TRUSTSTORE_CERT_HOST_PATH" ]; then
    log_error "Peer truststore certificate file not found on host: $PEER_TRUSTSTORE_CERT_HOST_PATH"
    exit 1
fi
if [ ! -f "$PEER_TLS_CERT_HOST_PATH" ]; then
    log_error "Peer TLS certificate file not found on host: $PEER_TLS_CERT_HOST_PATH"
    exit 1
fi

# --- Find the Utility Container ---
log_info "Looking for utility container for project '$PROJECT_NAME'..."
CONTAINER_ID=$(docker compose -p "$PROJECT_NAME" ps -q "$UTIL_CONTAINER_SERVICE_NAME" 2>/dev/null)

if [ -z "$CONTAINER_ID" ]; then
    log_error "Could not find a running container for service '$UTIL_CONTAINER_SERVICE_NAME' in project '$PROJECT_NAME'."
    log_error "Ensure the project is running (e.g., 'docker compose -p $PROJECT_NAME up -d')."
    exit 1
fi
if [ $(echo "$CONTAINER_ID" | wc -l) -ne 1 ]; then
    log_error "Found multiple containers for '$UTIL_CONTAINER_SERVICE_NAME' in project '$PROJECT_NAME'."
    exit 1
fi
log_info "Found utility container: $CONTAINER_ID"

# --- Find is the peer is on the same machine ---
PEER_CONTAINER_ID=$(docker compose -p "$PEER_NAME" ps -q "$UTIL_CONTAINER_SERVICE_NAME" 2>/dev/null)
PEER_ON_SAME_HOST=false
if [ -z "$CONTAINER_ID" ]; then
    log_info "Did not find the peer on the same host."
else
    log_info "Found the peer on the same host. Connectig with docker network..."
    PEER_ON_SAME_HOST=true
fi

if [ $PEER_ON_SAME_HOST ]; then
    SELF_NETWORK_NAME=$(docker network ls -f name=$PROJECT_NAME -q)
    PEER_HARMONY_SERVICE_ID=$(docker compose -p "$PEER_NAME" ps -q "$HARMONY_CONTAINER_SERVICE_NAME" 2>/dev/null)
    docker network connect "$SELF_NETWORK_NAME" "$PEER_HARMONY_SERVICE_ID"
    if [ $? -eq 0 ]; then
        log_info "Successfully added $PEER_NAME to $SELF_NETWORK_NAME"
    else
        log_error "Failed to add $PEER_NAME to $SELF_NETWORK_NAME"
        exit 1
    fi
fi

# --- Prepare Container for File Transfer ---
log_info "Preparing temporary directory inside container..."
docker exec "$CONTAINER_ID" rm -rf "$CONNECT_PATH_INSIDE_CONTAINER"
docker exec "$CONTAINER_ID" mkdir -p "$CONNECT_PATH_INSIDE_CONTAINER"

PEER_TS_CERT_CONTAINER_PATH="$CONNECT_PATH_INSIDE_CONTAINER/peer_truststore.pem"
PEER_TLS_CERT_CONTAINER_PATH="$CONNECT_PATH_INSIDE_CONTAINER/peer_tls.pem"

# --- Copy Peer Certificates to Container ---
log_info "Copying peer certificates from host to container '$CONTAINER_ID:$CONNECT_PATH_INSIDE_CONTAINER/'..."
if ! docker cp "$PEER_TRUSTSTORE_CERT_HOST_PATH" "$CONTAINER_ID:$PEER_TS_CERT_CONTAINER_PATH"; then
    log_error "Failed to copy peer truststore cert to container."
    exit 1
fi
if ! docker cp "$PEER_TLS_CERT_HOST_PATH" "$CONTAINER_ID:$PEER_TLS_CERT_CONTAINER_PATH"; then
    log_error "Failed to copy peer TLS cert to container."
    exit 1
fi

# --- Execute Connect Command in Container ---
log_info "Executing connect command inside the container..."
CMD_ARGS=(
    python main.py connect
    --peer-name "$PEER_NAME"
    --peer-url "$PEER_URL"
    --peer-truststore-cert "$PEER_TS_CERT_CONTAINER_PATH"
    --peer-tls-cert "$PEER_TLS_CERT_CONTAINER_PATH"
)

cmd_string=$(printf "%q " "${CMD_ARGS[@]}")
log_info "Running: docker exec $CONTAINER_ID $cmd_string"

if ! docker exec "$CONTAINER_ID" "${CMD_ARGS[@]}"; then
    log_error "Connect command failed inside the container. Check container logs:"
    log_error "docker logs $CONTAINER_ID"
    docker exec "$CONTAINER_ID" rm -rf "$CONNECT_PATH_INSIDE_CONTAINER" &> /dev/null || true
    exit 1
fi

log_info "Connect command finished successfully."

# --- Cleanup Inside Container ---
log_info "Cleaning up temporary directory inside the container..."
docker exec "$CONTAINER_ID" rm -rf "$CONNECT_PATH_INSIDE_CONTAINER"

log_info "--- Connection process completed successfully! ---"
log_info "Project '$PROJECT_NAME' Gate should now be configured to connect with '$PEER_NAME'."

exit 0
