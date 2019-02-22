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
        def pinnedCookbooks = []
        def unpinnedCookbooks = []
        def envData = readJSON text: "${envJson}"
        def cookbookHighestVersionChef = ""
        echo "Cookbooks + versions that are pinned in your environment:"
        for (element in envData.cookbook_versions) {
            echo "PINNED VERSION: ${element.key} ${element.value}"
            def trimmedVer = element.value.substring(2)
            pinnedCookbooks << "${element.key}:${trimmedVer}"
        }
        for (pinnedCookbook in pinnedCookbooks) {
            def (v, z) = pinnedCookbook.split(':')
            def highest = getHighestVersion("${v}")
            echo "UNPINNED HIGHEST: ${v} ${highest}"
            unpinnedCookbooks << "${v}:${highest}"
        }
        for (unpinnedCookbook in unpinnedCookbooks) {
            def (v, z) = unpinnedCookbook.split(':')
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
def getHighestVersion(String cookbook) {
    script {
        cookbookHighestVersionChef = sh (
            script: "knife cookbook show ${cookbook} | awk '{print \$2;}'",
            returnStdout: true
        ).trim()
    }
    return cookbookHighestVersionChef
}