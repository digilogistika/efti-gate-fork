# GATE UI BUILDING
FROM node:lts-slim AS gate-ui-builder

ARG OWNER_VALUE

WORKDIR /app/frontend
RUN npm install -g @angular/cli

COPY ./gate-admin-ui/package*.json ./
RUN npm ci

COPY ./gate-admin-ui ./

RUN sed -i "s/gateId: \"OWNER\"/gateId: \"${OWNER_VALUE}\"/" src/environments/environment.ts

RUN ng build --configuration=production

# AUTHORITY UI BUILDING
FROM node:22-alpine AS authority-ui-building

WORKDIR /app/frontend

COPY ./portal-mock/package*.json ./

RUN npm ci

COPY ./portal-mock/ .

RUN npm run build

# JAVA BUILDING
FROM maven:3.8-openjdk-17 AS java-builder

WORKDIR /build

COPY ./implementation/pom.xml ./implementation/
COPY ./implementation/authority-app/pom.xml ./implementation/authority-app/
COPY ./implementation/commons/pom.xml ./implementation/commons/
COPY ./implementation/edelivery-ap-connector/pom.xml ./implementation/edelivery-ap-connector/
COPY ./implementation/efti-logger/pom.xml ./implementation/efti-logger/
COPY ./implementation/efti-ws-plugin/pom.xml ./implementation/efti-ws-plugin/
COPY ./implementation/gate/pom.xml ./implementation/gate/
COPY ./implementation/platform-gate-simulator/pom.xml ./implementation/platform-gate-simulator/
COPY ./implementation/registry-of-identifiers/pom.xml ./implementation/registry-of-identifiers/

RUN mvn -B dependency:go-offline -f ./implementation/pom.xml

COPY ./implementation ./implementation
COPY ./schema ./schema

COPY --from=gate-ui-builder /app/frontend/dist/browser/ ./implementation/gate/src/main/resources/static/
COPY --from=authority-ui-building /app/frontend/dist/portal-mock/browser ./implementation/authority-app/src/main/resources/static/

RUN mvn -B clean package -f ./implementation/pom.xml -DskipTests
