def call(Map params) {
    switch (params.method) {
        case 'windows':
            bat "${params.command}"
            break
        case 'linux':
            sh "${params.command}"
            break
        default: break
    }
}
