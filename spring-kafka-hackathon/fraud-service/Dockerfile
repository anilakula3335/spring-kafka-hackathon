# Multi-stage: build then run with slim JRE
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY event-contracts/pom.xml event-contracts/
COPY config-server/pom.xml config-server/
COPY order-service/pom.xml order-service/
COPY inventory-service/pom.xml inventory-service/
COPY fraud-service/pom.xml fraud-service/
RUN mvn -B -pl fraud-service -am dependency:go-offline -DskipTests || true
COPY event-contracts/src event-contracts/src
COPY fraud-service/src fraud-service/src
RUN mvn -B -pl fraud-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN adduser -D -s /bin/sh app
WORKDIR /app
COPY --from=builder /build/fraud-service/target/fraud-service-*.jar app.jar
USER app
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
