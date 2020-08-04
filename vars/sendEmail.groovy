import main.groovy.example.Constant

def call(Map config = [:]) {
    def status = config.status
    def bodyMessage
    def subjectMessage

    if (status != null) {
        echo "${Constant.SENDING_EMAIL_TO} ${config.emailTo}"

        if (status == 'Success') {
            bodyMessage = Constant.BODY_EMAIL_SUCCESS_MSG
            subjectMessage = Constant.SUBJECT_EMAIL_SUCCESS_MSG
        } else {
            bodyMessage = Constant.BODY_EMAIL_FAILED_MSG
            subjectMessage = Constant.SUBJECT_EMAIL_FAILED_MSG
        }

        //This is Default Models...
        /*mail([
                body   : "${bodyMessage} ${env.BUILD_URL}\n\nBuild Number\t\t: ${env.BUILD_NUMBER}\nBuild Tag\t\t: ${env.BUILD_TAG}",
                from   : "aditya@jenkins.com",
                subject: "${subjectMessage} ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                to     : "${config.emailTo}"
        ])*/

        emailext([
                to                : "${config.emailTo}",
                subject           : "${subjectMessage} ${env.JOB_NAME} Build #${env.BUILD_NUMBER}",
                body              : "${bodyMessage} ${env.BUILD_URL}\n\nBuild Number\t\t: ${env.BUILD_NUMBER}\nBuild Tag\t\t: ${env.BUILD_TAG}",
                compressLog       : true,
                attachLog         : true,
                //recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']], //This use to Linking email send To..?
                //attachmentsPattern: "**/build/test-results/test/TEST-*.xml",
                attachmentsPattern: "**/build/reports/tests/test/*.html"
        ])
    }
}