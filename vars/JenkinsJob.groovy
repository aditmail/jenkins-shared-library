def call() {
    //stage('Initialize-Stage') {
        environment {
            def emailAddress = null
            def isEmailValid = false
            def isPatternValid = false
        }

        steps {
            script {
                outputMessage outputType: 'startStage', level: 'init'
                isEmailValid = utils utilities: 'validateEmail', params: emailto

                if (isEmailValid) {
                    outputMessage "Seems Like you Haven\'t Set Email Yet, Requesting New Input.."
                    emailAddress = utils utilities: 'inputEmail'
                } else {
                    isPatternValid = utils utilities: 'emailPattern', params: emailto

                    if (isPatternValid) {
                        emailAddress = emailto
                    } else {
                        outputMessage "Seems Like you Set Invalid Email, Requesting New Input.."
                        emailAddress = utils utilities: 'inputEmail'
                    }
                }

                //Checking again if email valid or not
                isEmailValid = utils utilities: 'validateEmail', params: emailAddress
                if (isEmailValid) {
                    currentBuild.result = 'ABORTED'
                    outputMessage outputType: 'build', status: 'aborted', params: emailAddress
                }
            }

            bat "java -version"
            bat "gradle -v"
        }
    //}

    /*stage('Build-Stage') {
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

            bat "gradle clean build check test jar"
        }
    }*/
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
                    buildUrl: buildURL,
                    buildNumber: buildNumber,
                    buildTag: buildTag,
                    jobName: jobName,
                    emailTo: emailAddress
            )

            if (params.JUnit) {
                outputMessage "Generating JUnit Reports"
                junit testResults: "**/build/test-results/test/TEST-*.xml"
            }

            if (params.Checkstyle) {
                outputMessage "Generating Checkstyle Reports"
                recordIssues(
                        tools: [
                                checkStyle(pattern: '**/build/reports/checkstyle/*.xml')
                        ]
                )
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
                    buildUrl: buildURL,
                    buildNumber: buildNumber,
                    buildTag: buildTag,
                    jobName: jobName,
                    emailTo: emailAddress
            )
        }
    }
}