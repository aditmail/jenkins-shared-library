def call(Map config = [:], Closure body) {
    node {
        git url: "urls..."

        stage("test") {

        }

        stage("deploy") {
            if (config.deploy) {
                //....
            }
        }

        body()
    }
}
