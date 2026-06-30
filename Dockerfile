# Multi-stage build for SathishLogger
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create logs directory
RUN mkdir -p /app/logs

# Copy the built JAR
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user
RUN addgroup --system sathish && adduser --system --group sathish
RUN chown -R sathish:sathish /app
USER sathish

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT:-8080}/api/logs/health || exit 1

# Expose port
EXPOSE 8080

# Non-sensitive defaults only — pass DATABASE_PASSWORD at runtime via docker-compose
ENV SERVER_PORT=8080
ENV DATABASE_URL=jdbc:h2:mem:sathishlogger
ENV DATABASE_USERNAME=sa
ENV JPA_DDL_AUTO=create-drop
ENV LOG_LEVEL=INFO
ENV STORAGE_TYPE=database
ENV LOG_RETENTION_DAYS=30

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
