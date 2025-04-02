FROM eclipse-temurin:11-jdk-alpine
EXPOSE 8089
ENV NEXUS_URL="http://192.168.33.10:8081/repository/maven-releases/tn/esprit/spring/kaddem/0.0.1/kaddem-0.0.1.jar"

COPY target/gestion-station-ski-1.0.jar.original gestion-station-ski-1.0.jar
ENTRYPOINT ["java","-jar","/gestion-station-ski-1.0.jar"]


FROM eclipse-temurin:11-jdk-alpine

# Exposer le port de l'application
EXPOSE 8082

# Définir l'URL du JAR sur Nexus
ENV NEXUS_URL="http://localhost:8081/repository/maven-releases/tn/esprit/spring/kaddem/0.0.1/kaddem-0.0.1.jar"

# Télécharger le fichier JAR depuis Nexus
RUN wget -O /kaddem-0.0.1.jar "$NEXUS_URL"

# Lancer l'application
ENTRYPOINT ["java", "-jar", "/kaddem-0.0.1.jar"]