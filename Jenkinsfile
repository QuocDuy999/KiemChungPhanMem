pipeline {
    agent any

    stages {

        stage('Build') {
            steps {
                bat '.\\mvnw.cmd clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat '.\\mvnw.cmd sonar:sonar -Dsonar.projectKey=food-store'
                }
            }
        }
    }
}