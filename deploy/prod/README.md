# Production Deployment

## Overview

For production deployment, two servers are used to host the gates. Both servers run a Harmony access point paired with
the related gate service.

In the `server-x` folder, there are two subfolders that are used to start these services.

Currently, both servers use the reference implementation, and the following imaginary countries are assigned:

| Server   | Country     |
|----------|-------------|
| server-1 | borduria    |
| server-2 | listenbourg |

## Server Setup Instructions

The setup process is identical for both servers. **Note:** Due to various permission issues, this reference production
deployment assumes that all actions are executed as the system `root` user.

### 1. Acquire a VPS

- **Operating System:** Preferably, run the latest LTS version of Ubuntu.
- **Hardware Requirements:** At least 8GB RAM and 4 CPU cores.
- **Networking:** The server must have a public IP address.

### 2. Install Required Packages

Update and upgrade the system packages:

```bash
sudo apt update && sudo apt upgrade -y
```

#### Docker and Docker Compose

Install Docker by following the official instructions (which include setting up Docker Compose):  
[Docker Engine Install - Ubuntu](https://docs.docker.com/engine/install/ubuntu/)

#### JDK 17 Setup

Install JDK 17:

```bash
sudo apt install openjdk-17-jdk -y
```

Set OpenJDK 17 as the default JDK:

```bash
# List installed JDKs
update-java-alternatives --list  

# Set the default JDK (replace path with the actual path to JDK 17)
sudo update-alternatives --set path/to/jdk-17/
```

#### Install Maven and Nginx

```bash
sudo apt install maven nginx -y
```

### 3. Setup Harmony Access Point

Before starting Harmony, change the owner of the `ws-plugin.properties` file to `999` in the Harmony folder. This is
necessary for Harmony to read the file contents.

```bash
# In the harmony folder
sudo chown 999:999 ws-plugin.properties
```

Start the Harmony containers:

```bash
docker compose up -d
```

#### Harmony Web UI Configuration

The Harmony web UI requires several setup steps to ensure proper connectivity with its gate and other access points:

1. **Login:**

    - **User:** `harmony`
    - **Password:** `Secret`

2. **Setup PMode File:**

    - Navigate to `PMode` > `Current` > `Upload` and upload the `pmode.xml` file located in the Harmony folder.
    - **Note:** Ensure the correct PMode file from the appropriate folder is selected. This configuration assumes that
      the server hosting Harmony is available at:
        - `harmony.borduria.efti.pikker.dev`
        - `harmony.listenbourg.efti.pikker.dev`

3. **Configure Plugin Users:**

    - Go to `Plugin Users` and create a new user with the following credentials:
        - **User Name:** `<country>_service_account`
        - **Role:** `ROLE_ADMIN`
        - **Password:** `Azerty59*1234567`
    - **Note:** Don’t forget to click **Save** after creating the user.

4. **Setup the Truststore:**

    - Initially, the truststore is empty. To populate it, add your keystore certificates.
    - Go to `Certificates` > `Keystore` and click **Download** to retrieve the keystore in p12 format.
    - Extract the certificate from the keystore using a command-line tool or a GUI tool such
      as [Keystore Explorer](https://keystore-explorer.org/). The default keystore password is `changeit`.
    - When using Keystore Explorer, right click on the keystore and select **Export** > **Export Certificate Chain**.
      Use the following export options:
        - **Format:** `X.509`
        - **PEM:** Check the box.
    - Upload the extracted certificate (in PEM format) by navigating to `Certificates` > `Truststore` and clicking **Add
      Certificate**.

5. **Setup Truststore on the Other Harmony AP:**

    - Repeat the certificate extraction process for the second Harmony access point and upload the certificate to its
      truststore.

6. **TLS Configuration:**

    - Under `Certificates` > `TLS`, extract the certificates following the same process as above. Ensure that these
      certificates are installed on both access points.

7. **Test Connectivity:**

    - In the **Connection Monitoring** section, you can view the connected access points. Test connectivity by clicking
      the Send button (depicted as a paper airplane). A green check mark indicates a successful connection.

### 4. Setup the Gate

Within the `gate` subfolder under the server directory, follow these steps:

1. **Create the Docker Network:**

   ```bash
   docker network create efti-network
   ```

2. **Deploy the Gate:**

    - Ensure Java, Maven, and Docker are installed and properly configured.
    - Deploy the gate by executing the `deploy.sh` script:

      ```bash
      sh deploy.sh
      ```

### 5. Setup Nginx

Nginx is used to provide Keycloak authentication for the gate. All endpoints on the gate are protected, and Keycloak in
production requires HTTPS. To address this, Nginx is used as a reverse proxy.

Additionally, to ensure the correct Keycloak realm is used, update the system’s hosts file.

1. **Edit the Hosts File** (`/etc/hosts`, requires sudo access):

   ```plaintext
   127.0.0.1 host.docker.internal
   127.0.0.1 auth.gate.<country>.eu
   ```

2. **Copy and Configure Nginx Default Config for Keycloak:**

    - In `/etc/nginx/sites-enabled`, copy the `default` config file and rename it to
      `keycloak.<country>.efti.pikker.dev`. Ensure that your domain's DNS records point to this URL.

3. **Edit the Config File:**

   Replace `<country>` with the appropriate country name:

   ```nginx
   server {
           listen 80;
           listen [::]:80;

           root /var/www/html;
           index index.html index.htm index.nginx-debian.html;

           server_name keycloak.<country>.efti.pikker.dev;

           location / {
                   proxy_pass http://auth.gate.<country>.eu:8080;
           }
   }
   ```

4. **Reload Nginx:**

   ```bash
   service nginx reload
   ```

### 6. Test Connectivity

After completing all the setup steps, test the connectivity between the gates using the provided Postman collection in
the `utils` folder of the project.

- First, authenticate with Keycloak by running the `BO` > `Gate` > `Authenticate BO` request.
- Then, call the `BO` > `Gate` > `Gate to Gate UIL` request. This request attempts to contact the Listenbourg gate for
  identifiers it does not have, and the expected result is an error response.