pipeline {
    agent any
    environment {
        JAVA_HOME = "/usr/lib/jvm/java-8-openjdk-amd64/"  // Utilise Java 1.8
        M2_HOME = "/usr/share/maven"
        PATH = "$M2_HOME/bin:$PATH"
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
                sh 'mvn clean compile -Xmx1024m -Xms512m'  // Compilation avec mémoire ajustée
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -Dspring.profiles.active=test -Xmx1024m -Xms512m'  // Teste tous les tests (incluant CourseTest)
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sq1') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t gestion-station-ski:latest .'
            }
        }

        stage('Docker Deploy') {
            steps {
                sh 'docker run -d -p 9000:9000 gestion-station-ski:latest'  // Déploie sur le port 9000 (externe) vers 8089 (interne)
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'  // Archive les rapports JUnit
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
        }
    }
}