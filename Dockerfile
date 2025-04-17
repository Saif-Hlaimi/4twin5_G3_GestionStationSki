# Utilisez l'image de base OpenJDK avec Alpine
FROM openjdk:17-jdk-alpine

# Définir les informations de l'auteur
LABEL authors="Sboui Elaa"

# Exposer le port 8089
EXPOSE 8089

# Copier le fichier JAR dans le conteneur (assurez-vous que le fichier .jar existe dans le dossier target)
ADD target/gestion-station-ski-1.0-SNAPSHOT.jar app.jar

# Définir le point d'entrée pour démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]