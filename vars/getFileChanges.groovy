def call() {
    MAX_MSG_LEN = 100
    def changeString = ""

    echo "Gathering SCM changes"
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            def files = new ArrayList(entry.affectedFiles)
                for (int k = 0; k < files.size(); k++) {
                    changeString += "${file.path}\n"
                }
        }
    }

    if (!changeString) {
        changeString = " - No new changes"
    }
    return changeString
}
