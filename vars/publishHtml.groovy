def call(Map params) {
    publishHTML target: [
            allowMissing         : false,
            alwaysLinkToLastBuild: false,
            keepAll              : true,
            reportDir            : "${params.reportDir}",
            reportFiles          : "${params.reportFile}",
            reportName           : "${params.reportName}"
    ]
}