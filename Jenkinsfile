pipeline {
    agent {
        docker { image 'piotrhosa/17-gradle-cdk' }
    }

    parameters {
        credentials(credentialType: 'com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl',
                defaultValue: '',
                description: 'AWS Credentials',
                name: 'aws_credentials',
                required: true
        )
    }

    stages {
        stage('Clone repo') {
            steps {
                git branch: 'main', url: 'https://github.com/hopstoker/tui-git.git'
            }
        }
        stage('Test service') {
            steps {
                sh './gradlew clean test integrationtest'
            }
        }
        stage('Create service jar') {
            steps {
                sh './gradlew clean bootjar'
            }
        }
        stage('CDK prepare and deploy') {
            steps {
                dir('cdk') {
                    sh './gradlew clean shadowjar'

                    withCredentials([aws(credentialsId: "aws_credentials")]) {
                        sh 'cdk synth'
                        sh 'cdk bootstrap --app cdk.out'
                        sh 'cdk deploy --app cdk.out --require-approval never'
                    }
                }
            }
        }
    }
}
