// run Test Kitchen
def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        unstash 'sslCert'
        env.SSL_CERT_FILE= "${workspace}/.chef/trusted_certs/chef-server_dbright_io.crt"
        sh 'ls -alt'
        sh 'pwd'
        sh 'kitchen test -d always'
    }
}