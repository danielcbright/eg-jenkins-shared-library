def call() {
    node {
        wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        sh 'knife ssl fetch'
        sh 'knife node list -c .chef/knife.rb'
        }
    }
}