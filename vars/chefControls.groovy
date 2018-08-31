#!/usr/bin/env groovy

def call(){
  pipeline {
    agent any
    stages {
      stage('Stage Environments') {
        steps {
          runChefEnvJobCompare()
        }
      }
      stage('Publish Environments to Production') {
        steps {
          runChefEnvJobProcess()
        }
      }
    }
  }
}