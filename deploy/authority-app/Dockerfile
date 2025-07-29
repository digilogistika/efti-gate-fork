FROM java-builder:latest AS java-builder

FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y --no-install-recommends fontconfig fonts-dejavu && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src/myapp

COPY --from=java-builder /build/implementation/authority-app/target/authority-app-*.jar ./authority-app.jar
COPY ./deploy/authority-app ./

CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "-Dspring.config.location=file:application.yml", "-Dspring.profiles.active=DEFAULT", "authority-app.jar", "--port=8090"]
