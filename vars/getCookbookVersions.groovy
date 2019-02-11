def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        unstash 'sslCert'
        env.SSL_CERT_DIR= "${workspace}/.chef/trusted_certs/"
        def json = sh 'echo `knife environment show dbright-dev -F j`'
        def data = readJSON text: json;
        echo data.cookbook_versions;
    }
}