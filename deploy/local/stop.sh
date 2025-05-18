#!/bin/bash

set -e

log_info() {
    echo "[INFO] $1"
}

log_success() {
    echo "[SUCCESS] $1"
}

log_info "Stopping the development environment..."
docker compose down

log_success "Development environment stopped successfully!"

exit 0
