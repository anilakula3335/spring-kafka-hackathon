# Multi-stage: build then run with slim JRE (no Git credentials needed for public config repo)
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
# Copy root and all module poms so Maven reactor can parse the multi-module project
COPY pom.xml .
COPY event-contracts/pom.xml event-contracts/
COPY config-server/pom.xml config-server/
COPY order-service/pom.xml order-service/
COPY inventory-service/pom.xml inventory-service/
COPY fraud-service/pom.xml fraud-service/
RUN mvn -B -pl config-server dependency:go-offline -DskipTests || true
COPY config-server/src config-server/src
RUN mvn -B -pl config-server package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget && adduser -D -s /bin/sh app
WORKDIR /app
COPY --from=builder /build/config-server/target/config-server-*.jar app.jar
USER app
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
