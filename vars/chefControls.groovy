#!/usr/bin/env groovy

def call() {
  pipeline {
    agent any
    triggers {
        issueCommentTrigger('.*test this please.*')
    }
    stages {
      stage('Chef Env Prep') {
        steps {
          envFunctionsPrep()
        }
      }
      stage('Prepare Chef Objects') {
        parallel {
          stage('Stage Environments') {
            steps {
              runChefEnvJobCompare()
            }
          }
          stage('Stage Data Bags') {
            steps {
              runDataBagCompare()
            }
          }
        }
      }
      stage('Approve & Publish Changes') {
        parallel {
          stage('Publish Environments to Production') {
            steps {
              script {
                if !(envOut ==~ /*.No change for the.*/) {
                  input 'Publish Environments to Production Chef Server?'
                  runChefEnvJobProcess()
                } else { 
                  echo 'No changes detected for Chef Environments'
                }
              } 
            }
          }
          stage('Publish Data Bags to Production') {
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
