#!/usr/bin/env groovy

def call() {
    pipeline {
        agent any
        tools {
            jdk 'Jdk.1.8'
            gradle 'gradle.6.6'
        }

        options {
            timestamps()
        }

        environment {
            jobName = "${env.JOB_NAME}"
            buildURL = "${env.BUILD_URL}"
            buildNumber = "${env.BUILD_NUMBER}"

            buildID = "${env.BUILD_ID}"
            buildTag = "${env.BUILD_TAG}"
        }

        stages {
            //Use this if using Job-DSL? or Seed Job?
            /*stage('Git-Stage') {
            steps {
                vcCheckout(
                        branch: 'master',
                        url: 'https://github.com/aditmail/jenkins-shared-library',
                        credentialsId: 'b5656ab7-b7ed-4c02-9833-9af6877e2b9e'
                )
            }
        }*/

            stage('Initialize-Stage') {
                environment {
                    def emailAddress = null
                    def isEmailValid = false
                    def isPatternValid = false
                }

                steps {
                    script {
                        outputMessage outputType: 'startStage', level: 'init'

                        if (utils.validateEmail(emailto)) {
                            outputMessage.printMsg('no_email')
                            emailAddress = utils.inputEmail()
                        } else {
                            if (utils.validateEmailPattern(emailto)) {
                                emailAddress = emailto
                            } else {
                                outputMessage.printMsg('invalid_email')
                                emailAddress = utils.inputEmail()
                            }
                        }

                        //Checking again if email valid or not
                        if (utils.validateEmail(emailAddress)) {
                            currentBuild.result = 'ABORTED'
                            error("Invalid Parameter in: $emailAddress")

                            outputMessage outputType: 'build', status: 'aborted', params: emailAddress
                        } else {
                            cmdScript method: 'windows', command: 'java -version'
                            cmdScript method: 'windows', command: 'gradle -v'
                        }
                    }
                }
            }

            stage('Build-Stage') {
                steps {
                    script {
                        outputMessage outputType: 'startStage', level: 'build'
                    }

                    git(
                            [
                                    branch       : "master",
                                    credentialsId: "b5656ab7-b7ed-4c02-9833-9af6877e2b9e",
                                    url          : "https://github.com/aditmail/gradleJavaProject.git"
                            ]
                    )
                }
            }

            stage('Unit-Test Stage') {
                steps {
                    script {
                        outputMessage outputType: 'startStage', level: 'unit-test'
                    }

                    cmdScript method: 'windows', command: 'gradle clean build check test jar'
                }
            }
        }

        post {
            always {
                script {
                    outputMessage outputType: 'startStage', level: 'post-stage', params: currentBuild.currentResult
                }
            }

            success {
                script {
                    sendEmail(
                            status: 'Success',
                            emailTo: emailAddress
                    )

                    if (params.JUnit) {
                        generateReports.genJUnit("**/build/test-results/test/TEST-*.xml")
                    }

                    if (params.Checkstyle) {
                        generateReports.genCheckstyle("**/build/reports/checkstyle/*.xml")
                    }
                }

                /*publishHTML target: [
                    allowMissing         : false,
                    alwaysLinkToLastBuild: false,
                    keepAll              : true,
                    reportDir            : "coverage",
                    reportFiles          : 'index.html',
                    reportName           : 'JUnit-Reports'
            ]*/
            }

            failure {
                script {
                    sendEmail(
                            status: 'Failed',
                            emailTo: emailAddress
                    )
                }
            }
        }
    }
}