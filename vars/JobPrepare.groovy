#!/usr/bin/env groovy

def call() {
    pipeline {
        //agent any
        agent {
            label 'Windows_Node'
        }

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

            stage("Running Build") {
                when {
                    expression {
                        return "${Refresh}" == "false"
                    }
                }

                stages {
                    stage("Clean Workspace") {
                        steps {
                            dir(WORKSPACE) {
                                script {
                                    cleanWs()

                                    println("URL Workspace: ${utilBCA.getURLWorkspace("$JOB_NAME")}")
                                    utilBCA.createProjectProperties(projectName: "${PROJECT_NAME}", description: "${DESCRIPTION}")

                                    writeDeploymentConfig()
                                }
                            }
                        }
                    }

                    stage("Create Persistent Checklist") {
                        steps {
                            dir(WORKSPACE) {
                                script {
                                    parallel(
                                            "Deployment": {
                                                writeFileDeployment()
                                            },
                                            "Config APP": {
                                                writeFileConfigAPP()
                                            },
                                            "Config WEB": {
                                                writeFileConfigWEB()
                                            }
                                    )
                                }
                            }
                        }
                    }

                    stage("Check Parameter Checklist") {
                        steps {
                            dir(WORKSPACE) {
                                script {
                                    changesFileConfig = [
                                            [dest: 'changes-deployment.txt', src: 'temp-changes-deployment.txt'],
                                            [dest: 'changes-config-app.txt', src: 'temp-changes-config-app.txt'],
                                            [dest: 'changes-config-web.txt', src: 'temp-changes-config-web.txt']
                                    ]
                                    //Skipped since there's error exception occur
                                    //cannot run program "nohup": CreateProcess error=2, The system cannot find the file specified
                                    utilBCA.printEnvironment(changesFileConfig)

                                    //Print env for windows
                                    //echo bat(returnStdout: true, script: 'set')

                                    /*PATH_PRINT_ENV = "var/printenv.txt"
                                    bat label: 'PrintEnv', script:"""
                                        del /s /q "\${PATH_PRINT_ENV}"
                                        set >> "${PATH_PRINT_ENV}"
                                        
                                    """*/
                                }
                            }
                        }
                    }

                    stage("Copy Config n Libraries") {
                        steps {
                            dir(WORKSPACE) {
                                script {
                                    parallel(
                                            "Config": {

                                            },
                                            "Library": {

                                            }
                                    )
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

def writeDeploymentConfig() {
    writeFile file: 'var/deployment_descriptor.json', text: '''
{
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
