FROM maven:3.8-openjdk-17 AS builder

WORKDIR /build

COPY ../../../../implementation/ .

RUN mvn -B clean -f pom.xml -DskipTests
RUN mvn -B package -f pom.xml -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /usr/src/myapp

COPY --from=builder /build/platform-gate-simulator/target/platform-gate-simulator-*.jar ./platform-simulator.jar

CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "-Dspring.config.location=/usr/src/myapp/", "-Dspring.profiles.active=${PROFILE}", "efti-gate.jar"]
