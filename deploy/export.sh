#!/bin/bash

set -e

# --- Configuration ---
DEFAULT_OUTPUT_DIR="."
UTIL_CONTAINER_SERVICE_NAME="harmony-setup-util"
EXPORT_PATH_INSIDE_CONTAINER="/export"

# --- Helper Functions ---
log_error() {
    echo "[ERROR] $1" >&2
}

log_info() {
    echo "[INFO] $1"
}

# --- Argument Parsing ---
PROJECT_NAME="$1"
OUTPUT_DIR="${2:-$DEFAULT_OUTPUT_DIR}"

if [ -z "$PROJECT_NAME" ]; then
    log_error "Usage: $0 <project_name> [output_directory]"
    log_error "Example: $0 estonia ./exported_certs"
    exit 1
fi
log_info "Looking for utility container for project '$PROJECT_NAME'..."
CONTAINER_ID=$(docker compose -p "$PROJECT_NAME" ps -q "$UTIL_CONTAINER_SERVICE_NAME" 2>/dev/null)

if [ -z "$CONTAINER_ID" ]; then
    log_error "Could not find a running container for service '$UTIL_CONTAINER_SERVICE_NAME' in project '$PROJECT_NAME'."
    log_error "Ensure the project is running (e.g., 'docker compose -p $PROJECT_NAME up -d')."
    exit 1
fi

# Check if more than one container found (shouldn't happen with default scaling)
if [ $(echo "$CONTAINER_ID" | wc -l) -ne 1 ]; then
    log_error "Found multiple containers for '$UTIL_CONTAINER_SERVICE_NAME' in project '$PROJECT_NAME'. This is unexpected."
    docker compose -p "$PROJECT_NAME" ps "$UTIL_CONTAINER_SERVICE_NAME"
    exit 1
fi

log_info "Found utility container: $CONTAINER_ID"

# --- Prepare Host Output Directory ---
log_info "Ensuring host output directory exists: $OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"
OUTPUT_DIR_ABS=$(cd "$OUTPUT_DIR" && pwd)
log_info "Absolute output path: $OUTPUT_DIR_ABS"


# --- Execute Export Command in Container ---
log_info "Executing export command inside the container..."
docker exec "$CONTAINER_ID" rm -rf "$EXPORT_PATH_INSIDE_CONTAINER"
docker exec "$CONTAINER_ID" mkdir -p "$EXPORT_PATH_INSIDE_CONTAINER"

if ! docker exec "$CONTAINER_ID" python main.py export --output-dir "$EXPORT_PATH_INSIDE_CONTAINER"; then
    log_error "Export command failed inside the container. Check container logs:"
    log_error "docker logs $CONTAINER_ID"
    exit 1
fi

log_info "Export command finished successfully."

# --- Copy Exported Files from Container to Host ---
log_info "Copying exported files from container '$CONTAINER_ID:$EXPORT_PATH_INSIDE_CONTAINER/' to host '$OUTPUT_DIR_ABS/'..."

if ! docker cp "$CONTAINER_ID:$EXPORT_PATH_INSIDE_CONTAINER/." "$OUTPUT_DIR_ABS/"; then
     log_error "Failed to copy files from container. Check permissions and paths."
     log_error "Files might be in container at '$CONTAINER_ID:$EXPORT_PATH_INSIDE_CONTAINER/'"
     exit 1
fi

# --- Cleanup Inside Container ---
log_info "Cleaning up export directory inside the container..."
docker exec "$CONTAINER_ID" rm -rf "$EXPORT_PATH_INSIDE_CONTAINER"

log_info "--- Export process completed successfully! ---"
log_info "Certificates saved to: $OUTPUT_DIR_ABS"

exit 0
