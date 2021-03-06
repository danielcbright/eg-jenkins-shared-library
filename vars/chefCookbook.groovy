def call() {
  def prInfo = []
  def String cookbookName = ""
  def String cookbookVersion = ""
  def String existsOnServer = null
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
            envFunctionsPrep()
        }
    }
    stage('Cookbook Version Validation') {
      parallel {
        stage('validate metadata.rb') {
          steps {
            unstash 'cookbook'
            script {
              cookbookInfo = compareCookbookVersions()
              (v, z, exists) = cookbookInfo.split(':')
              cookbookName = "${v}"
              cookbookVersion = "${z}"
              existsOnServer = "${exists}"
            }
          }
        }
      }
    }
    stage('Style Lint (cookstyle)') {
      when {
        not {
          allOf {
            branch 'master'
            expression { existsOnServer == 'Y' }
          }
        }
      }
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
      when {
        not {
          allOf {
            branch 'master'
            expression { existsOnServer == 'Y' }
          }
        }
      }
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
      when {
        not {
          allOf {
            branch 'master'
            expression { existsOnServer == 'Y' }
          }
        }
      }
      steps {
        echo 'performing test kitchen convergence test'
        deleteDir()
        unstash 'cookbook'
        sh 'tar -xvf cookbook.tar.gz --strip 1'
        //chefTestKitchen()
      }
    }
    stage('Publish Cookbook & Merge PR to Master') {
      when {
        not {
          allOf {
            branch 'master'
            expression { existsOnServer == 'Y' }
          }
        }
      }
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
      when {
        not {
          branch 'master'
        }
      }
      steps {
        script {
          sourceURLs = getCookbookVersions(cookbookName, cookbookVersion)
          if ( sourceURLs.isEmpty() ) {
            echo "Skipping last step, no dependencies."
          }
        }
      }
    }
    stage("Create PRs") {
      when { 
        not {
          anyOf {
            branch 'master';
            expression { sourceURLs.isEmpty() }
          }
        }
      }
      steps {
        script {
          sourceURLs.each {
            echo "${it}, ${cookbookName}, ${cookbookVersion}"
            stepName = "PR for ${it}"
            cookbookInfo = "${it};${cookbookName};${cookbookVersion}"
            prInfo << cookbookInfo
          }
        }
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
