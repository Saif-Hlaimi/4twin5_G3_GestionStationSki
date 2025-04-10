pipeline {
    agent any
    environment {
        DOCKER_IMAGE = 'walidkhrouf/skier-app'
        DOCKER_TAG = '1.0.0'
         COMPOSE_FILE = 'docker-compose.yml'
    }
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
                    sh 'docker tag skier-app:latest ${DOCKER_IMAGE}:${DOCKER_TAG}'
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
                        sh 'docker push ${DOCKER_IMAGE}:${DOCKER_TAG}'
                    }
                }
            }
        }
        stage('Deploy') {
                  steps {
                      script {
                          sh '''#!/bin/bash
                              # 1. Vérification et nettoyage
                              if docker-compose -f $COMPOSE_FILE ps -q mysqldb >/dev/null 2>&1; then
                                  echo "Arrêt des services existants..."
                                  docker-compose -f $COMPOSE_FILE down --volumes local
                              fi

                              # 2. Pull de la dernière image
                              docker pull ${DOCKER_IMAGE}:${DOCKER_TAG}

                              # 3. Déploiement avec healthcheck intégré
                              docker-compose -f $COMPOSE_FILE up -d --build

                              # 4. Vérification santé
                              echo "Vérification de l'état des services..."
                              timeout 180s bash -c '
                                  while ! docker-compose -f $COMPOSE_FILE ps | grep "(healthy)"; do
                                      sleep 5
                                      echo "En attente des services healthy..."
                                      docker-compose -f $COMPOSE_FILE ps
                                  done
                              '

                              # 5. Affichage final
                              echo "=== Déploiement réussi ==="
                              docker-compose -f $COMPOSE_FILE ps
                              curl -s http://localhost:8089/api/actuator/health | jq .
                          '''
                      }
                  }
              }
          }
          post {
              failure {
                  script {
                      sh '''
                          echo "=== DÉBOGAGE ÉCHEC ==="
                          docker-compose -f $COMPOSE_FILE logs --tail=50
                          docker ps -a
                          netstat -tulnp | grep -E '3306|8089'
                      '''
                  }
              }
              always {
                  cleanWs()
                  script {
                      emailext (
                          subject: "${currentBuild.result}: Job ${env.JOB_NAME}",
                          body: """
                              Détails du déploiement:
                              - Image: ${DOCKER_IMAGE}:${DOCKER_TAG}
                              - MySQL: ${sh(returnStdout: true, script: 'docker inspect -f "{{.State.Health.Status}}" timesheet-mysql')}
                              - App: ${sh(returnStdout: true, script: 'curl -s http://localhost:8089/api/actuator/health | jq .status')}
                              Logs: ${env.BUILD_URL}console
                          """,
                          to: 'devops@example.com'
                      )
                  }
              }
    }
}