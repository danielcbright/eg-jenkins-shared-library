def call(String cookbookInfo) {
    node {
        def (cookbookRepo, dependCookbook, newVersion) = cookbookInfo.split(';')
        echo "${cookbookRepo}, ${dependCookbook}, ${newVersion}"
        git branch: 'master',
            credentialsId: 'd8135cad-2efa-46fa-bfb5-4aabdf9e2953',
            url: "${cookbookRepo}"
        sh """
        sed "s/depends '${dependCookbook}'.*/depends '${dependCookbook}', '= ${newVersion}'/g" metadata.rb
        """
        sh 'cat metadata.rb'
    }
}