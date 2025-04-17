pipeline {
    agent any
    environment {
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64/"
        M2_HOME = "/usr/share/maven"
        PATH = "$M2_HOME/bin:$JAVA_HOME/bin:$PATH"
        DOCKER_IMAGE = 'elaasboui/gestion-station-ski'  // Docker image name
        DOCKER_TAG = '1.0.0'  // Docker image tag

        // Configurable parameters
        GRAFANA_URL = "http://192.168.33.10:3000/"
        DASHBOARD_URL = "http://192.168.33.10:3000/d/haryan-jenkins/jenkins3a-performance-and-health-overview?orgId=1&from=now-30m&to=now&timezone=browser"
        NOTIFICATION_EMAIL = 'elaa.sboui@esprit.tn'
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
                    url: 'https://github.com/Saif-Hlaimi/4twin5_G3_GestionStationSki.git'
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

        stage('Grafana Status Check') {
            steps {
                script {
                    def grafanaStatus = checkGrafanaStatus(GRAFANA_URL)
                    if (grafanaStatus) {
                        echo "Grafana is accessible: ${GRAFANA_URL}"
                        echo "Dashboard URL: ${DASHBOARD_URL}"
                    } else {
                        echo "Grafana is not accessible at the moment."
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    def imageExists = sh(script: "docker images -q ${DOCKER_IMAGE}:${DOCKER_TAG}", returnStdout: true).trim()
                    if (!imageExists) {
                        echo "Image not found. Building now..."
                        sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    } else {
                        echo "Docker image already exists: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    }
                }
            }
        }

        stage('Push Docker Image to DockerHub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_HUB_USER', passwordVariable: 'DOCKER_HUB_PWD')]) {
                        def imageName = "${DOCKER_IMAGE}".split('/')[1] // Ex: gestion-station-ski
                        def repo = "${DOCKER_IMAGE}".split('/')[0]    // Ex: ferielyahyaoui

                        def exists = sh(
                            script: """
                                curl -s -o /dev/null -w "%{http_code}" \
                                https://hub.docker.com/v2/repositories/${repo}/${imageName}/tags/${DOCKER_TAG}
                            """,
                            returnStdout: true
                        ).trim()

                        if (exists == '200') {
                            echo "Image ${DOCKER_IMAGE}:${DOCKER_TAG} exists on DockerHub. No need to push."
                        } else {
                            echo "Image not found on DockerHub. Pushing image..."
                            sh 'echo $DOCKER_HUB_PWD | docker login -u $DOCKER_HUB_USER --password-stdin'
                            sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    sh '''#!/bin/bash
                        set -e

                        # Stop and remove containers using ports 3306 and 8089
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

    post {
        success {
            sendMail('SUCCESS')
        }
        failure {
            sendMail('FAILURE')
        }
    }
}

def checkGrafanaStatus(String url) {
    echo "Checking Grafana status..."
    def response = httpRequest(url: url, validResponseCodes: '200,302')
    return response != null
}

def sendMail(String status) {
    def subject = (status == 'SUCCESS') ? "Success - Build ${env.JOB_NAME} #${env.BUILD_NUMBER}" : "Failure - Build ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    def body = (status == 'SUCCESS') ? generateSuccessMailBody() : generateFailureMailBody()

    mail to: "${params.NOTIFICATION_EMAIL}",
         subject: subject,
         mimeType: 'text/html',
         body: body
}

def generateSuccessMailBody() {
    return """
    <html>
        <body style="font-family:Arial, sans-serif; color:#333;">
            <h2 style="color:green;">Pipeline completed successfully</h2>
            <p><strong>Job:</strong> ${env.JOB_NAME}</p>
            <p><strong>Build #:</strong> ${env.BUILD_NUMBER}</p>
            <p><strong>Duration:</strong> ${currentBuild.durationString}</p>
            <p><strong>Status:</strong> <span style="color:green;"><b>SUCCESS</b></span></p>
            <p><strong>Link:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
        </body>
    </html>
    """
}

def generateFailureMailBody() {
    return """
    <html>
        <body style="font-family:Arial, sans-serif; color:#333;">
            <h2 style="color:red;">Pipeline failed</h2>
            <p><strong>Job:</strong> ${env.JOB_NAME}</p>
            <p><strong>Build #:</strong> ${env.BUILD_NUMBER}</p>
            <p><strong>Duration:</strong> ${currentBuild.durationString}</p>
            <p><strong>Status:</strong> <span style="color:red;"><b>FAILURE</b></span></p>
            <p><strong>Failed Step:</strong> Check Jenkins logs for details</p>
            <p><strong>Link:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
        </body>
    </html>
    """
}