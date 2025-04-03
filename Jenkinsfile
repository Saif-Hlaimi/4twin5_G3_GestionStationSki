pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test -Dspring.profiles.active=test'
            }
        }
        stage('Sonar Analysis') {
            steps {
                withSonarQubeEnv('sq1') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        stage('Nexus') {
            steps {
                sh 'mvn clean deploy -Dmaven.test.skip=true'
            }
        }
        stage('Docker Build') {
                  steps {
                      script {
                          // Verify JAR file exists
                          sh 'ls -la target/*.jar'

                          // Update Dockerfile base image
                          sh "sed -i 's|openjdk:11-jdk-alpine|eclipse-temurin:11-jdk-alpine|g' Dockerfile"

                          // Build Docker image
                          sh 'docker build --network=host -t skier-app:latest .'

                          // Tag the image for DockerHub
                          sh 'docker tag skier-app:latest walidkhrouf/skier-app:1.0.0'
                      }
                  }
              }
              stage('Push to DockerHub') {
                  steps {
                      script {
                          // Use Jenkins credentials to login to DockerHub
                          withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                         usernameVariable: 'DOCKER_HUB_USER',
                                                         passwordVariable: 'DOCKER_HUB_PWD')]) {
                              sh 'echo $DOCKER_HUB_PWD | docker login -u $DOCKER_HUB_USER --password-stdin'
                              sh 'docker push walidkhrouf/skier-app:1.0.0'
                          }
                      }
                  }
              }
          stage('Docker Compose Deploy') {
              steps {
                  script {
                      // Clean up any existing containers
                      sh 'docker compose down -v || true'

                      // Start services with longer timeout
                      sh 'docker compose up -d --wait --timeout 300'

                      // Enhanced health check with proper curl command
                      sh '''
                          echo "Waiting for application to start..."
                          for i in {1..30}; do
                              if curl -s -f http://localhost:8089/api/actuator/health | grep -q 'UP'; then
                                  echo "Application is up!"
                                  exit 0
                              fi
                              sleep 10
                              echo "Waiting... attempt \$i/30"
                          done
                          echo "Application failed to start after 5 minutes"
                          docker compose logs app-skier
                          exit 1
                      '''
                  }
              }
          }
    }
}
