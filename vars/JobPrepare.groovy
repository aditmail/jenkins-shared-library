#!/usr/bin/env groovy

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
            RUN_UAT = true
            APPLICATION = 'klikBCAIndividu'
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
            booleanParam(
                    name: 'TransitiveDependencies', defaultValue: true,
                    description: 'If "thick" all Transitive Dependencies  will include into library')

            //Model Build
            choice(choices: ['UAT', 'INTRA', 'INTER', 'PILOT', 'PRODUCTION'], description: 'Select Build Flavor', name: 'flavor')

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
                                            'Config APP': { writeFileConfigAPP() },
                                            'Config WEB': { writeFileConfigWEB() }
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
                                            [dest: 'changes-config-app.txt', src: 'temp-changes-config-app.txt'],
                                            [dest: 'changes-config-web.txt', src: 'temp-changes-config-web.txt']
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

                    stage('Validate Config Mapping'){
                        options{
                            timeout(time: 2, unit: 'MINUTES')
                        }

                        steps{
                            dir(WORKSPACE){
                                script {
                                    bat label: 'Copy Deployment', script: """
                                    java -cp "C:/Users/Adit/Documents/CI-CD/jenkins/library/jar/JenkinsUtilities.jar" \
                                    com.jenkins.util.checker.ConfigValidator \
                                    "${flavor}" \
                                    "${WORKSPACE}/PILOT/CONFIG/APP" \
                                    "${WORKSPACE}/var/changes-config-app.txt" \
                                    "${WORKSPACE}/var"       
                                    """
                                }
                            }
                        }
                    }

                    stage("Copy Deployment") {
                        options {
                            timeout(time: 5, unit: 'MINUTES')
                        }

                        steps {
                            dir(WORKSPACE) {
                                script {
                                    bat label: 'Copy Deployment', script: """
                                    java -cp "C:/Users/Adit/Documents/CI-CD/jenkins/library/JenkinsLibs/GeneratorV2.jar" \
                                    com.bca.jenkins.GeneratorV2.DeploymentGeneratorV2 \
                                    -c "${WORKSPACE}/var/deployment_descriptor.json" \
                                    -f "UAT" \
                                    -t "${WORKSPACE}/DEPLOY" \
                                    -u "${flavor}" \
                                    -l "${WORKSPACE}/var/changes-deployment.txt"       
                                    """
                                }
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
IBank
IBSmartphone
IBankBatch
LimitValidator
'''
}

def writeFileConfigAPP() {
    writeFile file: 'var/temp-changes-config-app.txt', text: '''
#Config APP
[app]Debug.properties
[app]SMSMessages.properties
[app]System.properties
[app]EmailQueue.properties
'''
}

def writeFileConfigWEB() {
    writeFile file: 'var/temp-changes-config-web.txt', text: '''
#Config WEB
[web]ibank/Debug.properties
[web]ibank/cabang.xml
[web]ibank/kota.xml
[web]ibank/System.properties
[web]mklik/Debug.properties
[web]mklik/cabang.xml
[web]mklik/kota.xml
[web]mklik/System.properties
'''
}

def copyConfig(flavor = "") {
    script {
        try {
            utilBCA.writeChangeConfigV2(
                    checklistFile: "var/changes-config-app.txt ",
                    toChangeCsv: 'C:/WORK_BCA/generate local config/APP-ConfigPropertiesV2/config_mapping/CHANGES.csv'
            )

            utilBCA.generateConfigV2(
                    pathToConfig: "C:/WORK_BCA/generate local config/APP-ConfigPropertiesV2",
                    descriptorFileName: 'descriptor.json',
                    flavor: "${flavor}",
                    generateDestination: "${flavor}/CONFIG/APP"
            )
        } catch (Exception ex) {
            println("Exception $ex")
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
            "name": "UAT",
            "node_groups": [
                {
                    "#": "Define the nodes",
                    "id": "app",
                    "nodes": ["APP/10.20.213.190_BCADAPPUAT6A", "APP/10.20.213.191_BCADAPPUAT6B"]
                },
                {
                    "id": "web",
                    "nodes": ["WEB/$node_web"]
                }
            ],

            "deploy_paths":[
                {
                    "tag": "app_deployment",
                    "paths": [
                        {
                            "node_id": "app",
                            "path": "/bcaibank/app/ibank_uat_1/deployment/ibank12c_uat_1_cluster_$instance"
                        }
                    ]
                },
                {
                    "tag": "lib_app",
                    "paths": [
                        {
                            "node_id": "app",
                            "path": "/bcaibank/app/ibank_uat_1/lib"
                        }
                    ]
                },

                {
                    "tag": "web_deployment",
                    "paths": [
                        {
                            "node_id": "web",
                            "path": "/bcaibank/app/ibankweb_uat1/deployment/ibankweb12c_uat1_web_$instance"
                        }
                    ]
                },
                {
                    "tag": "mklik_deployment",
                    "paths": [
                        {
                            "node_id": "web",
                            "path": "/bcaibank/app/ibankweb_uat1/deployment/ibankweb12c_uat1_mklik_$instance"
                        }
                    ]
                },
                {
                    "tag": "lib_web",
                    "paths": [
                        {
                            "node_id": "web",
                            "path": "/bcaibank/app/ibankweb_uat1/lib"
                        }
                    ]
                }
            ]
        },
        {
            "name": "INTER",
            "node_groups": [
                {
                    "id": "app_mbca",
                    "nodes": [ "APP/10.16.50.32_BCA1APP1", "APP/10.16.50.33_BCA1APP4" ]
                },
                {
                    "id": "app_wsa2",
                    "nodes": [ "APP/10.0.50.36_BCA2APP1", "APP/10.0.50.37_BCA2APP4" ]
                },
                {
                    "id": "app_grha",
                    "nodes": [ "APP/10.32.50.36_BCA3APP1", "APP/10.32.50.37_BCA3APP4" ]
                },

                {
                    "id": "web_mbca",
                    "nodes": [ "WEB/10.16.42.128_BCA1WIBI01", "WEB/10.16.42.129_BCA1WIBI02" ]
                },
                {
                    "id": "web_wsa2",
                    "nodes": [ "WEB/10.0.42.128_BCA2WIBI01", "WEB/10.0.42.129_BCA2WIBI02" ]
                },
                {
                    "id": "web_grha",
                    "nodes": [ "WEB/10.32.42.128_BCA3WIBI01", "WEB/10.32.42.129_BCA3WIBI02" ]
                }
            ],
            "deploy_paths":[
                {
                    "tag": "app_deployment",
                    "paths": [
                        {
                            "node_id": "app_mbca",
                            "path": "/bcaibank/app/kp1_ibank_inter12c_1/deployment/12.2.1"
                        },
                        {
                            "node_id": "app_wsa2",
                            "path": "/bcaibank/app/kp2_ibank_inter12c_1/deployment/12.2.1"
                        },
                        {
                            "node_id": "app_grha",
                            "path": "/bcaibank/app/kp3_ibank_inter12c_1/deployment/12.2.1"
                        }
                    ]
                },
                {
                    "tag": "lib_app",
                    "paths": [
                        {
                            "node_id": "app_mbca",
                            "path": "/bcaibank/app/kp1_ibank_inter12c_1/lib"
                        },
                        {
                            "node_id": "app_wsa2",
                            "path": "/bcaibank/app/kp2_ibank_inter12c_1/lib"
                        },
                        {
                            "node_id": "app_grha",
                            "path": "/bcaibank/app/kp3_ibank_inter12c_1/lib"
                        }
                    ]
                },

                {
                    "tag": "web_deployment",
                    "paths": [
                        {
                            "node_id": "web_mbca",
                            "path": "/bcaibank/app/kp1_ibank_inter12c_1/deployment/kp1_ibank_inter1/12.2.1"
                        },
                        {
                            "node_id": "web_wsa2",
                            "path": "/bcaibank/app/kp2_ibank_inter12c_1/deployment/kp2_ibank_inter1/12.2.1"
                        },
                        {
                            "node_id": "web_grha",
                            "path": "/bcaibank/app/kp3_ibank_inter12c_1/deployment/kp3_ibank_inter1/12.2.1"
                        }
                    ]
                },
                {
                    "tag": "mklik_deployment",
                    "paths": [
                        {
                            "node_id": "web_mbca",
                            "path": "/bcaibank/app/kp1_ibank_inter12c_1/deployment/kp1_mklik_inter1/12.2.1"
                        },
                        {
                            "node_id": "web_wsa2",
                            "path": "/bcaibank/app/kp2_ibank_inter12c_1/deployment/kp2_mklik_inter1/12.2.1"
                        },
                        {
                            "node_id": "web_grha",
                            "path": "/bcaibank/app/kp3_ibank_inter12c_1/deployment/kp3_mklik_inter1/12.2.1"
                        }
                    ]
                },
                {
                    "tag": "lib_web",
                    "paths": [
                        {
                            "node_id": "web_mbca",
                            "path": "/bcaibank/app/kp1_ibank_inter12c_1/lib"
                        },
                        {
                            "node_id": "web_wsa2",
                            "path": "/bcaibank/app/kp2_ibank_inter12c_1/lib"
                        },
                        {
                            "node_id": "web_grha",
                            "path": "/bcaibank/app/kp3_ibank_inter12c_1/lib"
                        }
                    ]
                }
            ]
        },
        {
            "name": "INTRA",
            "node_groups": [
                {
                    "id": "app_mbca",
                    "nodes": ["APP/10.16.50.32_BCA1APP1", "APP/10.0.50.37_BCA1APP4"]
                },
                {
                    "id": "app_wsa2",
                    "nodes": ["APP/10.0.50.36_BCA2APP1", "APP/10.0.50.37_BCA2APP4"]
                },
                {
                    "id": "app_grha",
                    "nodes": ["APP/10.32.50.36_BCA3APP1", "APP/10.32.50.37_BCA3APP4"]
                },
                {
                    "id": "web_mbca",
                    "nodes": ["WEB/10.16.42.128_BCA1WIBI01"]
                },
                {
                    "id": "web_wsa2",
                    "nodes": ["WEB/10.0.42.128_BCA2WIBI01"]
                },
                {
                    "id": "web_grha",
                    "nodes": ["WEB/10.32.42.128_BCA3WIBI01"]
                }
            ],
            "deploy_paths": [
                {
                    "tag": "app_deployment",
                    "path": [
                        {
                            "node_id": "app_mbca",
                            "path": "/bcaibank/app/ibank_intra12c_1/deployment/12.2.1"
                        },
                        {
                            "node_id": "app_wsa2",
                            "path": "/bcaibank/app/ibank_intra12c_1/deployment/12.2.1"
                        },
                        {
                            "node_id": "app_grha",
                            "path": "/bcaibank/app/ibank_intra12c_1/deployment/12.2.1"
                        }
                    ]
                },
                {
                    "tag": "lib_app",
                    "paths": [
                        {
                            "node_id": "app_mbca",
                            "path": "/bcaibank/app/ibank_intra12c_1/lib"
                        },
                        {
                            "node_id": "app_wsa2",
                            "path": "/bcaibank/app/ibank_intra12c_1/lib"
                        },
                        {
                            "node_id": "app_grha",
                            "path": "/bcaibank/app/ibank_intra12c_1/lib"
                        }
                    ]
                },
                {
                    "tag": "web_deployment",
                    "paths": [
                        {
                            "node_id": "web_mbca",
                            "path": "/bcaibank/app/kp1_ibank_intra12c_1/deployment/kp1_ibank_intra1"
                        },
                        {
                            "node_id": "web_wsa2",
                            "path": "/bcaibank/app/kp2_ibank_intra12c_1/deployment/kp2_ibank_intra1"
                        },
                        {
                            "node_id": "web_grha",
                            "path": "/bcaibank/app/kp3_ibank_intra12c_1/deployment/kp3_ibank_intra1"
                        }
                    ]
                },
                {
                    "tag": "lib_web",
                    "paths": [
                        {
                            "node_id": "web_mbca",
                            "path": "/bcaibank/app/kp1_ibank_intra12c_1/lib"
                        },
                        {
                            "node_id": "web_wsa2",
                            "path": "/bcaibank/app/kp2_ibank_intra12c_1/lib"
                        },
                        {
                             "node_id": "web_grha",
                             "path": "/bcaibank/app/kp3_ibank_intra12c_1/lib"
                        }
                  ]
                }
            ]
        },
        {
            "name": "PILOT",
            "node_groups": [
                {
                    "id": "app_mbca",
                    "nodes": ["APP/10.16.50.32-BCA1APP1"]
                },
                {
                    "id": "app_wsa2",
                    "nodes": ["APP/10.0.50.36-BCA2APP1"]
                },
                {
                    "id": "app_grha",
                    "nodes": ["APP/10.32.50.36-BCA3APP1"]
                },
                
                {
                    "id": "web_mbca",
                    "nodes": ["WEB/10.16.42.129-BCA1WIBI02"]
                },
                {
                    "id": "web_wsa2",
                    "nodes": ["WEB/10.0.42.129-BCA2WIBI02"]
                },
                {
                    "id": "web_grha",
                    "nodes": ["WEB/10.32.42.129-BCA3WIBI02"]
                }
            ],
            "deploy_paths": [
                {
                    "tag": "app_deployment",
                    "paths": [
                        {
                            "node_id": "app_mbca",
                            "path": "/bcaibank/app/ibank_pilot12c_1/deployment"
                        },
                        {
                            "node_id": "app_wsa2",
                            "path": "/bcaibank/app/ibank_pilot12c_1/deployment"
                        },
                        {
                            "node_id": "app_grha",
                            "path": "/bcaibank/app/ibank_pilot12c_1/deployment"
                        }
                    ]
                },
                {
                    "tag": "lib_app",
                    "paths": [
                        {
                            "node_id": "app_mbca",
                            "path": "/bcaibank/app/ibank_pilot12c_1/lib"
                        },
                        {
                            "node_id": "app_wsa2",
                            "path": "/bcaibank/app/ibank_pilot12c_1/lib"
                        },
                        {
                            "node_id": "app_grha",
                            "path": "/bcaibank/app/ibank_pilot12c_1/lib"
                        }
                    ]
                },
                {
                    "tag": "web_deployment",
                    "paths": [
                        {
                            "node_id": "web_mbca",
                            "path": "/bcaibank/app/kp1_ibank_pilot12c_1/deployment/ibank_pilot1/12.2.1"
                        },
                        {
                            "node_id": "web_wsa2",
                            "path": "/bcaibank/app/kp2_ibank_pilot12c_1/deployment/ibank_pilot1/12.2.1"
                        },
                        {
                            "node_id": "web_grha",
                            "path": "/bcaibank/app/kp3_ibank_pilot12c_1/deployment/ibank_pilot1/12.2.1"
                        }
                    ]
                },
                {
                    "tag": "mklik_deployment",
                    "paths": [
                        {
                            "node_id": "web_mbca",
                            "path": "/bcaibank/app/kp1_ibank_pilot12c_1/deployment/mklik_pilot1/12.2.1"
                        },
                        {
                            "node_id": "web_wsa2",
                            "path": "/bcaibank/app/kp2_ibank_pilot12c_1/deployment/mklik_pilot1/12.2.1"
                        },
                        {
                            "node_id": "web_grha",
                            "path": "/bcaibank/app/kp3_ibank_pilot12c_1/deployment/mklik_pilot1/12.2.1"
                        }
                    ]
                },
                {
                    "tag": "lib_web",
                    "paths": [
                        {
                            "node_id": "web_mbca",
                            "path": "/bcaibank/app/kp1_ibank_pilot12c_1/lib"
                        },
                        {
                            "node_id": "web_wsa2",
                            "path": "/bcaibank/app/kp2_ibank_pilot12c_1/lib"
                        },
                        {
                            "node_id": "web_grha",
                            "path": "/bcaibank/app/kp3_ibank_pilot12c_1/lib"
                        }
                    ]
                }
            ]
        }
    ],

    "artifacts": [
        {
            "tag":"app_deployment",
            "search_paths":[
                "/appjenkins/data/jenkins/workspace/klikBCAIndividu/BUILD/BACKEND/**"
            ],
            "file_qualifiers": ["*.jar", "*.war", "*.ear"],
            "searchable_artifacts":[
                {
                    "checklist_name":"IBank",
                    "products":[
                        { "build_name":"IBankApp.jar" }
                    ]
                }
            ]
        },
        {
            "tag":"web_deployment",
            "search_paths":[
                "/appjenkins/data/jenkins/workspace/klikBCAIndividu/BUILD/BACKEND/**"
            ],
            "file_qualifiers": ["*.jar", "*.war", "*.ear"],
            "searchable_artifacts":[
                {
                    "checklist_name":"IBank",
                    "products":[
                        { "build_name":"IBankWeb.war" }
                    ]
                }
            ]
        },
        {
            "tag":"mklik_deployment",
            "search_paths":[
                "/appjenkins/data/jenkins/workspace/klikBCAIndividu/BUILD/BACKEND/**"
            ],
            "file_qualifiers": ["*.jar", "*.war", "*.ear"],
            "searchable_artifacts":[
                {
                    "checklist_name": "IBSmartphone",
                    "products":[
                        {"build_name": "sp.war"}
                    ]
                }
            ]
        }
    ],

    "repositories": [
        {
            "url": "http://10.4.203.28:8081/artifactory/RepositoryDevOps",
            "repository_artifacts": [
                {
                    "tag": "lib_app",
                    "checklist_name": "LimitValidator",
                    "repository_path": "com.lib.IBank.12c:LimitValidator",
                    "artifact_name": "LimitValidator-2.0",
                    "version": "2.0",
                    "type": "zip",
                    "action": "unzip"
                },
                {
                    "tag": "lib_web",
                    "checklist_name": "LimitValidator",
                    "repository_path": "com.lib.IBank.12c:LimitValidator",
                    "artifact_name": "LimitValidator-2.0",
                    "version": "2.0",
                    "type": "zip",
                    "action": "unzip"
                }
            ]
        }
    ]
}
'''
}
