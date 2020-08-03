package main.groovy.example

class Constant {
    /**
     * Message Task
     * */
    static final String SLACK_MSG = "Sending Slack"
    static final String EMAIL_MSG = "Sending Email"
    static final String INPUT_EMAIL_MSG = "Seems like you haven\'t input Email before.. Please set first!"
    static final String DESCRIPTION_EMAIL_MSG = """<p style="color:red;">*Required</p><h5>Insert <b style="color:blue">Email Address</b> to Send Notification Email</h5>"""
    static final String BUILD_ABORTED_MESSAGE = "Build Job Aborted"

    static final String BODY_EMAIL_SUCCESS_MSG = "Test Successfully Build at thisss:\n"
    static final String SUBJECT_EMAIL_SUCCESS_MSG = "Success in Build Jenkins: :D"

    static final String BODY_EMAIL_FAILED_MSG = "Test Failed Occurs\nCheck Console Output at below to see Detail\n"
    static final String SUBJECT_EMAIL_FAILED_MSG = "Failure in Build Jenkins: :["

    static final String NO_EMAIL_SET_MSG = "Seems like you haven\'t set Email yet, Requesting Input.."
    static final String INVALID_EMAIL_SET_MSG = "Seems like you set Invalid Email, Requesting New Input.."

    /**
     * Simple Task
     * */
    static final String INSERT_BTN = "Insert"
    static final String EMAIL_EXAMPLE = "example@email.com"

    static final String INIT_STAGE = "Initialize"
    static final String BUILD_STAGE = "Build"
    static final String UNIT_TEST_STAGE = "Unit-Test"
    static final String POST_STAGE = "Build Job Done with"
    static final String RUNNING_AT = "Stage Running At"



    /**
     * Pattern -- Regex
     */
    static final String EMAIL_REGEX = "^[\\w!#\$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#\$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}\$"

}
