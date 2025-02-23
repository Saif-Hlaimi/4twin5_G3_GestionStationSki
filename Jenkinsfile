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
                sh 'mvn test'
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