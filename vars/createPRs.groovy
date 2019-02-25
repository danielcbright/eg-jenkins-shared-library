def call(String cookbookRepo, String dependCookbook, String newVersion) {
    def exists = fileExists "./${cookbookRepo}/.dir"
    if (exists){
        sh "rm -rf ./${cookbookRepo}"
        new File("./${cookbookRepo}/.dir").mkdir()
    }
    if (!exists){
        new File("./${cookbookRepo}/.dir").mkdir()
    }
    dir ("./${cookbookRepo}") {
        git branch: 'master',
            credentialsId: 'd8135cad-2efa-46fa-bfb5-4aabdf9e2953',
            url: 'https://github.com/danielcbright/eg-linux-role-cookbook-g.git'
        sh "sed \"s/depends\\s'${dependCookbook}'.*/depends\\s'${dependCookbook}',\\s'=\\s${newVersion}'/g\" metadata.rb"
        sh 'cat metadata.rb'
    }
}