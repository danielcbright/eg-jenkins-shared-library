def call() {
    def userInput = input(
        id: 'userInput', message: 'Enter cookbook information:?',
        parameters: [
            string(defaultValue: 'None',
                    description: 'Cookbook name',
                    name: 'COOKBOOKNAME'),
            string(defaultValue: 'None',
                    description: 'Supported systems (https://docs.chef.io/config_rb_metadata.html under "supports" section)',
                    name: 'SUPPORTS'),
            string(defaultValue: 'None',
                    description: 'Maintainer name',
                    name: 'MAINTAINER'),
            string(defaultValue: 'None',
                    description: 'Maintainer email',
                    name: 'MAINTAINER_EMAIL'),
            string(defaultValue: 'None',
                    description: 'License type (https://docs.chef.io/config_rb_metadata.html under "license" section)',
                    name: 'LICENSE'),
            string(defaultValue: 'None',
                    description: 'Short description of the cookbook',
                    name: 'SHORTDESCRIPTION'),
            string(defaultValue: 'None',
                    description: 'Longer description of the cookbook',
                    name: 'LONGDESCRIPTION'),
            string(defaultValue: '0.0.1',
                    description: 'Initial cookbook version (default is 0.0.1)',
                    name: 'COOKBOOKVERSION'),
            string(defaultValue: 'None',
                    description: 'Chef version supported (https://docs.chef.io/config_rb_metadata.html under "chef_version" section)',
                    name: 'CHEFVERSION'),
        ])
    inputCOOKBOOKNAME       = userInput.COOKBOOKNAME?:''
    inputSUPPORTS           = userInput.SUPPORTS?:''
    inputMAINTAINER         = userInput.MAINTAINER?:''
    inputMAINTAINER_EMAIL   = userInput.MAINTAINER_EMAIL?:''
    inputLICENSE            = userInput.LICENSE?:''
    inputSHORTDESCRIPTION   = userInput.SHORTDESCRIPTION?:''
    inputLONGDESCRIPTION    = userInput.LONGDESCRIPTION?:''
    inputCOOKBOOKVERSION    = userInput.COOKBOOKVERSION?:''
    inputCHEFVERSION        = userInput.CHEFVERSION?:''

    echo inputCOOKBOOKNAME
    echo inputSUPPORTS
    echo inputMAINTAINER
    echo inputMAINTAINER_EMAIL
    echo inputLICENSE
    echo inputSHORTDESCRIPTION
    echo inputLONGDESCRIPTION
    echo inputCOOKBOOKVERSION
    echo inputCHEFVERSION

    node {
        dir ("${inputCOOKBOOKNAME}") {
            git branch: 'master',
                credentialsId: 'd8135cad-2efa-46fa-bfb5-4aabdf9e2953',
                url: "https://github.com/danielcbright/eg-cookbook-template.git"
            withCredentials([usernamePassword(credentialsId: 'd8135cad-2efa-46fa-bfb5-4aabdf9e2953', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            env.GITHUB_TOKEN = "$PASSWORD"
            env.USERNAME = "$USERNAME"
            }
        }

        sh  """
            cd ${inputCOOKBOOKNAME}
            rm -rf .git
            mv _Jenkinsfile Jenkinsfile
            find . -type f -print0 | xargs -0 sed -i 's/COOKBOOKNAME/${inputCOOKBOOKNAME}/g'\n
            find . -type f -print0 | xargs -0 sed -i 's/SUPPORTS/${inputSUPPORTS}/g'\n
            find . -type f -print0 | xargs -0 sed -i 's/MAINTAINER/${inputMAINTAINER}/g'\n
            find . -type f -print0 | xargs -0 sed -i 's/MAINTAINER_EMAIL/${inputMAINTAINER_EMAIL}/g'\n
            find . -type f -print0 | xargs -0 sed -i 's/LICENSE/${inputLICENSE}/g'\n
            find . -type f -print0 | xargs -0 sed -i 's/SHORTDESCRIPTION/${inputSHORTDESCRIPTION}/g'\n
            find . -type f -print0 | xargs -0 sed -i 's/LONGDESCRIPTION/${inputLONGDESCRIPTION}/g'\n
            find . -type f -print0 | xargs -0 sed -i 's/COOKBOOKVERSION/${inputCOOKBOOKVERSION}/g'\n
            find . -type f -print0 | xargs -0 sed -i 's/CHEFVERSION/${inputCHEFVERSION}/g'\n
            pwd
            git config --global hub.protocol https
            git init
            git add .
            git commit -m "Initial commit of ${inputCOOKBOOKNAME} by Jenkins"
            /usr/local/bin/hub create
            /usr/local/bin/hub remote set-url origin https://\$GITHUB_TOKEN:x-oauth-basic@github.com/danielcbright/${$inputCOOKBOOKNAME}.git
            git push --set-upstream origin master
            """
    }
}