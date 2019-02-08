def call(String dir) {
    sh "chef exec cookstyle ${dir}/ --format progress"
}