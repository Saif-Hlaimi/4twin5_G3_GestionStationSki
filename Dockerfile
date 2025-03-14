# Utiliser une image Java 17
FROM openjdk:17-jdk-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier l’application compilée
COPY target/*.jar app.jar

# Exposer le port
EXPOSE 8089

# Lancer l’application
ENTRYPOINT ["java", "-jar", "app.jar"]