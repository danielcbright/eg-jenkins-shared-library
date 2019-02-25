def call(String cookbookRepo, String dependCookbook, String newVersion) {
    echo "${cookbookRepo}, ${dependCookbook}, ${newVersion}"
    git branch: 'master',
        credentialsId: 'd8135cad-2efa-46fa-bfb5-4aabdf9e2953',
        url: "${cookbookRepo}"
    sh "sed \"s/depends\\s'${dependCookbook}'.*/depends\\s'${dependCookbook}',\\s'=\\s${newVersion}'/g\" metadata.rb"
    sh 'cat metadata.rb'
}