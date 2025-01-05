pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Building...'
                sh './gradlew build' // Replace with your build command
            }
        }

        stage('Test') {
            steps {
                echo 'Testing...'
                sh './gradlew test' // Replace with your test command
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying...'
                sh './deploy.sh' // Replace with your deploy command
            }
        }
    }
}