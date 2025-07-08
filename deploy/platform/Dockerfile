FROM java-builder:latest AS java-builder

FROM openjdk:21-jdk-slim

WORKDIR /usr/src/myapp

COPY --from=java-builder /build/implementation/platform-gate-simulator/target/platform-gate-simulator-*.jar ./platform-simulator.jar
COPY ./deploy/platform ./

CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "-Dspring.config.location=file:application.yml", "-Dspring.profiles.active=DEFAULT", "platform-simulator.jar", "--port=8070"]
