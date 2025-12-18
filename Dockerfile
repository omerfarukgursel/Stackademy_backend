# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install FFmpeg for video processing
RUN apk add --no-cache ffmpeg

# Create a non-root user for security
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup

# Copy the JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Note: In Cloud Run, GCP Default Credentials are automatically available
# gcp-key.json is only needed for local development

# Change ownership
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

CMD java -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-8080} -jar app.jar
