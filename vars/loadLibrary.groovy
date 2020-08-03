def call(Map scmParams) {
    library(
            identifier: 'jenkins-shared-library@master',
            retriever: modernSCM([
                    $class       : 'GitSCMSource',
                    credentialsId: 'b5656ab7-b7ed-4c02-9833-9af6877e2b9e',
                    remote       : 'https://github.com/aditmail/jenkins-shared-library',
                    traits       : [gitBranchDiscovery()]
            ])
    )
}
