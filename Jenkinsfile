pipeline {
	agent any
    environment {
		JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64/"
        M2_HOME = "/usr/share/maven"  // Mise à jour du chemin de Maven
        PATH = "$M2_HOME/bin:$PATH"
    }

    stages {
		stage('Hello Test') {
			steps {
				echo ' hello feryal '
            }
        }

        stage('Git Checkout') {
			steps {
				git branch: 'FeryalYahyaoui-4TWIN5-G3',
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
				sh 'mvn -Dtest=RegistrationServicesImplTest clean test'
            }
        }

       stage('Nexus') {
			steps {
				sh 'mvn clean deploy -Dmaven.test.skip=true'
            }
        }
    }
}