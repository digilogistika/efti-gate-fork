FROM node:22-alpine AS angular-build

WORKDIR /app/frontend

COPY ./portal-mock/package*.json ./

RUN npm ci

COPY ./portal-mock/ .

RUN npm run build


FROM maven:3.8-openjdk-17 AS java-builder

WORKDIR /build

COPY ./implementation ./implementation 
COPY ./schema ./schema

COPY --from=angular-build /app/frontend/dist/ ./implementation/authority-app/src/main/resources/static/

RUN mvn -B clean -f ./implementation/pom.xml -DskipTests
RUN mvn -B package -f ./implementation/pom.xml -DskipTests


FROM openjdk:21-jdk-slim

WORKDIR /usr/src/myapp

COPY --from=java-builder /build/implementation/authority-app/target/authority-app-*.jar ./authority-app.jar
COPY ./deploy/authority-app ./

CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "-Dspring.config.location=file:application.yml", "-Dspring.profiles.active=DEFAULT", "authority-app.jar", "--port=8090"]
