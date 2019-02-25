def call() {
  def running_set = [:]
  def String cookbookName = ""
  def String cookbookVersion = ""
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
    stage('PR validation') {
      parallel {
        stage('validate metadata.rb') {
          steps {
            unstash 'cookbook'
            script {
              cookbookInfo = compareCookbookVersions()
              (v, z) = cookbookInfo.split(':')
              cookbookName = "${v}"
              cookbookVersion = "${z}"
            }
          }
        }
        stage('validate README.md') {
          steps {
            unstash 'cookbook'
            echo 'checking for existance of README.md'
            // script {
            //     if (fileExists('README.md')) {
            //         echo 'performing markdown lint check on README.md'
            //         sh '''
            //         scl enable rh-ruby22 bash
            //         /opt/rh/rh-ruby22/root/usr/local/share/gems/gems/mdl-0.5.0/bin/mdl README.md
            //         '''
            //     } else {
            //         error("README.md doesn't exist, please create one!")
            //     }
            //}
          }
        }
      }
    }
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
    stage("gather dependent cookbook sources") {
      steps {
        script {
          sourceURLs = getCookbookVersions()
        }
        script {
          for (sourceURL in sourceURLs) {
            echo "${sourceURL}, ${cookbookName}, ${cookbookVersion}"
            running_set.put("PR for ${sourceURL}", { createPRs("${sourceURL}", "${cookbookName}", "${cookbookVersion}") })
            echo "${sourceURL}, ${cookbookName}, ${cookbookVersion}"
          }
        }
      }
    }
    stage("setup hub for PR creation") {
      steps {
        echo 'creating dependent prs'
      }
    }
    stage('Create PRs') {
      steps {
        script {
          parallel(running_set)
        }
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
