FROM maven:3.8-openjdk-17 AS builder

WORKDIR /build

COPY ./implementation ./implementation 
COPY ./schema ./schema

RUN mvn -B clean -f ./implementation/pom.xml -DskipTests
RUN mvn -B package -f ./implementation/pom.xml -DskipTests

FROM openjdk:21-jdk-slim

WORKDIR /usr/src/myapp

COPY --from=builder /build/implementation/platform-gate-simulator/target/platform-gate-simulator-*.jar ./platform-simulator.jar
COPY ./deploy/platform ./

CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "-Dspring.config.location=file:application.yml", "-Dspring.profiles.active=DEFAULT", "platform-simulator.jar", "--port=8070"]
