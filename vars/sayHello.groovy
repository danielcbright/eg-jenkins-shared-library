#!/usr/bin/env groovy

def call(){
  pipeline {
    agent any
    stages {
      stage('Stage Environments') {
        steps {
          def scriptTest = libraryResource 'script_test.txt'
          writeFile file: 'scriptTest.test', text: scriptTest
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