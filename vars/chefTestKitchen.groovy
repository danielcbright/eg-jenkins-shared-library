// run Test Kitchen
def call() {
    node {
        wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        sh 'kitchen test ${workspace}/'
        }
    }
}