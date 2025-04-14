#!/bin/bash

# Stop and remove all containers, networks, volumes, and orphans
docker compose down -v --remove-orphans

# Ensure all containers with conflicting names are removed
docker ps -a --filter "name=efti-" --format "{{.Names}}" | xargs -r docker rm -f

# Start the containers
docker compose up -d

