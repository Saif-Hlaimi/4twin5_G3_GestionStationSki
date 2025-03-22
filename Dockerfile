FROM openjdk:17-jdk-alpine

LABEL authors="Feryal Yahyaoui"

EXPOSE 8089

ADD target/gestion-station-ski-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]