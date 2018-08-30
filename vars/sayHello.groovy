#!/usr/bin/env groovy

def call(){
  pipeline {
    agent any
    stages {
      stage('Stage Environments') {
        steps {
          def rubyContent = libraryResource('script_test.txt')
          writeFile(file: 'script_text.txt', text: rubyContent)
          sh '''
          ls -alt
          pwd
          '''
        }
      }
      stage('Publish Environments to Production') {
        steps {
          input 'Publish Environments to Production Chef Server?'
          sh 'echo testing'
        }
      }
    }
  }
}