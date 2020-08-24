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
        return getURLCustomWorkspace() + WS_BUILD_NUMBER + "/execution/node/9/ws/jenkins/${workspace}/${path}"
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