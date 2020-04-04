node {
    def app

    def registry = "docker.cubyte.org"
    def imageName = "${registry}/pschichtel/yearanalyser:latest"

    stage('Clone repository') {
        checkout scm
    }

    stage('Build image') {
        app = docker.build(imageName, "--pull .")
    }

    stage('Push image') {
        def tag = sh(returnStdout: true, script: "git tag --contains").trim()

        docker.withRegistry("https://${registry}", 'deployment-account') {
            app.push("latest")
            if (tag != "") {
                app.push(tag)
            } else {
                tag = 'latest'
            }
        }

        withCredentials([string(credentialsId: 'yearanalyser-trigger-token', variable: 'token')]) {
            def projectId = 221
            def triggerUrl = "https://git.cubyte.org/api/v4/projects/${projectId}/trigger/pipeline"
            def curlTrigger = "curl -X POST -F 'token=${token}' -F 'ref=master' -F 'variables[IMAGE_NAME]=${imageName}:${tag}' ${triggerUrl}"

            sh curlTrigger
        }
    }
}
