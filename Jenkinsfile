pipeline {
    agent any
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
    }
}