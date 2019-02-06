import groovy.transform.Field
@Field String changeString = "changeString\n"
def appendString() { changeString.concat() }
def changes() {
    echo "Gathering SCM changes"
    def changeLogSets = currentBuild.changeSets
    if (changeLogSets != null) {
        for (int i = 0; i < changeLogSets.size(); i++) {
            def entries = changeLogSets[i].items
            for (int j = 0; j < entries.length; j++) {
                def entry = entries[j]
                def files = new ArrayList(entry.affectedFiles)
                for (int k = 0; k < files.size(); k++) {
                    def file = files[k]
                    assert appendString() = "${file.path}\n"
                }
            }
        }
    }
    echo "$changeString"
}


def call() {
    echo "${changes()}"
}