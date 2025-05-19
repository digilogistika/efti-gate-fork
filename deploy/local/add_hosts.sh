#!/bin/bash

set -e

if [ "$EUID" -ne 0 ]; then
    log_error "Please run this script with sudo to allow host file modification"
    exit 1
fi

log_info "Updating /etc/hosts file for local development..."

ESTONIA_EXISTS=$(grep "estonia-harmony-1" /etc/hosts || echo "")
BORDURIA_EXISTS=$(grep "borduria-harmony-1" /etc/hosts || echo "")

if [ -z "$ESTONIA_EXISTS" ]; then
    echo "127.0.0.1 estonia-harmony-1" >> /etc/hosts
    log_info "Added estonia-harmony-1 to /etc/hosts"
fi

if [ -z "$BORDURIA_EXISTS" ]; then
    echo "127.0.0.1 borduria-harmony-1" >> /etc/hosts
    log_info "Added borduria-harmony-1 to /etc/hosts"
fi
