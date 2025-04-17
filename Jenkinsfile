pipeline {
    agent any

    environment {
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64/"
        M2_HOME = "/usr/share/maven"
        PATH = "$M2_HOME/bin:$PATH"
        DOCKER_IMAGE = "elaasboui/alpine:1.0.0"
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

        stage('Build JAR') {
            steps {
                script {
                    try {
                        sh 'mvn package -Dmaven.test.skip=true'
                    } catch (Exception e) {
                        error "JAR build failed: ${e.getMessage()}"
                    }
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                script {
                    try {
                        sh 'docker build -t $DOCKER_IMAGE .'
                        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                        sh 'docker push $DOCKER_IMAGE'
                    } catch (Exception e) {
                        error "Docker build/push failed: ${e.getMessage()}"
                    }
                }
            }
        }

        stage('Déploiement avec Docker Compose') {
            steps {
                script {
                    try {
                        sh 'docker pull $DOCKER_IMAGE'
                        sh 'docker compose down || true'
                        sh 'docker compose up -d'
                    } catch (Exception e) {
                        error "Déploiement échoué : ${e.getMessage()}"
                    }
                }
            }
        }

        stage('Vérification des conteneurs') {
            steps {
                script {
                    sh 'docker ps'
                }
            }
        }
    }

    post {
        always {
            echo '✅ Pipeline terminé.'
        }
    }
}
