@NonCPS
def call() {
    echo "Gathering SCM changes"
    sh 'touch changedFiles.txt'
    def changeString = "# This file contains a list of files changed since the last commit\n"
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            def files = new ArrayList(entry.affectedFiles)
            for (int k = 0; k < files.size(); k++) {
                def file = files[k]
                sh """echo ${file.path} >>> changedFiles.txt"""
            }
        }
    }

    if (!changeString) {
        changeString = " - No new changes"
    }
    sh "echo $changeString >> changedFiles.txt"
    sh 'cat changedFiles.txt'
    return changeString
}
