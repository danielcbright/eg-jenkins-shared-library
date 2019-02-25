def call(String cookbookRepo) {
    def exists = fileExists "./${cookbookRepo}/.dir"
    if (!exists){
        new File("./${cookbookRepo}/.dir").mkdir()
    }
    dir ("./${cookbookRepo}") {
        git branch: 'master',
            credentialsId: 'd8135cad-2efa-46fa-bfb5-4aabdf9e2953',
            url: 'https://github.com/danielcbright/eg-linux-role-cookbook-g.git'
        sh 'ls -alt'
    }
}