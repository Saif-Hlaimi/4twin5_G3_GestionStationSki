FROM eclipse-temurin:11-jdk-alpine
EXPOSE 9000
ENV NEXUS_URL="http://localhost:8081/repository/maven-releases/tn/esprit/spring/gestion-station-ski/1.0/gestion-station-ski-1.0.jar"
RUN wget -O /gestion-station-ski-1.0.jar "$NEXUS_URL"


ENTRYPOINT ["java","-jar","/gestion-station-ski-1.0.jar"]