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
            def match = envOut =~ /.*Change detected in.*/
            when {
              match.matches()
            }
            steps {
              input 'Publish Environments to Production Chef Server?'
              runChefEnvJobProcess()
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
