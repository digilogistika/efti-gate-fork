#!/bin/sh

wait_for_gate() {
    gate_port=$1
    echo "Waiting for the gate to be up and running at $gate_port..."
    until $(curl --output /dev/null --silent --head --fail http://localhost:$gate_port/actuator/health); do
        echo "Waiting for the gate to be up and running at $gate_port..."
        sleep 5
    done
}

is_service_running() {
    local service_name=$1
    
    if docker compose ps --services --filter "status=running" | grep -q "^$service_name$"; then
        return 0
    else
        return 1
    fi
}

if is_service_running "efti-gate-borduria"; then
wait_for_gate 8880
elif is_service_running "efti-gate-listenbourg"; then
wait_for_gate 8881
elif is_service_running "efti-gate-estonia"; then
wait_for_gate 8882
fi


for schema in eftibo eftili eftiee; do
  echo 'Configuring gate with initial data for database: ' $schema
  sed "1iset search_path to $schema;" gate-config.sql | docker exec -i reference-gate-shared-db psql -U efti -d efti
done
