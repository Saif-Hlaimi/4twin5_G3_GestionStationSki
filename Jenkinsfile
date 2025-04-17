pipeline {
    agent any
    environment {
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64/"
        M2_HOME = "/usr/share/maven"
        PATH = "$M2_HOME/bin:$JAVA_HOME/bin:$PATH"
        DOCKER_IMAGE = 'elaasboui/gestion-station-ski'  // Nom de l'image Docker
        DOCKER_TAG = '1.0.0'  // Tag de l'image Docker
    }

    stages {
        stage('Hello Test') {
            steps {
                echo 'hello elaa'
            }
        }

        stage('Git Checkout') {
            steps {
                git branch: 'Elaasboui-4Twin5-G3',
                    url: 'https://github.com/Saif-Hlaimi/4twin5_G3_GestionStationSki.git',
                    credentialsId: 'jenkins-key'
            }
        }

        stage('Clean compile') {
            steps {
                script {
                    try {
                        sh 'mvn clean compile'
                    } catch (Exception e) {
                        error "Maven clean compile failed: ${e.getMessage()}"
                    }
                }
            }
        }

        stage('Test Projet') {
            steps {
                script {
                    try {
                        sh 'mvn -Dtest=SubscriptionServicesImplTest clean test'
                    } catch (Exception e) {
                        error "Test execution failed: ${e.getMessage()}"
                    }
                }
            }
        }

        stage('Sonar Analysis') {
            steps {
                script {
                    try {
                        withSonarQubeEnv('sq1') {
                            sh 'mvn sonar:sonar'
                        }
                    } catch (Exception e) {
                        error "SonarQube analysis failed: ${e.getMessage()}"
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_HUB_USER',
                        passwordVariable: 'DOCKER_HUB_PWD')]) {
                        sh 'echo $DOCKER_HUB_PWD | docker login -u $DOCKER_HUB_USER --password-stdin'
                        sh 'docker build --network=host -t ${DOCKER_IMAGE}:${DOCKER_TAG} .'
                    }
                }
            }
        }

        stage('Push Docker Image to DockerHub') {
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

                        # Clean up known containers by name
                        docker stop station-ski-mysql gestion-station-ski-app || true
                        docker rm -f station-ski-mysql gestion-station-ski-app || true

                        # Clean any stopped containers
                        docker rm -f $(docker ps -a -q --filter "publish=3306") || true
                        docker rm -f $(docker ps -a -q --filter "publish=8089") || true

                        # Clean and recreate Docker network
                        docker network rm station-ski-net || true
                        docker network create station-ski-net || true

                        # Run MySQL container
                        docker run -d --name station-ski-mysql \
                            --network station-ski-net \
                            -e MYSQL_ALLOW_EMPTY_PASSWORD=yes \
                            -e MYSQL_DATABASE=stationSki \
                            -p 3306:3306 \
                            -v mysql_data:/var/lib/mysql \
                            --health-cmd="mysqladmin ping -h localhost" \
                            --health-interval=10s \
                            --health-timeout=5s \
                            --health-retries=5 \
                            mysql:5.7

                        # Wait for MySQL to be healthy
                        timeout 180s bash -c 'until docker inspect station-ski-mysql --format "{{.State.Health.Status}}" | grep "healthy"; do sleep 5; echo "Waiting for MySQL..."; done'

                        # Run application container
                        docker run -d --name gestion-station-ski-app \
                            --network station-ski-net \
                            -p 8089:8089 \
                            -e SPRING_PROFILES_ACTIVE=docker \
                            -e SPRING_DATASOURCE_URL=jdbc:mysql://station-ski-mysql:3306/stationSki?createDatabaseIfNotExist=true \
                            -e SPRING_DATASOURCE_USERNAME=root \
                            -e SPRING_DATASOURCE_PASSWORD= \
                            ${DOCKER_IMAGE}:${DOCKER_TAG}
                    '''
                }
            }
        }
    }
}