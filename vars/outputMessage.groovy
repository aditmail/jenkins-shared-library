import main.groovy.example.Constant

def call(Map config = [:]) {
    def outputType = config.outputType
    def dateTime = new Date().format('dd/MM/yyyy HH:mm:ss')

    if (outputType == "print") {
        echo config.message
    } else if (outputType == "start stage") {
        switch (config.level) {
            case 'init':
                echo "Initialize Stage Running at $dateTime"
                break


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