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
    }

    if ( cookbookVersion > cookbookHighestVersionChef ) {
        echo "PASS: local cookbook version [${cookbookVersion}] is higher than Chef Server version [${cookbookHighestVersionChef}]"
    } else {
        error "FAIL: local cookbook version [${cookbookVersion}] is NOT higher than Chef Server version [${cookbookHighestVersionChef}]"
    }
    return "${cookbookName}:${cookbookVersion}"
}