#!/bin/bash

set -e

log_error() {
    echo "[ERROR] $1" >&2
}

log_info() {
    echo "[INFO] $1"
}

log_success() {
    echo "[SUCCESS] $1"
}

log_info "Starting the development environment..."
docker compose up -d

log_info "Waiting for harmonies..."

while true; do
  if curl -k -s -o /dev/null -w "%{http_code}" "https://localhost:8443/" 2>/dev/null | grep -q "200"; then
    log_info "Harmonies are ready."
    break
  else
    log_info "Retrying in 5 seconds..."
    sleep 5
  fi
done

log_info "Setting up connections"
./setup-connections.sh


log_success "Local development environment started successfully!"
log_info ""
log_info "Harmony instances:"
log_info "  - Estonia Harmony: https://localhost:8443"
log_info "  - Borduria Harmony: https://localhost:9443"
log_info ""
log_info "Shared services:"
log_info "  - RabbitMQ: localhost:5672"
log_info "  - PostgreSQL DB: localhost:9001"
log_info "  - PostgreSQL Meta DB: localhost:2345"

exit 0
