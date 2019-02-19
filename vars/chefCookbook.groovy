def call() {
pipeline {
  agent any
  environment {
    PATH = "/opt/rh/rh-ruby22/root/usr/bin:$PATH"
    LD_LIBRARY_PATH = "/opt/rh/rh-ruby22/root/usr/lib64"
    PKG_CONFIG_PATH = "/opt/rh/rh-ruby22/root/usr/lib64/pkgconfig"
    MANPATH = "/opt/rh/rh-ruby22/root/usr/share/man:"
  }
  stages {
    stage('prepping environment') {
        steps {
            echo "Checking for changed files in PR"
            script {
                changedFiles = getFileChanges()
            }
            sh 'touch changedFiles.txt'
            sh "echo \"${changedFiles}\" >> changedFiles.txt"
            sh 'cat changedFiles.txt'
            stash includes: "changedFiles.txt", name: 'changedFiles'
            envFunctionsPrep()
            sh '''
            base=$(basename $PWD)
            cd ..
            tar -czf cookbook.tar.gz $base
            ls -alt
            pwd
            '''
            sh 'mv ../cookbook.tar.gz ./'
            stash includes: "cookbook.tar.gz", name: 'cookbook'
        }
    }
    // stage('PR validation') {
    //   parallel {
    //     stage('validate metadata.rb') {
    //       steps {
    //         unstash 'cookbook'
    //         compareCookbookVersions()
    //       }
    //     }
    //     stage('validate README.md') {
    //       steps {
    //         unstash 'cookbook'
    //         echo 'checking for existance of README.md'
    //         script {
    //             if (fileExists('README.md')) {
    //                 echo 'performing markdown lint check on README.md'
    //                 sh '''
    //                 scl enable rh-ruby22 bash
    //                 /opt/rh/rh-ruby22/root/usr/local/share/gems/gems/mdl-0.5.0/bin/mdl README.md
    //                 '''
    //             } else {
    //                 error("README.md doesn't exist, please create one!")
    //             }
    //         }
    //       }
    //     }
    //   }
    // }
    stage('style lint (cookstyle)') {
      parallel {
        stage('libraries') {
          steps {
            unstash 'cookbook'
            echo 'running cookstyle on libraries'
            chefCookstyle('libraries')
          }
        }
        stage('files/templates') {
          steps {
            unstash 'cookbook'
            echo 'running cookstyle on templates'
            chefCookstyle('templates')
          }
        }
        stage('attributes') {
          steps {
            unstash 'cookbook'
            echo 'running cookstyle on attributes'
            chefCookstyle('attributes')
          }
        }
        stage('recipes') {
          steps {
            unstash 'cookbook'
            echo 'running cookstyle on recipes'
            chefCookstyle('recipes')
          }
        }
      }
    }
    stage('syntax & logic lint') {
      parallel {
        stage('foodcritic') {
          steps {
            unstash 'cookbook'
            foodCritic()
          }
        }
        stage('chefspec') {
          steps {
            unstash 'cookbook'
            chefSpec()
          }
        }
        stage('validate metadata.rb') {
          steps {
            unstash 'cookbook'
            //compareCookbookVersions()
          }
        }
      }
    }
    stage('convergence & inspec test') {
        steps {
            echo 'performing test kitchen convergence test'
            unstash 'cookbook'
            //chefTestKitchen()
        }
    }
    stage("create PR's for dependent versions") {
      steps {
        getCookbookVersions()
      }
    }
    stage("setup hub for PR creation") {
      steps {
        withCredentials([
        string(credentialsId: env.GITHUB_MACHINE_USER_TOKEN, variable: 'token'),
        usernamePassword(credentialsId: env.GITHUB_MACHINE_USER_PASS, passwordVariable: 'password', usernameVariable: 'username')]) {

        writeFile file: env.HUB_CONFIG, text: """github.com:
- user: ${username}
oauth_token: ${token}
protocol: https"""
        }
        sh 'cat `$HUB_CONFIG`'
      }
    }
    stage('Publish Cookbook') {
      steps {
        echo 'publishing cookbook'
        //chefPublishCookbook()
      }
    }
    stage('Commit to Master') {
      steps {
        echo 'test'
      }
    }
  }
}
}
