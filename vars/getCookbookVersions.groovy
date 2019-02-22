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
        def sourceURLs = []
        def cookbooks = []
        def envData = readJSON text: "${envJson}"
        echo "Cookbooks + versions that are pinned in your environment:"
        for (element in envData.cookbook_versions) {
            echo "PINNED VERSION: ${element.key} ${element.value}"
            def trimmedVer = element.value.substring(2)
            script {
                def cookbookHighestVersionChef = sh (
                    script: "knife cookbook show ${element.key} | awk '{print \$2;}'",
                    returnStdout: true
                ).trim()
            }
            echo "UNPINNED HIGHEST: ${element.key} ${cookbookHighestVersionChef}"
            cookbooks << "${element.key}:${cookbookHighestVersionChef}"
        }
        for (cookbook in cookbooks) {
            echo cookbook
            def (v, z) = cookbook.split(':')
            script {
                def cookbookJson = sh (
                    script: "knife cookbook show ${v} ${z} -F j",
                    returnStdout: true
                ).trim()
            }
            def cookbookData = readJSON text: "${cookbookJson}"
            def sourceURL = cookbookData.metadata.source_url
            sourceURLs << cookbookData.metadata.source_url
        }
        return sourceURLs
    }
}