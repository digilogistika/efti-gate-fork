# Required side services

All the required side services that are needed to run the gate locally.

Side services are started using docker.

If you want add a profile to the gate (for example Estonia) then you need to add or change files here also.

NB! the `ensure-requirements.sh` script is used to download the required maven packages. If gate does not start then
running thsi script can help