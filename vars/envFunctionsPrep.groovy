def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        def rubyContent = libraryResource('envFunctions.rb')
        writeFile(file: 'envFunctions.rb', text: rubyContent)
        sh 'ls -alt && env && knife ssl fetch'
        env.SSL_CERT_FILE= "${workspace}/.chef/trusted_certs/chef-server_dbright_io.crt"
    }
}