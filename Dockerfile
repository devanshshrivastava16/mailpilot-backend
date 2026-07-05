# =============================================
# Dockerfile for MailPilot Backend
# =============================================

# --- Stage 1: Build Stage ---
# Use a Maven image to build the application
FROM maven:3.9.6-eclipse-temurin-21 as builder

LABEL maintainer="mailpilot-dev"

WORKDIR /app

# Copy pom.xml to leverage Docker's layer caching
COPY pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy the rest of the application source code
COPY src ./src

# Build the application and create the executable JAR
RUN mvn package -DskipTests -B

# --- Stage 2: Runtime Stage ---
# Use a smaller JRE image for the final container
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the executable JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]