pipeline {
    agent any
    environment {
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64"
        MAVEN_OPTS = "--add-opens java.base/java.lang=ALL-UNNAMED -Djdk.module.illegalAccess=permit"
        M2_HOME = "/usr/share/maven"
        PATH = "${M2_HOME}/bin:${JAVA_HOME}/bin:${PATH}"
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

        stage('Docker Build') {
            steps {
                sh 'sudo docker build -t gestion-station-ski:latest .'
            }
        }

        stage('Docker Deploy') {
            steps {
                sh 'sudo docker run -d -p 9000:9000 gestion-station-ski:latest'
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
