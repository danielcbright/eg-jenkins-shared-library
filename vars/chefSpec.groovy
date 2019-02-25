// Chefspec check
def call() {
    node {
        wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        unstash 'sslCert'
        env.SSL_CERT_DIR= "${workspace}/.chef/trusted_certs/"
        sh 'chef exec rspec'
        }
    }
}