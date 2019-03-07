def call() {
  def prInfo = []
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
    stage('Prepping Environment') {
        steps {
            echo "Checking for changed files in PR"
            script {
                changedFiles = getFileChanges()
            }
            sh 'touch changedFiles.txt'
            sh "echo \"${changedFiles}\" > changedFiles.txt"
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
    stage('PR Validation') {
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
      }
    }
    stage('Style Lint (cookstyle)') {
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
    stage('Syntax & Logic Test') {
      parallel {
        stage('foodcritic') {
          steps {
            unstash 'cookbook'
            foodCritic()
          }
        }
      }
    }
    stage('convergence & inspec test') {
      steps {
        echo 'performing test kitchen convergence test'
        deleteDir()
        unstash 'cookbook'
        sh 'tar -xvf cookbook.tar.gz --strip 1'
        chefTestKitchen()
      }
    }
    stage('Publish Cookbook & Merge PR to Master') {
      steps {
        script {
          def userInputPUB = input message: 'Publish Cookbook?',
              parameters: [choice(name: 'Publish', choices: 'no\nyes', description: 'Choose "yes" to publish this cookbook')]
          if (userInputPUB == 'yes') {
            deleteDir()
            chefPublishCookbook()
          }
        }
      }
    }
    stage("Lookup Dependencies") {
      steps {
        script {
          sourceURLs = getCookbookVersions(cookbookName, cookbookVersion)
        }
        script {
          sourceURLs.each {
            echo "${it}, ${cookbookName}, ${cookbookVersion}"
            stepName = "PR for ${it}"
            cookbookInfo = "${it};${cookbookName};${cookbookVersion}"
            prInfo << cookbookInfo
            }
          }
        }
    }
    stage('Create PRs') {
      steps {
        script {
          for (prs in prInfo) {
            def (cbURL, cbName, cbVersion) = prs.split(';')
            echo "+++ CREATING PR FOR: ${cbName} at source ${cbURL} +++"
          }
          def userInputPR = input message: 'Create Dependent PRs?',
              parameters: [choice(name: 'Create', choices: 'no\nyes', description: 'Choose "yes" to create dependent PRs for the cookbooks listed above')]
          if (userInputPR == 'yes') {
            deleteDir()
            script {
              for (pr in prInfo) {
                createPRs(pr)
              }
            }
          }
        }
      }
    }
  }
}
}
