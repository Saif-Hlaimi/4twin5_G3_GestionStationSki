pipeline {
    agent any
    environment {
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64"
        MAVEN_OPTS = "--add-opens java.base/java.lang=ALL-UNNAMED -Djdk.module.illegalAccess=permit"
        M2_HOME = "/usr/share/maven"
        PATH = "${M2_HOME}/bin:${JAVA_HOME}/bin:${PATH}"
        DOCKER_IMAGE = "gestion-station-ski:latest"
        DOCKER_TAG = '1.0.0'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'SaifHlaimi-4TWIN5-G3',
                    url: 'https://github.com/Saif-Hlaimi/4twin5_G3_GestionStationSki.git',
                    credentialsId: 'jenkins-key'
            }
        }

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

        stage('SonarQube Analysis') {
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
                            sh 'docker build --network=host -t ${DOCKER_IMAGE}:${DOCKER_TAG} .'
                            // Tag command fixed to use single tag
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
            }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/**/*.xml'
            cleanWs()
        }
        success {
            echo 'Pipeline exécutée avec succès! ✅'
        }
        failure {
            echo 'Échec de la pipeline! ❌'
        }
    }
}