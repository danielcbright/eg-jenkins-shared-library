def call() {
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        def json = sh 'echo `knife environment show dbright-dev -F j`'
        def data = readJSON text json;
        echo data.cookbook_versions;
    }
}