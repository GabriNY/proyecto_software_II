
pipeline {
    agent any
    tools {
        jdk 'JDK21'
    }
    options {
        skipDefaultCheckout(true)
    }

    stages {
        stage('1. Codigo Fuente') {
            steps {
                checkout scm
            }
        }

        stage('2. Construccion Automatica') {
            steps {
                dir('backend') {
                    bat 'mvnw.cmd clean compile'
                }
            }
        }

        stage('3. Pruebas Automatizadas') {
            steps {
                dir('backend') {
                    bat 'mvnw.cmd test'
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true,
                  testResults: 'backend/target/surefire-reports/TEST-*.xml'
        }

        success {
            echo 'Pipeline completado correctamente.'
        }

        failure {
            echo 'El pipeline ha fallado.'
        }
    }
}
