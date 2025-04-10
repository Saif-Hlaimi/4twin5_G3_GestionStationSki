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
                          sh 'ls -la target/*.jar'
                          sh "sed -i 's|openjdk:11-jdk-alpine|eclipse-temurin:11-jdk-alpine|g' Dockerfile"
                          sh 'docker build --network=host -t skier-app:latest .'
                          sh 'docker tag skier-app:latest walidkhrouf/skier-app:1.0.0'
                      }
                  }
              }
              stage('Push to DockerHub') {
                  steps {
                      script {
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
                               sh 'docker compose down || true'
                               sh 'docker compose up -d'
                               sh '''
                                   echo "Waiting for application to start..."
                                   for i in {1..10}; do
                                       if curl -s http://localhost:8089/api/actuator/health | grep -q 'UP'; then
                                           echo "Application is up!"
                                           exit 0
                                       fi
                                       sleep 10
                                       echo "Waiting... attempt $i/10"
                                   done
                                   echo "Application failed to start"
                                   exit 1
                               '''
                           }
                       }
                   }
    }
}


