// run Test Kitchen
def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        unstash 'sslCert'
        env.SSL_CERT_DIR= "${workspace}/.chef/trusted_certs/"
        sh 'ls -alt'
        sh 'pwd'
        sh 'kitchen test -d always'
    }
}