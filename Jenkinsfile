pipeline {
  agent any
  stages {
    stage('startBuild') {
      steps {
        sh '''#!/bin/sh
            chmod a+x ./gradlew
            ./gradlew clean build'''
        archiveArtifacts 'build/libs/*.jar'
      }
    }

  }
}
