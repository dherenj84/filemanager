pipeline {
  agent {
    node {
      label 'master'
    }

  }
  stages {
    stage('build') {
      steps {
        sh '''export MAVEN_HOME=/Applications/apache-maven-3.6.2
export PATH=$PATH:$MAVEN_HOME/bin
mvn clean package -DskipTests'''
      }
    }

    stage('deploy') {
      steps {
        pushToCloudFoundry(organization: 'bubbacorp', cloudSpace: 'development', target: 'api.run.pivotol.io', credentialsId: '12345')
      }
    }

  }
}