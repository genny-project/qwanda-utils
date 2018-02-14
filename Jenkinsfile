pipeline {
	agent any
  tools {
      maven 'mvn 3.5'
  }
	triggers {
  	upstream(upstreamProjects: "Qwanda", threshold: hudson.model.Result.SUCCESS)
	}
	stages {
		stage('Build') {
			steps {
				sh 'mvn clean install -U -DskipTests=false'
			}
		}
		stage('Deploy') {
      when { branch 'master'}
			steps {
				sh 'echo Deploying...'
			}
		}
		stage('Done') {
			steps {
				sh 'echo Slacking'
			}
		}
	}
	post {
		always {
	    deleteDir()
	  }
	}
}
