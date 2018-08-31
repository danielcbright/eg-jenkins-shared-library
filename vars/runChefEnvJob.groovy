def call() {
    node {
        wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
            def rubyContent = libraryResource('envFunctions.rb')
            writeFile(file: 'envFunctions.rb', text: rubyContent)
            sh('chmod +x envFunctions.rb')
            sh('ls -alt')
            sh 'knife ssl fetch'
            sh 'chef exec ruby envFunctions.rb -k .chef/knife.rb'
        }
    }
}