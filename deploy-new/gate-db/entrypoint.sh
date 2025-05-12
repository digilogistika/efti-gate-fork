#!/bin/sh

for schema in eftibo eftili eftiee; do
  echo 'Configuring gate with initial data for database: ' $schema
  sed "1iset search_path to $schema;" gate-config.sql | docker exec -i reference-gate-shared-db psql -U efti -d efti
done
