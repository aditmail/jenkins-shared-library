import main.groovy.example.Constant

static def call(Map config = [:]) {
    switch (config.utilities) {
        case 'dateTime':
            dateTime()
            break

        case 'inputEmail':
            inputEmail()
            break

        case 'validateEmail':
            validateEmail(config.params = null)
            break
        default: break
    }

}

/** Get TimeStamp **/
static def String dateTime() {
    return new Date().format('dd/MM/yyyy HH:mm:ss')
}

/** Set Email Address **/
def inputEmail() {
    input(
            message: "${Constant.INPUT_EMAIL_MSG}",
            ok: "$Constant.INSERT_BTN",
            parameters: [
                    string(
                            defaultValue: "$Constant.EMAIL_EXAMPLE",
                            description: "$Constant.DESCRIPTION_EMAIL_MSG",
                            name: 'inputEmailTo',
                            trim: true
                    )
            ]
    )
}

/** Check Email Validation **/
static def Boolean validateEmail(emailAddress) {
    return (emailAddress == null || emailAddress == "" || emailAddress == "example@email.com")
}
