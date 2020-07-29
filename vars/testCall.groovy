def call(Map config = [:], Closure body) {
    node {
        //git url: "urls..."

        stage("test") {
            echo "test"
        }

        stage("deploy") {
            if (config.deploy) {
                echo "config ${config.deploy}"
            }else{
                echo "not deploy.."
            }
        }

        body()
    }
}
