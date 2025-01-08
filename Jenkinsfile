pipeline {
    agent any
    tools {
        jdk 'jdk' // Use the JDK version configured in Jenkins
        gradle 'gradle8' // Use the Gradle version configured in Jenkins
    }
    environment {
        SONAR_TOKEN = credentials('key2') // Jenkins credential ID for SonarQube token
        SONAR_HOST_URL = 'http://localhost:9000/'
    }
    stages {
        stage('Build') {
            steps {
                // Run Gradle build
                sh 'echo "Running Gradle build..."'
                sh './gradlew clean build' // Use the Gradle wrapper
            }
        }
        stage('Static Code Analysis') {
            steps {
                withSonarQubeEnv('sonar') { // Replace 'SonarQube' with the correct SonarQube installation name
                    sh "echo 'Running SonarQube Analysis...' ""
                    sh "./gradlew sonar -Dsonar.host.url=${env.SONAR_HOST_URL} -Dsonar.login=${env.SONAR_TOKEN}"
                    sh "echo 'SonarQube analysis triggered. Waiting for Quality Gate...'"
                }
            }
        }
        stage('Quality Gate Check') {
            steps {
                script {
                    def qualityGate = waitForQualityGate(abortPipeline: true, credentialsId: 'key2')
                    if (qualityGate.status != 'OK') {
                        echo "Quality Gate failed with status: ${qualityGate.status}"
                        // Optionally take other actions based on failure
                    } else {
                        echo "Quality Gate passed successfully."
                    }
                }
            }
        }
    }
}