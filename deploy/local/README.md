# Local Deploy

The gate requires some services to be available for it to work. For local development these services can be made
available by running the `start.sh` script. The script has to be run as `sudo`, because it will add required hosts to `/etc/hosts` file.

## Starting services

```bash
sudo ./start.sh
```

## Stoping services

```bash
./stop.sh
```

## Starting local development on IDE

To run gate use `EftiGateApplication` with EE or BO profile.

To run platform use `PlatformGateSimulatorApplication` with ACME or ESTPLAT profile.

## Setting up indicators

After you have started both gates locally (EE and BO), then you can use the `setup-indicators.sh` script to add indicators to database.

Indicators are needed for Identifiers requests. Without the indicators the Identifiers requests will fail, but UIL ones will still work.

```bash
./setup-indicators.sh
```

## Logs issue

[Sometimes the gate won't start locally (outside of docker) because of a log file directory access issue.

This happens because the `/var/log/javapp` directory is not created by default. This can be created manually by running:

```bash
sudo mkdir /var/log/javapp
```

Also the permissions for the folder need to be changed to that of the user running the gate. This can be done by
running:

```bash
sudo chown -R $USER /var/log/javapp
```