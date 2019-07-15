node {
    def app

    stage('Clone repository') {
        checkout scm
    }

    stage('Build image') {
        app = docker.build("docker.cubyte.org/pschichtel/mailmanager")
    }

    stage('Push image') {
        def tag = sh(returnStdout: true, script: "git tag --contains").trim()

        docker.withRegistry('https://docker.cubyte.org', 'deployment-account') {
            app.push("latest")
            if (tag != "") {
                app.push(tag)
            }
        }
    }
}