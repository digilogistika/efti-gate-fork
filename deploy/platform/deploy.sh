#!/bin/sh

# Stop on fail
set -e
cd $(dirname $0)

projectPomFile=../../../../implementation/pom.xml

echo "Cleaning up..."
mvn -B clean --file $projectPomFile -DskipTests

echo "Building..."
mvn -B package --file $projectPomFile -DskipTests

echo "Copying apps..."
cp -rf ../../../../implementation/platform-gate-simulator/target/platform-gate-simulator-*.jar ./platform/platform-simulator.jar

echo "Starting up docker compose"
docker compose up -d

# This goes through the schemas in the common database and configures all of them with the same data
for schema in eftibo eftili eftisy; do
  echo 'Configuring gate with initial data for database: ' $schema
  # wait for postgres to be up
  sleep 20

  sed "1iset search_path to $schema;" ./gate-db/gate-config.sql | docker exec -i reference-gate-shared-db psql -U efti -d efti
done
$SHELL
