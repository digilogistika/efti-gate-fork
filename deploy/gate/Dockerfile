FROM maven:3.8-openjdk-17 AS builder

WORKDIR /build

COPY ./implementation ./implementation 
COPY ./schema ./schema

RUN mvn -B clean -f ./implementation/pom.xml -DskipTests
RUN mvn -B package -f ./implementation/pom.xml -DskipTests

FROM openjdk:21-jdk-slim

WORKDIR /usr/src/myapp

COPY --from=builder /build/implementation/gate/target/gate-*.jar ./efti-gate.jar
COPY ./deploy/gate ./

RUN apt-get update && \
    apt-get install -y curl && \
    # Clean up to reduce image size
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "-Dspring.config.location=/usr/src/myapp/", "-Dspring.profiles.active=${PROFILE}", "efti-gate.jar"]
