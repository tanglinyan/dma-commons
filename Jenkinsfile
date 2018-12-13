pipeline {
    agent any

    tools {
        maven 'M3.3.9'
    }

    triggers {
        pollSCM('H * * * *')
    }

    stages {
        stage('build') {
            steps {
                withMaven(options: [junitPublisher(ignoreAttachments: false), artifactsPublisher()]) {
                    sh 'mvn -DincludeSrcJavadocs clean source:jar install checkstyle:check'
                }
            }
        }
    }


    post {
        failure {
            // notify users when the Pipeline fails
            mail to: 'steen@lundogbendsen.dk',
                    subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                    body: "Something is wrong with ${env.BUILD_URL}"
        }
    }
}

