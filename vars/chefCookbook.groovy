pipeline {
  agent any
  stages {
    stage('Chef Env Prep') {
        steps {
            echo "Checking for changed files in PR"
            script {
                changedFiles = getFileChanges()
            }
            sh 'touch changedFiles.txt'
            sh "echo \"${changedFiles}\" >> changedFiles.txt"
            sh 'cat changedFiles.txt'
            stash includes: "changedFiles.txt", name: 'changedFiles'
        }
    }
    stage('Prepping Chef Environment')
        steps {
            envFunctionsPrep()
        }
    stage('tar Cookbook for Testing') {
        steps {
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
    stage('Cookbook Testing') {
      parallel {
        stage('run Test Kitchen') {
          steps {
            unstash 'cookbook'
            sh 'tar --strip-components=1 -zxvf cookbook.tar.gz'
            sh 'ls -alt'
            //chefTestKitchen()
          }
        }
        stage('check metadata.rb') {
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
        stage('Attrbutes') {
          steps {
            echo 'test'
          }
        }
        stage('ReadMe') {
          steps {
            echo 'test'
          }
        }
        stage('Libraries') {
          steps {
            echo 'test'
          }
        }
        stage('Files/Templates') {
          steps {
            echo 'test'
          }
        }
      }
    }
    stage('Unit Test') {
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
    stage('Syntax Check') {
      parallel {
        stage('Syntax Check') {
          steps {
            echo 'test'
          }
        }
        stage('Cookstyle') {
          steps {
            echo 'test'
          }
        }
      }
    }
    stage('Lint Check') {
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