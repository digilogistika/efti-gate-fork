# Local Deploy

First add required hosts to your `/etc/hosts` file. Sudo, is needed.
Here is a script for that:

```bash
sudo ./add_hosts.sh
```

The gate requires some services to be available for it to work. For local development these services can be made
available by running the `start.sh` script.

## Starting services

```bash
./start.sh
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

## Frequent issues

### Logs issue

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

### Docker host access issue

In certain machines and with certain docker setup it is impossible to access the host machine from docker container. Access to host machine is required by the harmony container to push incoming messages to the gate application. Using docker desktop instead of docker engine is reccomended in these cases as it allows the connection from container to host machine. 
