FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install git (required for Git backend)
RUN apk add --no-cache git

# Copy the pre-built JAR file
COPY target/*.jar app.jar

# Expose Config Server port
EXPOSE 8888

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8888/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
