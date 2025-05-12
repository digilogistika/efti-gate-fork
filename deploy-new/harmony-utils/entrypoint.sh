#!/bin/sh
set -e

echo "Running initial setup script..."
python main.py initial-setup

echo "Initial setup finished. Container will remain running."

exec "$@"
