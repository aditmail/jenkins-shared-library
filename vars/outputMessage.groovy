import main.groovy.example.Constant

def call(String message) {
    echo message
}

def call(Map config = [:]) {
    def dateTime = new Date().format('dd/MM/yyyy HH:mm:ss')

    def outputType = config.outputType
    if (outputType == "startStage") {
        switch (config.level) {
            case 'init':
                echo "$Constant.INIT_STAGE Stage Running at $dateTime"
                break
            case 'build':
                echo "$Constant.BUILD_STAGE Stage Running at $dateTime"
                break
            case 'unit-test':
                echo "$Constant.UNIT_TEST_STAGE Stage Running at $dateTime"
                break
            case 'post-stage':
                echo "$Constant.POST_STAGE ${(config.params != null ? "with Status: ${config.params}" : "")}"
                break

            default: break

        }
    } else if (outputType == "build") {
        switch (config.status) {
            case 'aborted':
                currentBuild.result = 'ABORTED'
                def condition = (config.params != null ? " - Because: ${config.params}" : "")

                echo "${Constant.BUILD_ABORTED_MESSAGE} at $dateTime $condition}"
                break

            default: break
        }
    }
}