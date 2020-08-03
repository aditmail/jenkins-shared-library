import main.groovy.example.Constant

def call(String message) {
    echo message
}

def printMsg(status = "") {
    switch (status) {
        case 'no_email':
            echo "$Constant.NO_EMAIL_SET_MSG"
            break
        case 'invalid_email':
            echo "$Constant.INVALID_EMAIL_SET_MSG"
            break
        default:
            echo status
            break
    }

}

def call(Map config = [:]) {
    def dateTime = new Date().format('dd/MM/yyyy HH:mm:ss')

    def outputType = config.outputType
    if (outputType == "startStage") {
        switch (config.level) {
            case 'init':
                echo "$Constant.INIT_STAGE $Constant.RUNNING_AT $dateTime"
                break
            case 'build':
                echo "$Constant.BUILD_STAGE $Constant.RUNNING_AT $dateTime"
                break
            case 'unit-test':
                echo "$Constant.UNIT_TEST_STAGE $Constant.RUNNING_AT $dateTime"
                break
            case 'post-stage':
                echo "$Constant.POST_STAGE ${(config.params != null ? "Status: ${config.params}" : "")}"
                break

            default: break

        }
    } else if (outputType == "build") {
        switch (config.status) {
            case 'aborted':
                def condition = (config.params != null ? " - Because: ${config.params}" : "")
                echo "${Constant.BUILD_ABORTED_MESSAGE} at $dateTime $condition}"
                break

            default: break
        }
    }
}