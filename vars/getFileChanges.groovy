def changes() {
    def changeString = ""

    echo "Gathering SCM changes"
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            def files = new ArrayList(entry.affectedFiles)
            for (int k = 0; k < files.size(); k++) {
                def file = files[k]
                sh """echo ${file.path}"""
            }
        }
    }

    if (!changeString) {
        changeString = " - No new changes"
    }
    return changeString
}


def call() {
    changes()"
}