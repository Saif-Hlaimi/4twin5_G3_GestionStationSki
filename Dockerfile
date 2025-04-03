FROM eclipse-temurin:11-jdk-alpine
EXPOSE 8089
ENV NEXUS_URL="http://192.168.33.10:8081/repository/maven-releases/tn/esprit/spring/gestion-station-ski/1.0/gestion-station-ski-1.0.jar"
RUN wget -O /gestion-station-ski-1.0.jar "$NEXUS_URL"


ENTRYPOINT ["java","-jar","/gestion-station-ski-1.0.jar"]