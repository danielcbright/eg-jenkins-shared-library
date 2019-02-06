def call() {
pipeline {
  agent any
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
    stage('cookbook testing') {
      parallel {
        stage('kitchen test') {
          steps {
            unstash 'cookbook'
            sh 'tar --strip-components=1 -zxvf cookbook.tar.gz'
            sh 'ls -alt'
            //chefTestKitchen()
          }
        }
        stage('metadata.rb') {
          steps {
            unstash 'changedFiles'
            sh 'grep -Fxq "metadata.rb" changedFiles.txt'
          }
        }
        stage('syntax check') {
          steps {
            chefSpec()
          }
        }
        stage('attrbutes') {
          steps {
            echo 'test'
          }
        }
        stage('README.md') {
          steps {
            echo 'test'
          }
        }
        stage('libraries') {
          steps {
            echo 'test'
          }
        }
        stage('files/templates') {
          steps {
            echo 'test'
          }
        }
      }
    }
    stage('unit test') {
      parallel {
        stage('Syntax Check') {
          steps {
            chefSpec()
          }
        }
        stage('Chefspec') {
          steps {
            echo 'test'
          }
        }
      }
    }
    stage('syntax check') {
      parallel {
        stage('chefSpec') {
          steps {
            chefSpec()
          }
        }
        stage('Cookstyle') {
          steps {
            echo 'test'
          }
        }
      }
    }
    stage('lint check') {
      parallel {
        stage('Lint Check') {
          steps {
            echo 'test'
          }
        }
        stage('Foodcritic') {
          steps {
            echo 'Test'
          }
        }
      }
    }
    stage('Code Review') {
      steps {
        echo 'Test'
      }
    }
    stage('Publish Cookbook') {
      steps {
        echo 'test'
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