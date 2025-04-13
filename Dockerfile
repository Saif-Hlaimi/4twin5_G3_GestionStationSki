FROM eclipse-temurin:11-jdk-alpine
EXPOSE 9000
ENV NEXUS_URL="http://localhost:8081/repository/maven-snapshots/tn/esprit/spring/gestion-station-ski/1.0-SNAPSHOT/gestion-station-ski-1.0-SNAPSHOT.jar"
RUN wget -O /gestion-station-ski-1.0-SNAPSHOT.jar "$NEXUS_URL"
ENTRYPOINT ["java", "-jar", "/gestion-station-ski-1.0-SNAPSHOT.jar"]