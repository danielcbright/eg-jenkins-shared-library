// foodcritic check
def call() {
    node {
        wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        sh 'foodcritic ./'
        }
    }
}