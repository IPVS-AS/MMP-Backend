def version = "${env.BUILD_NUMBER}"
def app
def containerName = "mmp-backend${version}"

pipeline {
	agent any
	stages {
	    stage('Clone Repository') {
	        /* Let's make sure we have the repository cloned to our workspace */
	        steps {
		        cleanWs()
		        checkout scm
	        }
	    }

	    stage('Build Gradle') {
	        /* This builds the actual image; synonymous to
	         * docker build on the command line */
	        steps {
	       		sh "./gradlew clean"
	       		sh "./gradlew -Pversion=${version} build"
	       	}
	    }

	    stage('Test Gradle') {
	        steps {
	        	sh "./gradlew test"
	        }
		}

		stage('SonarQube analysis') {
	        steps {
                withSonarQubeEnv('My SonarQube Server') {
                  // requires SonarQube Scanner for Gradle 2.1+
                  // It's important to add --info because of SONARJNKNS-281
                  sh './gradlew --info sonarqube -Dsonar.projectVersion=${version}'
                }
	        }
        }
	    stage('Publish JAR to Registry') {
		    steps {
		    	uploadJarToNexus(version)
		    }
	    }

	    stage('Publish Docker to Registry') {
		    steps {
		    	script {
				    docker.withServer('tcp://10.0.19.19:2375') {
				        docker.withRegistry('https://10.0.19.19:18444', 'adminnexus') {
				            docker.build("ipvs.as/mmp-backend", "--build-arg JAR_FILE=build/libs/mmp-backend-${version}-boot.jar .").push("${version}")
				        }
				    }
				}
		    }
	    }

		stage('Test Docker') {
	  		steps {
			    script{
			    	docker.withServer('tcp://10.0.19.19:2375') {
				      	sh "docker run -d --name ${containerName} \
				        -p 8080:8080 10.0.19.19:18444/ipvs.as/mmp-backend:${version}"
			    	}
			    	try {
                        timeout(time: 2, unit: 'MINUTES') {
                            waitUntil {
                                try {
                                    def statusCode = sh(script: "curl --max-time 30 -sL -w '%{http_code}' '10.0.19.19:8080/actuator/health' -o /dev/null", returnStdout: true).trim()
                                    return (statusCode == '200')
                                } catch (exception) {
                                    return false
                                }
                            }
                        }
                    } catch (err) {
                        echo "Caught Timeout: ${err}"
                        currentBuild.result = 'FAILURE'
                    }
			    }
			}
		}

	  	stage('Run Docker') {
	  		steps {
	  			script {
		        	docker.withServer('tcp://10.0.19.17:2375') {
		            	docker.withRegistry('https://10.0.19.19:18444', 'adminnexus') {
		                	sh "docker stop mmp-backend || true && docker rm mmp-backend || true"
		                	sh "docker rmi \$(docker images |grep '10.0.19.19:18444/ipvs.as/mmp-backend') || true"
		                	sh "docker pull 10.0.19.19:18444/ipvs.as/mmp-backend:${version}"
		                	sh "docker run -d --name=mmp-backend --restart=always -p 8080:8080 10.0.19.19:18444/ipvs.as/mmp-backend:${version}"

		        			}
		      			}
	  			}
		    }
	  	}
	  	stage("Quality Gate") {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                    // true = set pipeline to UNSTABLE, false = don't
                    // Requires SonarQube Scanner for Jenkins 2.7+
                    waitForQualityGate abortPipeline: true
                }
            }
        }
	 }

	post('Send Messages and Clean Up') {
	    success {
	        slackSend message:"Build Success! Keep it up! - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", color: 'good'
	    }
	    failure {
	        slackSend message:"Build Failed! You done fucked up! - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", color: 'danger'
	    }
	    fixed {
	        slackSend message:"Build Fixed! Good job! - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", color: '#439FE0'
	    }
	    cleanup {
	    	script {
		    	docker.withServer('tcp://10.0.19.19:2375') {
		      		sh "docker rm -f ${containerName} || true"
			        sh "docker rmi -f 10.0.19.19:18444/ipvs.as/mmp-backend:${version} || true"
			        sh "docker rmi -f ipvs.as/mmp-backend:latest || true"
				}
			}
	    }
	}
}

def uploadJarToNexus(version) {
  	nexusArtifactUploader artifacts: [
    [artifactId: 'mmp-backend',
    file: "build/libs/mmp-backend-${version}-boot.jar",
    type: 'jar']
  	], credentialsId: 'adminnexus', groupId: 'ipvs.as', nexusUrl: '10.0.19.19:8081',
  	nexusVersion: 'nexus3', protocol: 'http', repository: 'maven-releases',
  	version: "${version}"
}