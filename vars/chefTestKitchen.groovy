// run Test Kitchen
def call() {
        wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        sh 'ls -alt'
        sh 'pwd'
        sh 'kitchen test'
        }
}