def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        unstash 'sslCert'
        env.SSL_CERT_DIR= "${workspace}/.chef/trusted_certs/"
        script {
            envJson = sh (
                script: 'knife environment show dbright-dev -F j',
                returnStdout: true
            ).trim()
        }
        def envData = readJSON text: "${envJson}"
        for (element in envData.cookbook_versions) {
            echo "${element.key} ${element.value}"
            trimmedVer = element.value.substring(2)
            cookbookJson = sh (
                script: "knife cookbook show ${element.key} ${trimmedVer} -F j",
                returnStdout: true
            ).trim()
            def cookbookData = readJSON text: "${cookbookJson}"
            echo cookbookData
            // for (sourceURL in cookbookData.metadata.source_url) {
            //     echo "${sourceURL.value}"
            // }
        }
    }
}