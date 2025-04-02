FROM eclipse-temurin:11-jdk-alpine
EXPOSE 8089
COPY target/gestion-station-ski-1.0.jar.original gestion-station-ski-1.0.jar
ENTRYPOINT ["java","-jar","/gestion-station-ski-1.0.jar"]