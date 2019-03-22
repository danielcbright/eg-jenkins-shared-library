def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        def rubyContent = libraryResource('envFunctions.rb')
        writeFile(file: 'envFunctions.rb', text: rubyContent)
        sh "knife ssl fetch"
        sh '''
            base=$(basename $PWD)
            cd ..
            tar -czf cookbook.tar.gz $base
            '''
        stash includes: ".chef/trusted_certs/**", name: 'sslCert'
        sh 'mv ../cookbook.tar.gz ./'
        stash includes: "cookbook.tar.gz", name: 'cookbook'
    }
}