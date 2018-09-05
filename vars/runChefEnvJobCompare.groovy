def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        unstash 'sslCert'
        env.SSL_CERT_FILE= "${workspace}/.chef/trusted_certs/chef-server_dbright_io.crt"
        envOut = sh script: 'chef exec ruby envFunctions.rb -k .chef/knife.rb -c', returnStdout: true
    }
}