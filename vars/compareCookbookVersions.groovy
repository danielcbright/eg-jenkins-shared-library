def call () {
    echo "Check if version in metadata.rb is higher than what's currently on the Chef Server"
    script {
        cookbookName = sh (
            script: 'sed -e "s/^\'//" -e "s/\'$//" <<< `awk \'{for (I=1;I<=NF;I++) if ($I == "name") {print $(I+1)};}\' metadata.rb`',
            returnStdout: true
        ).trim()
        cookbookVersion = sh (
            script: 'sed -e "s/^\'//" -e "s/\'$//" <<< `awk \'{for (I=1;I<=NF;I++) if ($I == "version") {print $(I+1)};}\' metadata.rb`',
            returnStdout: true
        ).trim()
    }
    echo "Cookbook Name:${cookbookName} Version in metadata.rb:${cookbookVersion}"
    wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
        unstash 'sslCert'
        env.SSL_CERT_DIR= "${workspace}/.chef/trusted_certs/"
        script {
            cookbookHighestVersionChef = sh (
                script: "knife cookbook show ${cookbookName} | awk '{print \$2;}'",
                returnStdout: true
            ).trim()
        }
        echo "Cookbook Name:${cookbookName} metadata.rb version:${cookbookVersion} version on Chef Server:${cookbookHighestVersionChef}"
    }

    sh """
    function version { echo "\$@" | gawk -F. '{ printf(\"%03d%03d%03d\n\", \$1,\$2,\$3); }'; }
    if [ \"$(version \"$cookbookVersion\")\" -gt \"$(version \"$cookbookHigestVersionChef\")\" ];
        echo \"local version is higher than Chef Server\"
    fi
    """
}