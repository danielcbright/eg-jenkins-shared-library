def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        unstash 'cookbook'
        sh 'tar -xvf cookbook.tar.gz --strip 1'
        unstash 'sslCert'
        env.SSL_CERT_DIR= "${workspace}/.chef/trusted_certs/"
        sh 'ls -alt'
        sh 'pwd'
        sh 'berks install'
        sh 'berks update'
        sh 'berks upload'
        }
}