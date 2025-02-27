pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {

                git branch: 'master', url: 'https://github.com/Saif-Hlaimi/4twin5_G3_GestionStationSki.git'
            }
        }
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