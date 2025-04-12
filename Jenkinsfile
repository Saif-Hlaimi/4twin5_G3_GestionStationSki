pipeline {
    agent any
    environment {
        DOCKER_IMAGE = 'walidkhrouf/skier-app'
        DOCKER_TAG = '1.0.0'
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
                    // No need to list target/*.jar since we're fetching from Nexus
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
                     set -e

                     # Stop and remove any containers using ports 3306 and 8089
                     echo "Checking for containers on port 3306..."
                     CONTAINERS_3306=$(docker ps -q --filter "publish=3306")
                     if [ -n "$CONTAINERS_3306" ]; then
                         echo "Port 3306 in use, stopping containers: $CONTAINERS_3306"
                         docker stop $CONTAINERS_3306 || true
                         docker rm -f $CONTAINERS_3306 || true
                     fi

                     echo "Checking for containers on port 8089..."
                     CONTAINERS_8089=$(docker ps -q --filter "publish=8089")
                     if [ -n "$CONTAINERS_8089" ]; then
                         echo "Port 8089 in use, stopping containers: $CONTAINERS_8089"
                         docker stop $CONTAINERS_8089 || true
                         docker rm -f $CONTAINERS_8089 || true
                     fi

                     # Additional cleanup for known container names
                     docker stop walidkhrouf-app timesheet-mysql walidkhrouf-4twin5-g3-mysqldb-1 walidkhrouf-4twin5-g3-app-skier-1 || true
                     docker rm -f walidkhrouf-app timesheet-mysql walidkhrouf-4twin5-g3-mysqldb-1 walidkhrouf-4twin5-g3-app-skier-1 || true

                     # Clean up any stopped containers still binding ports
                     docker rm -f $(docker ps -a -q --filter "publish=3306") || true
                     docker rm -f $(docker ps -a -q --filter "publish=8089") || true

                     # Clean up and recreate network
                     docker network rm timesheet-net || true
                     docker network create timesheet-net || true

                     # Run MySQL container
                     docker run -d --name timesheet-mysql \\
                         --network timesheet-net \\
                         -e MYSQL_ALLOW_EMPTY_PASSWORD=yes \\
                         -e MYSQL_DATABASE=stationSki \\
                         -p 3306:3306 \\
                         -v mysql_data:/var/lib/mysql \\
                         --health-cmd="mysqladmin ping -h localhost" \\
                         --health-interval=10s \\
                         --health-timeout=5s \\
                         --health-retries=5 \\
                         mysql:5.7

                     # Wait for MySQL to be healthy
                     timeout 180s bash -c 'until docker inspect timesheet-mysql --format "{{.State.Health.Status}}" | grep "healthy"; do sleep 5; echo "Waiting for MySQL..."; done'

                     # Run application container
                     docker run -d --name walidkhrouf-app \\
                         --network timesheet-net \\
                         -p 8089:8089 \\
                         -e SPRING_PROFILES_ACTIVE=docker \\
                         -e SPRING_DATASOURCE_URL=jdbc:mysql://timesheet-mysql:3306/stationSki?createDatabaseIfNotExist=true \\
                         -e SPRING_DATASOURCE_USERNAME=root \\
                         -e SPRING_DATASOURCE_PASSWORD= \\
                         ${DOCKER_IMAGE}:${DOCKER_TAG}
                 '''
             }
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
                    to: 'walid.khrouf@esprit.tn'
                )
            }
        }
    }
}