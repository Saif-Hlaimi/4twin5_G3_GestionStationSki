pipeline {
    agent any
    environment {
        DOCKER_IMAGE = 'saifhl888/gestion-station-ski'
        DOCKER_TAG = '1.0.0'
        EMAIL_RECIPIENT = 'saif.hlaimi@esprit.tn'
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64"
        MAVEN_OPTS = "--add-opens java.base/java.lang=ALL-UNNAMED -Djdk.module.illegalAccess=permit"
        M2_HOME = "/usr/share/maven"
        PATH = "${M2_HOME}/bin:${JAVA_HOME}/bin:${PATH}"
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test -Dspring.profiles.active=test -DargLine="--add-opens java.base/java.lang=ALL-UNNAMED"'
            }
        }
        stage('Sonar Analysis') {
            steps {
                withSonarQubeEnv('sq1') {
                    sh 'mvn sonar:sonar -Dsonar.java.jdkHome=${JAVA_HOME}'
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
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                   usernameVariable: 'DOCKER_HUB_USER',
                                                   passwordVariable: 'DOCKER_HUB_PWD')]) {
                        sh 'echo $DOCKER_HUB_PWD | docker login -u $DOCKER_HUB_USER --password-stdin'
                        sh 'docker build --network=host -t ${DOCKER_IMAGE}:${DOCKER_TAG} .'
                    }
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

                        # Stop and remove any containers using ports 3306 and 9000
                        echo "Checking for containers on port 3306..."
                        CONTAINERS_3306=$(docker ps -q --filter "publish=3306")
                        if [ -n "$CONTAINERS_3306" ]; then
                            echo "Port 3306 in use, stopping containers: $CONTAINERS_3306"
                            docker stop $CONTAINERS_3306 || true
                            docker rm -f $CONTAINERS_3306 || true
                        fi

                        echo "Checking for containers on port 9000..."
                        CONTAINERS_9000=$(docker ps -q --filter "publish=9000")
                        if [ -n "$CONTAINERS_9000" ]; then
                            echo "Port 9000 in use, stopping containers: $CONTAINERS_9000"
                            docker stop $CONTAINERS_9000 || true
                            docker rm -f $CONTAINERS_9000 || true
                        fi

                        # Additional cleanup for known container names
                        docker stop station-ski-mysql gestion-station-ski-app || true
                        docker rm -f station-ski-mysql gestion-station-ski-app || true

                        # Clean up any stopped containers still binding ports
                        docker rm -f $(docker ps -a -q --filter "publish=3306") || true
                        docker rm -f $(docker ps -a -q --filter "publish=9000") || true

                        # Clean up and recreate network
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
                            -p 9000:9000 \
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
    post {
        always {
            script {
                emailext(
                    subject: "${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """<h2>Build Notification</h2>
                        <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build #:</strong> ${env.BUILD_NUMBER}</p>
                        <p><strong>Status:</strong> ${currentBuild.currentResult}</p>
                        <p><strong>URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    """,
                    to: env.EMAIL_RECIPIENT,
                    mimeType: 'text/html',
                    replyTo: 'saif.hlaimi@esprit.tn',
                    from: 'saif.hlaimi@esprit.tn'
                )
            }
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            cleanWs()
        }
    }
}