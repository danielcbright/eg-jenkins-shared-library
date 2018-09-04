#!/usr/bin/env groovy

def call(){
  pipeline {
      agent any
        stages {
          parallel {
            stage('Stage Environments') {
              steps {
                runChefEnvJobCompare()
              }
            }
            state('Stage Data Bags') {
              steps (
                runDataBagCompare()
              )
            }
        stage('Publish Environments to Production') {
          parallel {
            steps {
              input 'Publish Environments to Production Chef Server?'
              runChefEnvJobProcess()
            }
            steps {
              input 'Publish Data Bags to Production Chef Server?'
              runDataBagProcess()
            }
          }
        }
      }
    }
  }
}
