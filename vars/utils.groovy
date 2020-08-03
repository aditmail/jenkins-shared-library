import main.groovy.example.Constant

import java.util.regex.Pattern

def call(Map config = [:]) {
    switch (config.utilities) {
        case 'dateTime':
            /* Get TimeStamp */
            return new Date().format('dd/MM/yyyy HH:mm:ss')
            break

        case 'inputEmail':
            return input(
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

        case 'emailPattern':
            String email = config.params
            String regex = Constant.EMAIL_REGEX

            Pattern pattern = Pattern.compile(regex)
            return pattern.matcher(email).matches()
            break

        default: break
    }
}

/** Generate JUnit Test Result **/
/*def generateJUnit(location = "") {
    echo "$Constant.GENERATE_JUNIT_REPORT"
    junit testResults: location
}*/

/** Generate Checkstyle Reports **/
/*def generateCheckstyle(location = "") {
    echo "$Constant.GENERATE_CHECKSTYLE_REPORT"
    recordIssues(
            tools: [
                    checkStyle(pattern: location)
            ]
    )
}*/

/** Get TimeStamp **/
static String dateTime() {
    return new Date().format('dd/MM/yyyy HH:mm:ss')
}

/** Set Email Address **/
def inputEmail() {
    return input(
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
static def Boolean validateEmail(emailAddress = "") {
    return (emailAddress == null || emailAddress == "" || emailAddress == "example@email.com")
}

/** check Email Pattern **/
static def Boolean validateEmailPattern(emailAddress = "") {
    String regex = Constant.EMAIL_REGEX
    Pattern pattern = Pattern.compile(regex)

    return pattern.matcher(emailAddress).matches()
}
