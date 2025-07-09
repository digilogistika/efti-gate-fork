FROM java-builder:latest AS java-builder

FROM openjdk:21-jdk-slim

WORKDIR /usr/src/myapp

COPY --from=java-builder /build/implementation/gate/target/gate-*.jar ./efti-gate.jar
COPY ./deploy/gate ./

RUN apt-get update && apt-get install -y curl && apt-get clean && rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "-Dspring.config.location=/usr/src/myapp/", "-Dspring.profiles.active=DEFAULT", "efti-gate.jar"]
