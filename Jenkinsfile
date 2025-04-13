pipeline {
    agent any
    environment {
        DOCKER_IMAGE = 'saifhlaimi/gestion-station-ski' // Adjust to your DockerHub username
        DOCKER_TAG = '1.0.0'
        EMAIL_RECIPIENT = 'saif.hlaimi@esprit.tn' // Replace with your email
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64"
        MAVEN_OPTS = "--add-opens java.base/java.lang=ALL-UNNAMED -Djdk.module.illegalAccess=permit"
        M2_HOME = "/usr/share/maven"
        PATH = "${M2_HOME}/bin:${JAVA_HOME}/bin:${PATH}"
    }
    stages {
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
        stage('Sonar Analysis') {
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
    post {
        always {
            script {
                emailext(
                    subject: "${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """<h2>Build Notification</h2>
                        <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build #:</strong> ${env.BUILD_NUMBER}</p>
                        <p><strong>Status:</strong> ${currentBuild.currentResult}</p>
                        <p><strong>URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    """,
                    to: env.EMAIL_RECIPIENT,
                    mimeType: 'text/html',
                    replyTo: 'saif.hlaimi@esprit.tn', // Replace with your email
                    from: 'saif.hlaimi@esprit.tn',    // Replace with your email
                    smtp: [
                        host: 'smtp.gmail.com',
                        port: '587',
                        auth: 'true',
                        user: 'saif.hlaimi@esprit.tn', // Replace with your email
                        password: credentials('gmail-smtp-password') // Add this credential in Jenkins
                    ]
                )
            }
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            cleanWs()
        }
    }
}