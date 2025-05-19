#!/bin/sh

for schema in eftibo eftiee; do
  echo 'Configuring gate with initial data for database: ' $schema
  sed "1iset search_path to $schema;" ./gate-db/gate-config.sql | docker exec -i efti-psql psql -U efti -d efti
done
