def call(String cookbookInfo) {
    def (cookbookRepo, dependCookbook, newVersion) = cookbookInfo.split(';')
    echo "${cookbookRepo}, ${dependCookbook}, ${newVersion}"
    def (httpS, gitUrl) = cookbookRepo.split('//')
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
        bumpMinorVersion()
        sh 'cat metadata.rb'
        def newDependVer = getDependsVersion(dependCookbook, newVersion)
        if(newDependVer == newVersion) {
            withCredentials([usernamePassword(credentialsId: 'd8135cad-2efa-46fa-bfb5-4aabdf9e2953', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            env.GITHUB_TOKEN = "$PASSWORD"
            env.USERNAME = "$USERNAME"
            }
            echo "Version updated in metadata.rb successfully, making Git PR now."
            sh "git remote rm origin"
            sh "git remote add origin https://$USERNAME:$GITHUB_TOKEN@${gitUrl}"
            sh 'git branch | grep -v "master" | xargs git branch -D'
            sh "git checkout -b ${dependCookbook}-${newVersion}-JenkinsAutoUpdate"
            sh 'git status'
            sh 'git add metadata.rb'
            def buildURL = env.BUILD_URL
            sh "git commit -m \"[Jenkins] Updating metadata.rb for ${dependCookbook} version: ${newVersion} dependency and testing. [${buildURL}]\""
            sh "git push --set-upstream origin ${dependCookbook}-${newVersion}-JenkinsAutoUpdate"
            sh "/usr/local/bin/hub pull-request -m \"[Jenkins] Updating metadata.rb\" -F- <<<\"Updating metadata.rb: ${dependCookbook} version: ${newVersion} for dependency and testing. [${buildURL}]\""
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
def bumpMinorVersion() {
    script {
        cookbookVersion = sh (
            script: 'sed -e "s/^\'//" -e "s/\'$//" <<< `awk \'{for (I=1;I<=NF;I++) if ($I == "version") {print $(I+1)};}\' metadata.rb`',
            returnStdout: true
        ).trim()
    }
    sh 'cat metadata.rb'
    echo "${cookbookVersion}"
    def (int major, int minor, int patch) = cookbookVersion.split('.')
    echo "${major} ${minor} ${patch}"
    patch++
    def newSemVer = "${major}.${minor}.${patch}"
    echo "PREV: ${cookbookVersion} NEW: ${newSemVer}"
    echo "Bumping cookbook version (patch only)"
    sh "sed -i \"s/^version '.*/version '${newSemVer}'/g\" metadata.rb"
}