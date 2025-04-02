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
        stage('Nexus') {
            steps {
                sh 'mvn clean deploy -Dmaven.test.skip=true'
            }
        }
       stage('Docker Build') {
                  steps {
                      script {
                          def imageName = "skier-app"
                          def imageTag = "latest"

                          // Ensure Dockerfile uses eclipse-temurin:11-jdk-alpine
                          sh "sed -i 's|openjdk:11-jdk-alpine|eclipse-temurin:11-jdk-alpine|g' Dockerfile"

                          // Build with host networking
                          sh "docker build --network=host -t ${imageName}:${imageTag} ."
                      }
                  }
              }
    }
}