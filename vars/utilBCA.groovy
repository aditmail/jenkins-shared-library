def getURLWorkspace(
        path,
        node = "master",
        workspace = "workspace",
        outside = false) {

    WS_BUILD_NUMBER = "27"
    try {
        println("getURLWorkspace - path : ${path}")
        println("getURLWorkspace - node : ${node}")
        println("getURLWorkspace - workspace : ${workspace}")

        node = node.trim()
    } catch (Exception e) {
        e.printStackTrace()
        node = node.trim()
    }

    if (node.isEmpty()) node = "${NODE_NAME}"

    if (node == "master") {
        //return getURLCustomWorkspace() + WS_BUILD_NUMBER + "/execution/node/9/ws/jenkins/${workspace}/${path}"
        return WORKSPACE
                .replace(" ", "%20")
                .replace("%2F", "%252F")
    } else if (node.startsWith("LandingDeploy")) {
        return getURLCustomWorkspace() + WS_BUILD_NUMBER + "/execution/node/9/ws/LandingDeploy/${path}"
                .replace(" ", "%20")
                .replace("%2F", "%252F")
    } else if (node.startsWith("EXECUTOR-KP3BLD02-")) {
        return getURLCustomWorkspace() + WS_BUILD_NUMBER + "/execution/node/9/ws/${path}"
                .replace(" ", "%20")
                .replace("%2F", "%252F")
    } else if (node.startsWith("EXECUTOR-P090DH102A-")) {
        return getURLCustomWorkspace() + WS_BUILD_NUMBER + "/execution/node/19/ws/${path}"
                .replace(" ", "%20")
                .replace("%2F", "%252F")
    } else if (node.startsWith("EXECUTOR-KP1BLD01N-")) {
        return getURLCustomWorkspace() + WS_BUILD_NUMBER + "/execution/node/29/ws/${path}"
                .replace(" ", "%20")
                .replace("%2F", "%252F")
    } else {
        if (outside)
            path = "${workspace}/${path}"
        else
            path = "${node}/${workspace}/${path}"

        return getURLCustomWorkspace() + WS_BUILD_NUMBER + "/execution/node/19/ws/${path}"
                .replace(" ", "%20")
                .replace("%2F", "%252F")
    }
}

static def getURLCustomWorkspace() {
    return "localhost:8080/job/Repository/job/CustomWorkspace/"
}

def createProjectProperties(Map properties = [:]) {
    /**
     * Map -> projectName = Name of the Project
     * Map -> description = Description of the Project
     * */

    try {
        FILENAME = "var/project.properties"

        File projectProps = new File("${WORKSPACE}/${FILENAME}")
        new File(projectProps.getParent()).mkdirs()

        projectProps.write('')
        projectProps.append("projectName=${properties.projectName} \n")
        projectProps.append("description=${properties.description} \n")
    } catch (Exception e) {
        e.printStackTrace()
    }
}

def printEnvironment(changes = []) {
    try {
        PATH_PRINT_ENV = "var/printenv.txt"

        //Error using LabelledShell... nohup?
        /*labelledShell label: "printEnvironment", script: """
            JENKINS_JOB = ${JENKINS_HOME}/jobs
            
            echo "JOB_NAME: ${JOB_NAME}"
            echo "JENKINS_JOB: \${JENKINS_JOB}"
            
            FILE_PATH = "${WORKSPACE}/var/BUILD_URL_CONFIG.txt"
            TEMP_JOB = job/
            TEMP_JOB_BUILD = builds/
            
            REM #Remove Recursive files in Windows Style
            del /s /q "\${FILE_PATH}"
            
            REM #Inserting JOB_URL to BUILD_URL_CONFIG.txt?
            echo "$JOB_URL" >> "\${FILE_PATH}"
        
            REM #concantenate in Windows Style
            ENV_VAR = `type "\${FILE_PATH}"`
        
            del /s /q "\${PATH_PRINT_ENV}"
            set >> "${PATH_PRINT_ENV}"
        """*/

        bat label: "printEnvironment", script: """
            set JENKINS_JOB=${JENKINS_HOME}/jobs
            
            echo "JOB_NAME: ${JOB_NAME}"
            echo "JENKINS_JOB: %JENKINS_JOB%"
            
            set FILE_PATH=${WORKSPACE}/var/BUILD_URL_CONFIG.txt
            set TEMP_JOB=job/
            set TEMP_JOB_BUILD=builds/
            
            del /s /q "%FILE_PATH%"
            echo "$JOB_URL">> "%FILE_PATH%"

            set ENV_VAR = `type "%FILE_PATH%"`
            
            del /s /q %PATH_PRINT_ENV%
            set>>${PATH_PRINT_ENV}
        """

        for (int i = 0; i < changes.size(); i++) {
            //Running Java File..
            //Since we don't have access to it.. commented
            /*bat label: "ReadFileProperties #${i}", script: """
                java -jar "${EXECUTABLE}/library/jar/JenkinsUtils.jar" \
                "ReadFileProperties" \
                "${PATH_PRINT_ENV}" \
                "var/${changes[i].src}" \
                "var/${changes[i].dest}"  
            """*/

            bat label: "ReadFileProperties BCA Model", script: """
            java -cp "C:/Users/Adit/Documents/CI-CD/jenkins/library/JenkinsLibs/JenkinsUtil.jar" \
                com.bca.jenkins.util.RunFunc \
                "ReadFileProperties" \
                "${PATH_PRINT_ENV}" \
                "var/${changes[i].dest}" \
                "var/${changes[i].src}"
            """
        }
    } catch (Exception e) {
        e.printStackTrace()
        println("Error: " + e.message.toString())
    }
}

def writeChangeConfigV2(Map path = [:]) {
    bat label: 'WriteChangeConfigV2', script: """
    java -cp "C:/Users/Adit/Documents/CI-CD/jenkins/library/JenkinsLibs/GeneratorV2.jar" \
        com.bca.jenkins.GeneratorV2.ChangesUpdater \
        "${path.checklistFile}" \
        "${path.toChangeCsv}"
    """
}

def generateConfigV2(Map configs = [:]) {
    bat label: 'GenerateChangeConfigV2', script: """
    java -cp "C:/Users/Adit/Documents/CI-CD/jenkins/library/JenkinsLibs/GeneratorV2.jar" \
        com.bca.jenkins.GeneratorV2.ConfigGeneratorV2 \
        "${configs.pathToConfig}" \
        "${configs.descriptorFileName}" \
        "${configs.flavor}" \
        "${configs.generateDestination}" 
    """
}