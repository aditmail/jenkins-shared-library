def call() {
    pipeline {
        agent any
        /*agent {
            label "Windows_Node"
        }*/

        tools {
            maven 'maven3.6-internal'
        }

        environment {
            RUN_UAT = true
            APPLICATION = "klikBCAIndividu"
            DEPLOY_FOLDER = "${JWORKSPACE}/${APPLICATION}/PREPARE/UAT/${SERVER_TARGET}"
            WORKSPACE = "${JWORKSPACE}/${JOB_NAME}"
        }

        parameters {
            booleanParam(name: 'Refresh', defaultValue: false, description: 'Reload Jenkinsfile and Exit')
            string(name: 'PROJECT_NAME', defaultValue: '', description: '')
            text(name: 'DESCRIPTION', defaultValue: '', description: '')

            booleanParam(name: 'IBank', defaultValue: false, description: '')
            booleanParam(name: 'IBSmartphone', defaultValue: false, description: '')
            booleanParam(name: 'IBankBatch', defaultValue: false, description: '')
            booleanParam(name: 'LimitValidator', defaultValue: false, description: '')

            //Copy Library for WEB
            text(name: 'CopyLibWeb', defaultValue: '', description: '')
            string(name: 'CustomFolderLibWeb', defaultValue: '', description: '')

            //Copy Library for APP
            text(name: 'CopyLibApp', defaultValue: '', description: '''
                Please Write your lib to copy in maven structure 
				Example :: 
				<dependency>
							  <groupId>log4j</groupId>
							  <artifactId>log4j</artifactId>
							  <version>1.2.13</version>
				</dependency>
				'''
            )
            string(name: 'CustomFolderLibApp', defaultValue: '', description: '')

            booleanParam(name: 'StripVersion', defaultValue: true, description: 'If "thick" remove version in name lib')
            booleanParam(name: 'TransitiveDependencies', defaultValue: true, description: 'If "thick" all Transitive Dependencies  will include into library')

            //Properties for APP
            booleanParam(name: '[app]Debug.properties', defaultValue: false, description: '')
            booleanParam(name: '[app]Debug_img.properties', defaultValue: false, description: '')
            booleanParam(name: '[app]SMSMessages.properties', defaultValue: false, description: '')
            booleanParam(name: '[app]System.properties', defaultValue: false, description: '')
            booleanParam(name: '[app]EmailQueue.properties', defaultValue: false, description: '')

            //Properties for WEB
            //*for IBank
            booleanParam(name: '[web]ibank/Debug.properties', defaultValue: false, description: '')
            booleanParam(name: '[web]ibank/cabang.xml', defaultValue: false, description: '')
            booleanParam(name: '[web]ibank/kota.xml', defaultValue: false, description: '')
            booleanParam(name: '[web]ibank/System.properties', defaultValue: false, description: '')

            //*for Mklik
            booleanParam(name: '[web]mklik/Debug.properties', defaultValue: false, description: '')
            booleanParam(name: '[web]mklik/cabang.xml', defaultValue: false, description: '')
            booleanParam(name: '[web]mklik/kota.xml', defaultValue: false, description: '')
            booleanParam(name: '[web]mklik/System.properties', defaultValue: false, description: '')

            booleanParam(name: 'DEPLOY_APP', defaultValue: false, description: '')
            booleanParam(name: 'DEPLOY_WEB', defaultValue: false, description: '')
        }
    }

    stages {
        stage("Reload JenkinsFile") {
            when {
                expression {
                    return "${Refresh}" == "true"
                }
            }

            steps {
                println("URL Workspace: ${utilBCA.getURLWorkspace("$JOB_NAME")}")
            }
        }
    }
}
