properties([
    buildDiscarder(logRotator(daysToKeepStr: '', numToKeepStr: '7')),
])
timestamps {
  node {
    mattermostSend endpoint: 'https://mm.citeck.ru/hooks/9ytch3uox3retkfypuq7xi3yyr', channel: "build_notifications", color: 'good', message: " :arrow_forward: Build info - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
    try {
      stage('Checkout SCM') {
        checkout([
          $class: 'MercurialSCM',
          credentialsId: 'bc074014-bab1-4fb0-b5a4-4cfa9ded5e66',
          installation: '(Default)',
          revision: "${env.BRANCH_NAME}",
          source: "ssh://hg@bitbucket.org/citeck/ecos-community"
        ])
      }
      def project_version = readMavenPom().getVersion()
      if ((env.BRANCH_NAME != "master") && (!project_version.contains('SNAPSHOT')))  {
        echo "Assembly of release artifacts is allowed only from the master branch!"
        currentBuild.result = 'SUCCESS'
        return
      }
      stage('Assembling and publishing project artifacts') {
        withMaven(mavenLocalRepo: '/opt/jenkins/.m2/repository', tempBinDir: '') {
          sh "mvn clean deploy -Penterprise -DskipTests=true"
          sh "cd war-solution/ && mvn clean deploy -Pjavamelody -DskipTests=true"
        }
      }
      stage('Building an ecos docker image') {
        build job: 'build_ecos_image', parameters: [
          string(name: 'DOCKER_BUILD_DIR', value: '/docker/centos/ecos'), 
          string(name: 'ECOS', value: 'community'), 
          string(name: 'ECOS_VERSION', value: "${project_version}"), 
          string(name: 'ECOS_CLASSIFIER', value: '5.1.f-com'), 
          string(name: 'FLOWABLE_VERSION', value: '1.5.0')
        ]
      }
    }
    catch (Exception e) {
      currentBuild.result = 'FAILURE'
      error_message = e.getMessage()
      echo error_message
    }
    script{
      if(currentBuild.result != 'FAILURE'){
        mattermostSend endpoint: 'https://mm.citeck.ru/hooks/9ytch3uox3retkfypuq7xi3yyr', channel: "build_notifications", color: 'good', message: " :white_check_mark: Build complete - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
      }
      else{
        mattermostSend endpoint: 'https://mm.citeck.ru/hooks/9ytch3uox3retkfypuq7xi3yyr', channel: "build_notifications", color: 'danger', message: " @channel :exclamation: Build failure - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>) :\n${error_message}"
      }
    }
  }
}
