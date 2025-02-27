FROM openjdk:8-jdk
COPY . /app
WORKDIR /app
RUN mvn clean install
CMD ["java", "-jar", "target/gestion-station-ski-1.0.jar"]