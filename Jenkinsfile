pipeline {
    agent any
    tools {
        jdk 'jdk' // Use the JDK version configured in Jenkins
        gradle 'gradle8' // Use the Gradle version configured in Jenkins
    }
    environment {
        SONAR_TOKEN = credentials('sqp_3b453fb5f3e71f70170588d8c34651a8fd32e876') // Replace with your Jenkins credential ID for SonarQube token
        SONAR_HOST_URL = 'http://localhost:9000/'
    }
    stages {
        stage('Build') {
            steps {
                // Run Gradle build
                sh './gradlew clean build'
            }
        }
      stage("Sonarqube Analysis "){
            steps{
                withSonarQubeEnv('sonar-server') {
                    sh ''' $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectName=ABC \
                    -Dsonar.java.binaries=. \
                    -Dsonar.projectKey=ABC '''

                }
            }
        }

        stage("OWASP Dependency Check"){
            steps{
                dependencyCheck additionalArguments: '--scan ./ --format HTML ', odcInstallation: 'DP'
                dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
            }
        }
        stage("Quality Gate Check") {
            steps {
                // Check SonarQube Quality Gate status
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Post-Quality Gate Build') {
            steps {
                // Proceed with subsequent build steps if the quality gate passes
                echo 'Quality Gate passed. Proceeding with the build...'
                sh './gradlew package' // Example additional build task
            }
        }
    }
}