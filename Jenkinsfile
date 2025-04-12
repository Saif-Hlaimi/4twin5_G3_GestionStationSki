pipeline {
	agent any
    environment {
		JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64/"
        M2_HOME = "/usr/share/maven"  // Mise à jour du chemin de Maven
        PATH = "$M2_HOME/bin:$PATH"
        DOCKER_IMAGE = 'ferielyahyaoui/gestion-station-ski:1.0.0'


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
	    /*
         stage('Nexus') {
			steps {
				sh 'mvn clean deploy -Dmaven.test.skip=true'            }
        }*/
	   stage('Sonar Analysis') {
    steps {
        script {
            try {
                withCredentials([string(credentialsId: 'jenkins-sonar', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('sq1') {
                        sh "mvn sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.projectKey=tn.esprit.spring:gestion-station-ski -Dsonar.host.url=http://192.168.1.18:8181"
                    }
                }
            } catch (Exception e) {
                error "SonarQube analysis failed: ${e.getMessage()}"
            }
        }
    }
}




          stage('Deploy avec Docker Compose') {
                    steps {
                        script {
                              sh 'which docker || echo "Docker non disponible"'
         		     sh 'docker pull $DOCKER_IMAGE'
                	    sh 'docker-compose down || true'
       			     sh 'docker-compose up -d'
                        }
                    }
                }

          stage('Vérification des conteneurs') {
             steps {
                script {
                  sh 'docker ps'
                       }
                       }
          }



stage('Grafana') {
    steps {
        script {
            echo "Vérification de l'état de Grafana depuis Jenkins..."

            try {
                // Vérification de la connectivité avec Grafana
                def response = sh(script: """
                    curl -s -o /dev/null -w "%{http_code}" http://grafana:3000
                """, returnStdout: true).trim()

                if (response == '200') {
                    echo "Grafana est opérationnel depuis Jenkins."
                } else {
                    echo "Grafana n'est pas accessible depuis Jenkins. Code HTTP: ${response}"
                }
            } catch (Exception e) {
                echo "Erreur lors de la vérification de Grafana : ${e.message}"
            }
        }
    }
}


















	    
	
}

}
