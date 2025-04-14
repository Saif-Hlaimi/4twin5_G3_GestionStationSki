pipeline {
    agent any

    environment {
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64/"
        M2_HOME = "/usr/share/maven"
        PATH = "$M2_HOME/bin:$PATH"
        DOCKER_IMAGE = 'ferielyahyaoui/gestion-station-ski'
        DOCKER_TAG = '1.0.0'
    }

    stages {

        stage('Hello Test') {
            steps {
                echo ' Hello Feryal!'
            }
        }

        stage('Git Checkout') {
            steps {
                git branch: 'FeryalYahyaoui-4TWIN5-G3',
                    url: 'https://github.com/Saif-Hlaimi/4twin5_G3_GestionStationSki.git',
                    credentialsId: 'jenkins-key'
            }
        }

        stage('Clean & Compile') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test Projet') {
            steps {
                sh 'mvn -Dtest=RegistrationServicesImplTest clean test'
            }
        }

        stage('Sonar Analysis') {
            steps {
                withCredentials([string(credentialsId: 'jenkins-sonar', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('sq1') {
                        sh """
                            mvn sonar:sonar \
                            -Dsonar.login=$SONAR_TOKEN \
                            -Dsonar.projectKey=tn.esprit.spring:gestion-station-ski \
                            -Dsonar.host.url=http://192.168.1.18:8181
                        """
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def imageExists = sh(script: "docker images -q ${DOCKER_IMAGE}:${DOCKER_TAG}", returnStdout: true).trim()
                    if (!imageExists) {
                        echo " Image not found. Building now..."
                        sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    } else {
                        echo " Docker image already exists: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    }
                }
            }
        }

        stage('Push to DockerHub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        def imageName = "${DOCKER_IMAGE}".split('/')[1]
                        def repo = "${DOCKER_IMAGE}".split('/')[0]

                        def exists = sh(
                            script: """
                                curl -s -o /dev/null -w "%{http_code}" \
                                https://hub.docker.com/v2/repositories/${repo}/${imageName}/tags/${DOCKER_TAG}
                            """,
                            returnStdout: true
                        ).trim()

                        if (exists == '200') {
                            echo "Image ${DOCKER_IMAGE}:${DOCKER_TAG} already exists on DockerHub. Skipping push."
                        } else {
                            echo " Image does not exist. Logging in and pushing..."
                            sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                            sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        }
                    }
                }
            }
        }

        stage('Deploy avec Docker Compose') {
            steps {
                script {
                    sh 'which docker || echo "Docker non disponible"'
                    sh 'docker pull $DOCKER_IMAGE:$DOCKER_TAG'
                    sh 'docker-compose down || true'
                    sh 'docker-compose up -d'
                }
            }
        }

        stage('Vérification des conteneurs') {
            steps {
                sh 'docker ps'
            }
        }

        stage('Grafana') {
            steps {
                script {
                    echo "Vérification de l'état de Grafana depuis Jenkins..."

                    try {
                        def response = sh(script: """
                            curl -s -o /dev/null -w "%{http_code}" http://grafana:3000
                        """, returnStdout: true).trim()

                        if (response == '200') {
                            echo "Grafana est opérationnel depuis Jenkins."
                        } else {
                            echo "Grafana n'est pas accessible depuis Jenkins. Code HTTP: ${response}"
                        }
                    } catch (Exception e) {
                        echo "Erreur lors de la vérification de Grafana : ${e.message}"
                    }
                }
            }
        }
    }

  
}
