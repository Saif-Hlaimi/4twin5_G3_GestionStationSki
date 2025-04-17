pipeline {
    agent any

    environment {
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64/"
        M2_HOME = "/usr/share/maven"
        PATH = "$M2_HOME/bin:$PATH"
        DOCKER_IMAGE = 'elaasboui/gestion-station-ski'
        DOCKER_TAG = '1.0.0'
    }

    stages {
        stage('Hello Test') {
            steps {
                echo '👋 Hello Elaa ! Pipeline lancé...'
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
                        error "❌ Maven clean compile failed: ${e.getMessage()}"
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
                        error "❌ Test execution failed: ${e.getMessage()}"
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
                        error "❌ SonarQube analysis failed: ${e.getMessage()}"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def imageExists = sh(script: "docker images -q ${DOCKER_IMAGE}:${DOCKER_TAG}", returnStdout: true).trim()
                    if (!imageExists) {
                        echo "🛠️ Image introuvable. Construction en cours..."
                        sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    } else {
                        echo "✅ Image déjà existante : ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    }
                }
            }
        }

        stage('Push to Docker Hub') {
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
                            echo "⚠️ L'image ${DOCKER_IMAGE}:${DOCKER_TAG} existe déjà sur DockerHub. Pas de push nécessaire."
                        } else {
                            echo "🔐 Connexion à DockerHub et push en cours..."
                            sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                            sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        }
                    }
                }
            }
        }

        stage('Déploiement avec Docker Compose') {
            steps {
                script {
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
    }

    post {
        always {
            echo '✅ Pipeline terminé avec succès ou avec erreurs.'
        }
    }
}