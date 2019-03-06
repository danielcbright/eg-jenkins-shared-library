def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        unstash 'cookbook'
        unstash 'sslCert'
        env.SSL_CERT_DIR= "${workspace}/.chef/trusted_certs/"
        sh 'ls -alt'
        sh 'pwd'
        sh 'berks install'
        sh 'berks update'
        sh 'berks upload'
        }
}