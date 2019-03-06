def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        unstash 'cookbook'
        sh 'tar -xvf cookboog.tar.gz'
        unstash 'sslCert'
        env.SSL_CERT_DIR= "${workspace}/.chef/trusted_certs/"
        sh 'ls -alt'
        sh 'pwd'
        sh 'berks install'
        sh 'berks update'
        sh 'berks upload'
        }
}