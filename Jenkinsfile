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
           stage('Deploy') {
               steps {
                   script {
                       sh '''
                           docker stop walidkhrouf-app timesheet-mysql || true
                           docker rm walidkhrouf-app timesheet-mysql || true
                           docker network create timesheet-net || true

                           docker run -d --name timesheet-mysql \\
                               --network timesheet-net \\
                               -e MYSQL_ROOT_PASSWORD=${DB_PASSWORD} \\  // À définir comme secret
                               -e MYSQL_DATABASE=stationSki \\
                               -p 3306:3306 \\
                               -v mysql_data:/var/lib/mysql \\
                               mysql:5.7

                           timeout 300s bash -c 'while [[ "$(docker inspect -f '{{.State.Health.Status}}' timesheet-mysql)" != "healthy" ]]; do sleep 5; echo "Waiting for MySQL..."; done'

                           docker run -d --name walidkhrouf-app \\
                               --network timesheet-net \\
                               -p 8089:8089 \\
                               -e SPRING_DATASOURCE_URL=jdbc:mysql://timesheet-mysql:3306/stationSki \\
                               -e SPRING_DATASOURCE_USERNAME=root \\
                               -e SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD} \\  // À ajouter
                               ${DOCKER_IMAGE}:${DOCKER_TAG}
                       '''
                   }
               }
           }

                 post {
                     always {
                         cleanWs()
                         script {
                             emailext (
                                 subject: "${currentBuild.result}: Job ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                                 body: """
                                     Build: ${env.JOB_NAME} - #${env.BUILD_NUMBER}
                                     Status: ${currentBuild.result}
                                     URL: ${env.BUILD_URL}
                                     Image: ${DOCKER_IMAGE}:${DOCKER_TAG}
                                 """,
                                 to: 'devops@example.com'
                             )
                         }
                     }
                     }
    }
}