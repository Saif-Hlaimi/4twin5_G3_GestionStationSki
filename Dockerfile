# Étape 1 : Construction de l’application avec Maven
FROM maven:3.8.7-openjdk-17 AS builder
WORKDIR /app
# Copier le fichier pom.xml et le répertoire source
COPY pom.xml .
COPY src ./src
# Compilation et packaging de l’application (sans lancer les tests)
RUN mvn clean package -DskipTests

# Étape 2 : Création de l'image finale pour l'exécution
FROM openjdk:17-jdk
WORKDIR /app
# Copier l’artefact depuis l'étape de build
COPY --from=builder /app/target/gestion-station-ski-1.0.jar gestion-station-ski.jar
EXPOSE 9000
CMD ["java", "-jar", "gestion-station-ski.jar"]
