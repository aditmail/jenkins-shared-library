#!/usr/bin/env groovy
import javax.print.attribute.standard.JobName

def call() {
    pipeline {
        agent {
            label 'Windows_Node'
        }

        options {
            disableConcurrentBuilds()
            timestamps()
        }

        tools {
            maven 'maven3.6-internal'
        }

        environment {
            APPLICATION = 'klikBCAIndividu'
            DEPLOY_FOLDER = "${JWORKSPACE}/${APPLICATION}/PREPARE/CHECKLIST-SERVICE/UAT/${SERVER_TARGET}"
            WORKSPACE = "${JWORKSPACE}/${JOB_NAME}"
        }

        parameters {
            booleanParam(name: 'Refresh', defaultValue: false, description: 'Reload Jenkinsfile and Exit')
            string(name: 'PROJECT_NAME', defaultValue: '', description: '')
            text(name: 'DESCRIPTION', defaultValue: '', description: '')

            booleanParam(name: 'JMS-Listener', defaultValue: false, description: '')
            booleanParam(name: 'IBUserService', defaultValue: false, description: '')
            booleanParam(name: 'IBAdministrationService', defaultValue: false, description: '')
            booleanParam(name: 'IBTransferService', defaultValue: false, description: '')
            booleanParam(name: 'IBConsumerRespApproval', defaultValue: false, description: '')
            booleanParam(name: 'IBConsumerRespRegistration', defaultValue: false, description: '')

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
            booleanParam(
                    name: 'TransitiveDependencies', defaultValue: true,
                    description: 'If "thick" all Transitive Dependencies  will include into library')

            //Model Build
            choice(choices: ['UAT', 'INTRA', 'INTER', 'PILOT', 'PRODUCTION'], description: 'Select Build Flavor', name: 'flavor')

            //Properties for EmailListener
            booleanParam(name: '[service]EmailListener/System.properties', defaultValue: false, description: '')
            booleanParam(name: '[service]EmailListener/Debug.properties', defaultValue: false, description: '')

            //Properties for IBUserService
            booleanParam(name: '[service]IBUserService/System.properties', defaultValue: false, description: '')
            booleanParam(name: '[service]IBUserService/Debug.properties', defaultValue: false, description: '')
            booleanParam(name: '[service]IBUserService/EmailQueue.properties', defaultValue: false, description: '')

            //Properties for IBAdministrationService
            booleanParam(name: '[service]IBAdministrationService/System.properties', defaultValue: false, description: '')
            booleanParam(name: '[service]IBAdministrationService/Debug.properties', defaultValue: false, description: '')

            //Properties for IBTransferService
            booleanParam(name: '[service]IBTransferService/System.properties', defaultValue: false, description: '')
            booleanParam(name: '[service]IBTransferService/Debug.properties', defaultValue: false, description: '')

            //Properties for IBConsumerRespApproval
            booleanParam(name: '[service]IBConsumerRespApproval/System.properties', defaultValue: false, description: '')
            booleanParam(name: '[service]IBConsumerRespApproval/Debug.properties', defaultValue: false, description: '')

            //Properties for IBConsumerRespRegistration
            booleanParam(name: '[service]IBConsumerRespRegistration/System.properties', defaultValue: false, description: '')
            booleanParam(name: '[service]IBConsumerRespRegistration/Debug.properties', defaultValue: false, description: '')

            booleanParam(name: 'DEPLOY_APP', defaultValue: false, description: '')
        }
    }

    stages {
        stage('Reload JenkinsFile') {
            options {
                timeout(time: 2, unit: 'MINUTES')
            }

            when {
                equals(expected: 'true', actual: "${Refresh}")
            }

            steps {
                println("URL Workspace: ${utilBCA.getURLWorkspace("$JOB_NAME")}")
                println("Jenkinsfile Reloaded!")
            }
        }

        stage('Running Build') {
            when {
                equals(expected: 'false', actual: "${Refresh}")
            }

            stages {
                stage('Clean Workspace') {
                    options {
                        timeout(time: 2, unit: 'MINUTES')
                    }

                    steps {
                        dir(WORKSPACE) {
                            script {
                                cleanWs()

                                println("URL Workspace: ${utilBCA.getURLWorkspace("$JOB_NAME")}")
                                utilBCA.createProjectProperties(
                                        projectName: "${PROJECT_NAME}", description: "${DESCRIPTION}"
                                )

                                writeDeploymentConfig()
                            }
                        }
                    }
                }

                stage('Create Persistent Checklist') {
                    options {
                        timeout(time: 2, unit: 'MINUTES')
                    }

                    steps {
                        dir(WORKSPACE) {
                            script {
                                parallel(
                                        'Deployment': { writeFileDeployment() },
                                        'Config APP': { writeFileConfigAPP() }
                                )
                            }
                        }
                    }
                }

                stage('Check Parameter Checklist') {
                    options {
                        timeout(time: 5, unit: 'MINUTES')
                    }

                    steps {
                        dir(WORKSPACE) {
                            script {
                                changesFileConfig = [
                                        [dest: 'changes-deployment.txt', src: 'temp-changes-deployment.txt'],
                                        [dest: 'changes-config-app.txt', src: 'temp-changes-config-app.txt']
                                ]
                                utilBCA.printEnvironment(changesFileConfig)
                            }
                        }
                    }
                }

                stage('Copy Config n Libraries') {
                    options {
                        timeout(time: 5, unit: 'MINUTES')
                    }

                    steps {
                        dir(WORKSPACE) {
                            script {
                                parallel(
                                        'Config': { copyConfig("${flavor}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

def writeFileDeployment() {
    writeFile file: 'var/temp-changes-deployment.txt', text: '''
#Deployment
JMS-Listener
IBUserService
IBAdministrationService
IBTransferService
IBConsumerRespApproval
IBConsumerRespRegistration
'''
}

def writeFileConfigAPP() {
    writeFile file: 'var/temp-changes-config-app.txt', text: '''
#Config APP
[service]EmailListener/System.properties
[service]EmailListener/Debug.properties
[service]IBUserService/System.properties
[service]IBUserService/Debug.properties
[service]IBUserService/EmailQueue.properties
[service]IBAdministrationService/System.properties
[service]IBAdministrationService/Debug.properties
[service]IBTransferService/System.properties
[service]IBTransferService/Debug.properties
[service]IBConsumerRespApproval/System.properties
[service]IBConsumerRespApproval/Debug.properties
[service]IBConsumerRespRegistration/System.properties
[service]IBConsumerRespRegistration/Debug.properties
'''
}

def copyConfig(flavor = "") {
    script {
        //APP CONFIG 'JMS' --->
        try {
            utilBCA.writeChangeConfigV2(
                    checklistFile: "var/changes-config-app-jms.txt",
                    toChangeCsv: 'C:/WORK_BCA/generate local config/JMS-ConfigPropertiesV2/config_mapping/CHANGES.csv'
            )

            utilBCA.generateConfigV2(
                    pathToConfig: 'C:/WORK_BCA/generate local config/JMS-ConfigPropertiesV2',
                    descriptorFileName: 'descriptor.json',
                    flavor: "${flavor}",
                    generateDestination: "${flavor}/CONFIG/JMS"
            )
        } catch (Exception ex) {
            println("Exception $ex")
            currentBuild.displayName = "#${BUILD_NUMBER} Failures (JMS-ConfigPropertiesV2)"
            currentBuild.result = 'FAILURE'
            currentBuild.description = "Error:: ${ex}"
        }

        //APP CONFIG - 'SVC' --->
        try {
            utilBCA.writeChangeConfigV2(
                    checklistFile: 'var/changes-config-app-svc.txt',
                    toChangeCsv: 'C:/WORK_BCA/generate local config/SVC-ConfigPropertiesV2/config_mapping/CHANGES.csv'
            )

            utilBCA.generateConfigV2(
                    pathToConfig: 'C:/WORK_BCA/generate local config/SVC-ConfigPropertiesV2',
                    descriptorFileName: 'descriptor.json',
                    flavor: "${flavor}",
                    generateDestination: "${flavor}/CONFIG/SVC"
            )

        } catch (Exception ex) {
            println("Exception $ex")
            currentBuild.displayName = "#${BUILD_NUMBER} Failures (WEB-ConfigPropertiesV2)"
            currentBuild.result = 'FAILURE'
            currentBuild.description = "Error:: ${ex}"
        }
    }
}

def writeDeploymentConfig() {
    writeFile file: 'var/deployment_descriptor.json', text: '''
{
    "environments": [
        {
            "name":"UAT",
            "node_groups": [
                {
                    "id": "app_service",
                    "nodes": ["APP/10.20.214.173_onluappie01", "APP/10.20.214.174_onluappie02"]
                }
            ],

            "deploy_paths":[
                {
                    "tag":"consumer_service",
                    "paths":[
                        { "node_id": "app_service", "path":"/bcaibank/app/ibank_csmr_uat1" }
                    ]
                },
                {
                    "tag":"svc",
                    "paths":[
                        { "node_id": "app_service", "path":"/bcaibank/app/ibank_svc_uat1" }
                    ]
                }
            ]
        },
        {
            "name": "PRODUCTION",
            "node_groups": [
                {
                    "id": "app_service",
                    "nodes": ["APP/10.16.51.196_oln1appconsie07", "APP/10.16.51.197_oln1appconsie08",
                              "APP/10.0.51.196_oln2appconsie07" , "APP/10.0.51.197_oln2appconsie08",
                              "APP/10.32.51.196_oln3appconsie07", "APP/10.32.51.197_oln3appconsie08" ]
                }
            ],
            "deploy_paths": [
                {
                    "tag": "consumer_service",
                    "paths": [
                        { "node_id": "app_service", "path": "/bcaibank/app/ibank_inter1_csmr"  }
                    ]
                },
                {
                    "tag": "svc",
                    "paths": [
                        { "node_id": "app_service",  "path": "/bcaibank/app/ibank_inter1_svc" }
                    ]
                }
            ]
        },
        {
            "name": "PILOT",
            "node_group": [
                {
                    "id": "app_service",
                    "nodes": [
                        "APP/10.16.51.196_oln1appconsie07", "APP/10.0.51.196_oln2appconsie07",
                        "APP/10.32.51.196_oln3appconsie07"
                    ]
                }
            ],
            "deploy_paths": [
                {
                    "tag": "consumer_service",
                    "paths": [
                        {"node_id": "app_service", "path": "/bcaibank/app/ibank_pilot1_csmr"}
                    ]
                },
                {
                    "tag": "svc",
                    "paths": [
                        {"node_id": "app_service", "path": "/bcaibank/app/ibank_pilot1_svc"}
                    ]
                }
            ]
        }
    ],

    "artifacts": [
        {
            "tag":"consumer_service",
            "search_paths":[
                "/appjenkins/data/jenkins/workspace/klikBCAIndividu/BUILD/Services/**"
            ],
            "file_qualifiers": ["*.jar", "*.war", "*.ear"],
            "searchable_artifacts":[
                {
                    "checklist_name":"IBConsumerRespApproval",
                    "products":[
                        { "build_name":"IBConsumer-RespApproval.war", "postfix_folder": "ibconsumer_resp_approval/deployment$ibcsmr_user_approval_env_directory" }
                    ]
                },
                {
                    "checklist_name":"IBConsumerRespRegistration",
                    "products":[
                        { "build_name":"IBConsumer-RespRegistration.war", "postfix_folder": "ibconsumer_resp_registration/deployment$ibcsmr_resp_regis_env_directory" }
                    ]
                }
            ]
        },
        {
            "tag":"svc",
            "search_paths":[
                "/appjenkins/data/jenkins/workspace/klikBCAIndividu/BUILD/Services/**"
            ],
            "file_qualifiers": ["*.jar", "*.war", "*.ear"],
            "searchable_artifacts":[
                {
                    "checklist_name":"IBUserService",
                    "products":[
                        { "build_name":"IBService-User.war", "postfix_folder": "ibservice_user/deployment$ibservice_user_env_directory" }
                    ]
                },
                {
                    "checklist_name":"IBAdministrationService",
                    "products":[
                        { "build_name":"IBService-Administration.war", "postfix_folder": "ibservice_administration/deployment$ibservice_administration_env_directory" }
                    ]
                },
                {
                    "checklist_name":"IBTransferService",
                    "products":[
                        { "build_name":"IBService-Transfer.war", "postfix_folder": "ibservice_transfer/deployment$ibservice_transfer_env_directory" }
                    ]
                }
            ]
        }
    ]
}
'''
}