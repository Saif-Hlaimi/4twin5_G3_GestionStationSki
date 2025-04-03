FROM eclipse-temurin:11-jdk-alpine
EXPOSE 8089

# Add wait-for-it script for better service dependency handling
RUN apk add --no-cache bash curl && \
    curl -o /wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh && \
    chmod +x /wait-for-it.sh

# Download application
ENV NEXUS_URL="http://192.168.33.10:8081/repository/maven-releases/tn/esprit/spring/gestion-station-ski/1.0/gestion-station-ski-1.0.jar"
RUN wget -O /app.jar "$NEXUS_URL"

# Health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8089/api/actuator/health || exit 1

# Entrypoint with wait-for-it for MySQL
ENTRYPOINT ["/wait-for-it.sh", "mysqldb:3306", "--timeout=120", "--", "java", "-jar", "/app.jar"]