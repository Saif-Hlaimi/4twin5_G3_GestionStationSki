pipeline {
    agent any

    environment {
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64/"
        M2_HOME = "/usr/share/maven"
        PATH = "$M2_HOME/bin:$PATH"
        DOCKER_IMAGE = 'ferielyahyaoui/gestion-station-ski'
        DOCKER_TAG = '1.0.0'
    }

    stages {

        stage('Hello Test') {
            steps {
                echo ' Hello Feryal!'
            }
        }

        stage('Git Checkout') {
            steps {
                git branch: 'FeryalYahyaoui-4TWIN5-G3',
                    url: 'https://github.com/Saif-Hlaimi/4twin5_G3_GestionStationSki.git',
                    credentialsId: 'jenkins-key'
            }
        }

        stage('Clean & Compile') {
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
                withCredentials([string(credentialsId: 'jenkins-sonar', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('sq1') {
                        sh """
                            mvn sonar:sonar \
                            -Dsonar.login=$SONAR_TOKEN \
                            -Dsonar.projectKey=tn.esprit.spring:gestion-station-ski \
                            -Dsonar.host.url=http://192.168.1.18:8181
                        """
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def imageExists = sh(script: "docker images -q ${DOCKER_IMAGE}:${DOCKER_TAG}", returnStdout: true).trim()
                    if (!imageExists) {
                        echo " Image not found. Building now..."
                        sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    } else {
                        echo " Docker image already exists: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    }
                }
            }
        }

	    
        stage('Push to Docker Hub') {
    steps {
        script {
            withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {

                // Extraire nom d'image sans le namespace
                def imageName = "${DOCKER_IMAGE}".split('/')[1] // Ex: gestion-station-ski
                def repo = "${DOCKER_IMAGE}".split('/')[0]       // Ex: ferielyahyaoui

                // Vérifier si le tag existe déjà sur DockerHub
                def exists = sh(
                    script: """
                        curl -s -o /dev/null -w "%{http_code}" \
                        https://hub.docker.com/v2/repositories/${repo}/${imageName}/tags/${DOCKER_TAG}
                    """,
                    returnStdout: true
                ).trim()

                if (exists == '200') {
                    echo "L'image ${DOCKER_IMAGE}:${DOCKER_TAG} existe déjà sur DockerHub. Pas de push nécessaire." 
                } else {
                    echo " Image non trouvée sur DockerHub. Connexion et push en cours..." 
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                }
            }
        }
    }
}


        stage('Deploy avec Docker Compose') {
            steps {
                script {
                    sh 'which docker || echo "Docker non disponible"'
                    sh 'docker pull $DOCKER_IMAGE:$DOCKER_TAG'
                    sh 'docker-compose down || true'
                    sh 'docker-compose up -d'
                }
            }
        }

        stage('Vérification des conteneurs') {
            steps {
                sh 'docker ps'
            }
        }

 stage('Grafana') {
    steps {
        script {
            def grafanaUrl = "http://192.168.1.18:3000"
            def dashboardUrl = "${grafanaUrl}/d/haryan-jenkins/jenkins3a-performance-and-health-overview?orgId=1&from=now-30m&to=now"

            echo " Vérification de l'état de Grafana..."

            def status = sh(script: "curl -s -o /dev/null -w '%{http_code}' ${grafanaUrl}", returnStdout: true).trim()

		           if (status == '200' || status == '302') {
		    echo " Grafana est accessible : ${grafanaUrl}"
		    echo " Dashboard Jenkins : ${dashboardUrl}"
		} else {
		    echo "Grafana inaccessible (HTTP ${status})"
		}

        }
    }
}


	    
    }
 post {
    success {
        mail to: 'ferielyahyaouiii@gmail.com',
             subject: " Succès - Build ${env.JOB_NAME} #${env.BUILD_NUMBER}",
             mimeType: 'text/html',
             body: """
             <html>
                 <body style="font-family:Arial, sans-serif; color:#333;">
                     <h2 style="color:green;">Pipeline terminé avec succès</h2>
                     <p><strong> Job :</strong> ${env.JOB_NAME}</p>
                     <p><strong> Build # :</strong> ${env.BUILD_NUMBER}</p>
                     <p><strong> Durée :</strong> ${currentBuild.durationString}</p>
                     <p><strong> Status :</strong> <span style="color:green;"><b>SUCCESS</b></span></p>
                     <p><strong> Lien :</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                 </body>
             </html>
             """
    }

    failure {
        mail to: 'ferielyahyaouiii@gmail.com',
             subject: " Échec - Build ${env.JOB_NAME} #${env.BUILD_NUMBER}",
             mimeType: 'text/html',
             body: """
             <html>
                 <body style="font-family:Arial, sans-serif; color:#333;">
                     <h2 style="color:red;"> Le pipeline a échoué</h2>
                     <p><strong> Job :</strong> ${env.JOB_NAME}</p>
                     <p><strong> Build # :</strong> ${env.BUILD_NUMBER}</p>
                     <p><strong>Durée :</strong> ${currentBuild.durationString}</p>
                     <p><strong> Status :</strong> <span style="color:red;"><b>FAILURE</b></span></p>
                     <p><strong> Étape échouée :</strong> Consulte les logs dans Jenkins</p>
                     <p><strong> Lien :</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                 </body>
             </html>
             """
    }
}
}
