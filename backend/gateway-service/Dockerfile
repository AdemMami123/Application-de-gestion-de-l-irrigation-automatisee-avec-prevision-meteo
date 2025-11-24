FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the pre-built JAR file
COPY target/gateway-service-1.0.0.jar app.jar

# Expose gateway port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
