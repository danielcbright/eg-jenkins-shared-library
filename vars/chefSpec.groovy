// Chefspec check
def call() {
    node {
        wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        sh 'chef exec rspec'
        }
    }
}