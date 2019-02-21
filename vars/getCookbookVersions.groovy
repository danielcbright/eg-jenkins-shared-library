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
        def cookbooks = [:]
        def envData = readJSON text: "${envJson}"
        for (element in envData.cookbook_versions) {
            //echo "${element.key} ${element.value}"
            trimmedVer = element.value.substring(2)
            cookbooks.put("${element.key}", "${trimmedVer}")
        }
        for (cookbook in cookbooks) {
            echo "${cookbook.key} ${cookbook.value}"
            script {
                cookbookJson = sh (
                    script: "knife cookbook show ${cookbook.key} ${cookbook.value} -F j",
                    returnStdout: true
                ).trim()
            }
            //def cookbookData = readJSON text: "${cookbookJson}"
            // for (sourceURL in cookbookData.metadata.source_url) {
            //     echo "${sourceURL.value}"
            // }
        }
    }
}