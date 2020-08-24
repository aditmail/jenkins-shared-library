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

        labelledShell label: "printEnvironment", script: """
            JENKINS_JOB = ${JENKINS_HOME}/jobs
            
            echo "JOB_NAME: ${JOB_NAME}"
            echo "JENKINS_JOB: \${JENKINS_JOB}"
            
            #FILE_PATH = "${WORKSPACE}/var/BUILD_URL_CONFIG.txt"
            TEMP_JOB = job/
            TEMP_JOB_BUILD = builds/
            
            #Remove Recursive files in Windows Style
            #del /s /q "\${FILE_PATH}"
            
            #Inserting JOB_URL to BUILD_URL_CONFIG.txt?
            #echo "$JOB_URL" >> "\${FILE_PATH}"
        
            #concantenate in Windows Style
            #ENV_VAR = `type "\${FILE_PATH}"`
        
            del /s /q "\${PATH_PRINT_ENV}"
            printenv >> "${PATH_PRINT_ENV}"
        """

        for (int i = 0; i < changes.size(); i++) {
            println(changes[i])
        }
    } catch (Exception e) {
        e.printStackTrace()
        println("Error: " + e.message.toString())
    }
}