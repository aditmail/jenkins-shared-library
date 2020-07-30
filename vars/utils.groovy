import main.groovy.example.Constant

def call(Map config = [:]) {
    switch (config.utilities) {
        case 'dateTime':
            /* Get TimeStamp */
            return new Date().format('dd/MM/yyyy HH:mm:ss')
            break

        case 'inputEmail':
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
            break

        case 'validateEmail':
            /* Check Email Validation */
            def emailAddress = config.params
            return (emailAddress == null || emailAddress == "" || emailAddress == "example@email.com")
            break

        default: break
    }
}

///** Get TimeStamp **/
//static def String dateTime() {
//    return new Date().format('dd/MM/yyyy HH:mm:ss')
//}
//
///** Set Email Address **/
//def inputEmail() {
//    input(
//            message: "${Constant.INPUT_EMAIL_MSG}",
//            ok: "$Constant.INSERT_BTN",
//            parameters: [
//                    string(
//                            defaultValue: "$Constant.EMAIL_EXAMPLE",
//                            description: "$Constant.DESCRIPTION_EMAIL_MSG",
//                            name: 'inputEmailTo',
//                            trim: true
//                    )
//            ]
//    )
//}
//
///** Check Email Validation **/
//static def Boolean validateEmail(emailAddress) {
//    return (emailAddress == null || emailAddress == "" || emailAddress == "example@email.com")
//}
