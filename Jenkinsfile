pipeline {
    agent any
    environment {
        JAVA_HOME = "/usr/lib/jvm/java-8-openjdk-amd64/"  // Changé pour Java 1.8
        M2_HOME = "/usr/share/maven"
        PATH = "$M2_HOME/bin:$PATH"
    }

    stages {
        stage('Hello Test') {
            steps {
                echo ' hello saif '
            }
        }

        stage('Git Checkout') {
            steps {
                git branch: 'SaifHlaimi-4TWIN5-G3',
                    url: 'https://github.com/Saif-Hlaimi/4twin5_G3_GestionStationSki.git',
                    credentialsId: 'jenkins-key'
            }
        }

        stage('Clean compile') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test Projet') {
            steps {
                sh 'mvn -Dtest=RegistrationServicesImplTest clean test -Dspring.profiles.active=test'
            }
        }

        stage('Sonar Analysis') {
            steps {
                withSonarQubeEnv('sq1') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t gestion-station-ski:latest .'
            }
        }

        stage('Push Docker Image') {
            steps {
                sh 'docker push gestion-station-ski:latest'
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker run -d -p 9000:9000 gestion-station-ski:latest'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
            junit 'target/surefire-reports/*.xml'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
        }
    }
}