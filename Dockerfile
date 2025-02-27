
FROM maven:3.8.7-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk
WORKDIR /app
COPY --from=builder /app/target/gestion-station-ski-1.0.jar gestion-station-ski.jar
EXPOSE 9000
CMD ["java", "-jar", "gestion-station-ski.jar"]
