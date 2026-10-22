# ============================================================
# Multi-stage Dockerfile for HRGenius (Spring Boot 3 + Java 17)
# ============================================================

# Stage 1: Build the JAR package with Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom and source code
COPY pom.xml .
COPY src ./src

# Build production JAR without running tests during image creation
RUN mvn clean package -DskipTests

# Stage 2: Lightweight runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy compiled JAR from build stage
COPY --from=build /app/target/hrgenius-1.0.0.jar app.jar

# Expose HTTP port (Render passes dynamic PORT environment variable)
EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
