import main.groovy.example.Constant

/** Generate JUnit Test Result **/
def genJUnit(location = "") {
    echo "$Constant.GENERATE_JUNIT_REPORT"
    junit testResults: location
}

/** Generate Checkstyle Reports **/
def genCheckstyle(location = "") {
    echo "$Constant.GENERATE_CHECKSTYLE_REPORT"
    recordIssues(
            tools: [
                    checkStyle(pattern: location)
            ]
    )
}
