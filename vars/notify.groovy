import main.groovy.example.Constant

def call(Map config = [:]) {
    if (config.type == "slack") {
        echo Constant.SLACK_MSG
        echo config.message
    } else {
        echo Constant.EMAIL_MSG
        echo config.message
    }
}
