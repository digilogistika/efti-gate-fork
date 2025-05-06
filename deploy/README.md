# Deploy

## Starting services

Syntax (without platform):
```
docker compose -p <party_name> up -d
```

Syntax (with platform):
```
docker compose -p <party_name> --profile platform up -d
```

Example:
```
docker compose -p estonia --profile platform up -d
```

## Exporting certs

Syntax:
```
./export.sh <party_name> <directory_to_export>
```

Example:
```
./export.sh estonia ./estonia
```

## Connecting gates

Syntax:
```
./connect.sh <project_name> <peer_url> <peer_name> <peer_truststore_cert_path> <peer_tls_cert_path>
```

Example:
```
./connect.sh estonia https://borduria-harmony-gate-1:8443 borduria ./borduria/borduria_truststore_cert.pem ./borduria/borduria_tls_cert.pem
```
