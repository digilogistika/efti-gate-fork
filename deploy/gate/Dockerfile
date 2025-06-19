FROM node:lts-slim AS angular-builder

ARG OWNER_VALUE

WORKDIR /app/frontend
RUN npm install -g @angular/cli

COPY ./gate-admin-ui/package*.json ./
RUN npm ci

COPY ./gate-admin-ui ./

RUN sed -i "s/gateId: \"OWNER\"/gateId: \"${OWNER_VALUE}\"/" src/environments/environment.ts

RUN ng build --configuration=production


FROM maven:3.8-openjdk-17 AS java-builder

WORKDIR /build

COPY ./implementation ./implementation
COPY ./schema ./schema
COPY --from=angular-builder /app/frontend/dist/browser/ ./implementation/gate/src/main/resources/static/

RUN mvn -B clean -f ./implementation/pom.xml -DskipTests
RUN mvn -B package -f ./implementation/pom.xml -DskipTests

FROM openjdk:21-jdk-slim

WORKDIR /usr/src/myapp

COPY --from=java-builder /build/implementation/gate/target/gate-*.jar ./efti-gate.jar
COPY ./deploy/gate ./

RUN apt-get update && \
    apt-get install -y curl && \
    # Clean up to reduce image size
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "-Dspring.config.location=/usr/src/myapp/", "-Dspring.profiles.active=DEFAULT", "efti-gate.jar"]
