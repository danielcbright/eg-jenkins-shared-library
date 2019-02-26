def call(String cookbookInfo) {
    def (cookbookRepo, dependCookbook, newVersion) = cookbookInfo.split(';')
    echo "${cookbookRepo}, ${dependCookbook}, ${newVersion}"
    git branch: 'master',
        credentialsId: 'd8135cad-2efa-46fa-bfb5-4aabdf9e2953',
        url: "${cookbookRepo}"
    def dependVer = getDependsVersion(dependCookbook, newVersion)
    if(dependVer >= newVersion) {
        echo "Version of ${dependCookbook} defined in metadata.rb is ${dependVer}, which is equal to or greater than the version being applied (${newVersion}), no changes being made."
    }
    if(dependVer < newVersion) {
        echo "Version of ${dependCookbook} defined in metadata.rb is ${dependVer}, which is lower than the version being applied (${newVersion}), making changes."
        sh """
        sed -i "s/depends '${dependCookbook}'.*/depends '${dependCookbook}', '= ${newVersion}'/g" metadata.rb
        """
        sh 'cat metadata.rb'
        def newDependVer = getDependsVersion(dependCookbook, newVersion)
        if(newDependVer == newVersion) {
            withCredentials([usernamePassword(credentialsId: 'd8135cad-2efa-46fa-bfb5-4aabdf9e2953', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            // available as an env variable, but will be masked if you try to print it out any which way
            // note: single quotes prevent Groovy interpolation; expansion is by Bourne Shell, which is what you want
            sh 'echo $PASSWORD'
            env.GITHUB_TOKEN = "$PASSWORD"
            sh 'env'
            // also available as a Groovy variable
            echo USERNAME
            // or inside double quotes for string interpolation
            echo "username is $USERNAME"
            }
            echo "Version updated in metadata.rb successfully, making Git PR now."
            sh "git checkout -b ${dependCookbook}-${newVersion}-JenkinsAutoUpdate"
            sh 'git status'
            sh 'git add metadata.rb'
            def buildURL = env.BUILD_URL
            sh "git commit -m \"[Jenkins] Updating metadata.rb for ${dependCookbook} version: ${newVersion} dependency and testing. [${buildURL}]\""
            sh "git push --set-upstream origin ${dependCookbook}-${newVersion}-JenkinsAutoUpdate"
            sh "/usr/local/bin/hub pull-request -m \"[Jenkins] Updating metadata.rb for ${dependCookbook} version: ${newVersion} dependency and testing. [${buildURL}]\""
        }
    }
}
def getDependsVersion(String dependCookbook, String newVersion) {
    script {
        dependVersion = sh (
            script: "sed -e \"s/^'//\" -e \"s/'\$//\" <<< `cat metadata.rb | grep ${dependCookbook} | awk '{print \$4;}'`",
            returnStdout: true
        ).trim()
    }
    return dependVersion
}