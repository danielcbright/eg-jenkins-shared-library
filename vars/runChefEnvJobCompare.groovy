def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        env.SSL_CERT_FILE= "${workspace}/.chef/trusted_certs/chef-server_dbright_io.crt"
        sh "ls -alt ${workspace}/.chef/trusted_certs/ && ls -alt && env && chef exec ruby envFunctions.rb -k .chef/knife.rb -c"
    }
}