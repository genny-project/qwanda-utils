pipeline {
	agent any
  tools {
      maven 'mvn 3.5'
  }
	options([
		pipelineTriggers([
			upstream(
        threshold: hudson.model.Result.SUCCESS,
        upstreamProjects: "Qwanda")
			])
		])
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
