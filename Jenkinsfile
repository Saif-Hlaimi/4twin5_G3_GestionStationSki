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

                    // Start services with logging
                    sh 'docker compose up -d'

                    // Enhanced health check with proper curl command and logging
                    sh '''
                        echo "Waiting for services to start..."
                        echo "Checking MySQL..."
                        docker compose logs mysqldb --tail=50

                        echo "Checking application..."
                        for i in {1..30}; do
                            if docker compose ps | grep app-skier | grep -q '(healthy)'; then
                                echo "Application is healthy!"
                                exit 0
                            fi
                            echo "Waiting... attempt \$i/30"
                            docker compose logs app-skier --tail=20
                            sleep 10
                        done

                        echo "Application failed to become healthy"
                        docker compose logs app-skier
                        exit 1
                    '''
                }
            }
        }
    }
}
