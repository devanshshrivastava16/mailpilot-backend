# =============================================
# Dockerfile for MailPilot Backend
# =============================================

# --- Stage 1: Build Stage ---
# Use a JDK image to build the application
FROM eclipse-temurin:21-jdk-jammy as builder

LABEL maintainer="mailpilot-dev"

WORKDIR /app

# Copy Maven wrapper and pom.xml to leverage Docker's layer caching
# This step is only re-run when pom.xml or the wrapper changes
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the application source code
COPY src ./src

# Build the application and create the executable JAR
RUN ./mvnw package -DskipTests -B

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