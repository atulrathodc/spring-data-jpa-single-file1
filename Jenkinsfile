pipeline {
    agent any
    tools {
        jdk 'jdk' // Use the JDK version configured in Jenkins
        gradle 'gradle8' // Use the Gradle version configured in Jenkins
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
            environment {
                SONAR_TOKEN = credentials('sqp_3b453fb5f3e71f70170588d8c34651a8fd32e876') // Jenkins credential ID for SonarQube token
                SONAR_HOST_URL = 'http://localhost:9000/'
            }
            steps {
                withSonarQubeEnv('sonar') { // Replace with the correct SonarQube installation name
                    sh "./gradlew sonar -Dsonar.host.url=${env.SONAR_HOST_URL} -Dsonar.login=${env.SONAR_TOKEN}"
                }
            }
        }
    }
}