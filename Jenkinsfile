pipeline {
    agent any

    stages {

        stage('Build') {
            steps {
                bat '.\\mvnw.cmd clean verify'
            }
        }
        stage('Run API Tests') {
            steps {
                bat 'newman run postman/FoodStore.Postman.json'
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