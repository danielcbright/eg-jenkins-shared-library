// run Test Kitchen
def call() {
    node {
        wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        sh 'ls -alt'
        sh 'kitchen test ${workspace}/'
        }
    }
}