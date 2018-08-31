#!/usr/bin/env groovy

def call(){
  pipeline {
    agent any
    stages {
      stage('Stage Environments') {
        steps {
          runChefEnvJob()
          sh '''
          ls -alt
          pwd
          '''
          wrap([$class: 'ChefIdentityBuildWrapper', jobIdentity: 'Jenkins']) {
            sh 'knife node list -c .chef/knife.rb'
          }
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