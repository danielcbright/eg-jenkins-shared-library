def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        def rubyContent = libraryResource('envFunctions.rb')
        writeFile(file: 'envFunctions.rb', text: rubyContent)
        sh "knife ssl fetch"
        stash includes: ".chef/trusted_certs/**", name: 'sslCert'
        sh "env"
    }
}