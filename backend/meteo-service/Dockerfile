FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the pre-built JAR file
COPY target/*.jar app.jar

# Expose meteo service port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]