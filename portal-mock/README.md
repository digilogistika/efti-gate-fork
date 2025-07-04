# eFTI CA APP mock

This project is the mock application for the Competent Authority application.
Using this mock, you can query for identifiers and datasets, also send out follow-up messages for datasets received.
It is meant to be used with the eFTI Authority Backend Application.
Upon first run, you must register a user to the system by going to the admin panel.

When typing on the keyboard "a", "d", "m", "i", "n" in sequence, admin menu option will be displayed in the navbar.
Registering a user is done by supplying the master API key aka the Super API key to the registration form. For local development the key is `SuperSecretSecret123`.

## Prerequisites for local development

- The deploy/local docker project must be running.
- The eFTI Authority Backend Application must be running.
- The Borduria Gate must be running.
- ACME platform should also be running

For easier configuration, this mock use the borduria gate as it's associated gate, so make sure it is up and running.
