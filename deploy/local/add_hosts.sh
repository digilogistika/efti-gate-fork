#!/bin/bash

set -e

if [ "$EUID" -ne 0 ]; then
    exit 1
fi

ESTONIA_EXISTS=$(grep "estonia-harmony-1" /etc/hosts || echo "")
BORDURIA_EXISTS=$(grep "borduria-harmony-1" /etc/hosts || echo "")

if [ -z "$ESTONIA_EXISTS" ]; then
    echo "127.0.0.1 estonia-harmony-1" >> /etc/hosts
fi

if [ -z "$BORDURIA_EXISTS" ]; then
    echo "127.0.0.1 borduria-harmony-1" >> /etc/hosts
fi
