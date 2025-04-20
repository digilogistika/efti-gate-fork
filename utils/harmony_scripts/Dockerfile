FROM python:3.13-alpine
LABEL authors="rig"
LABEL description="Dockerfile to automate harmony access point deployment and setup"
LABEL version="1.0"

# Set the working directory
WORKDIR /app

COPY . ./

RUN pip install -r requirements.txt

RUN apk update
RUN apk add openjdk17


ENTRYPOINT ["python3", "setup.py"]