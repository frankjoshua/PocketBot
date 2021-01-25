#!groovy​

def buildNumber = env.BUILD_NUMBER
def gitUrl = 'ssh://git@stash.tesseractmobile.net:7999/poc/pocketbot.git'
def branch = 'master'
def credentialsId = '9a65d10b-144b-4b63-9f93-91f9ef1ce543'

stage('Stage Build'){
    node(){
        deleteDir()
        git credentialsId: credentialsId, url: gitUrl, branch: branch
        sh "chmod +x ./gradlew"
        sh "export ANDROID_HOME=/opt/android-sdk && ./gradlew clean assembleDebug"
    }
}


stage('Stage Unit Test'){
    node('android') {
        git credentialsId: credentialsId, url: gitUrl, branch: branch
        sh "export ANDROID_HOME=/opt/android-sdk && ./gradlew test"
    }
}

stage('Stage Connected Test') {
    node('android') {
        git credentialsId: credentialsId, url: gitUrl, branch: branch
        sh "export ANDROID_HOME=/opt/android-sdk && ./gradlew uninstallAll"
        sh "export ANDROID_HOME=/opt/android-sdk && ./gradlew installDebug"
        sh "export ANDROID_HOME=/opt/android-sdk && ./gradlew grantPermissions"
        sh "export ANDROID_HOME=/opt/android-sdk && ./gradlew connectedDebugAndroidTest"
    }
}


stage('Stage Beta Release'){
    node() {
        git credentialsId: credentialsId, url: gitUrl, branch: branch
        sshagent([credentialsId]) {
            //Update build number
            sh "export ANDROID_HOME=/opt/android-sdk && ./gradlew incrementVersion"
            sh "git config --global user.email \"jenkins@tesseractmobile.com\""
            sh "git config --global user.name \"Jenkins\""
            sh "git commit -a -m\"Updated version number from Jenkins build $buildNumber\""
            sh 'git tag $(cat version.txt)'
            sh "git pull origin master"
            sh "git push origin master"
            sh "git push --tags"
            sh "git checkout beta"
            sh "git merge origin/master"
            sh "git push origin beta"
            //Save Change Log
            sh "export ANDROID_HOME=/opt/android-sdk && ./gradlew gitChangelogTask"
            //Upload to Crashlytics beta
            sh "export ANDROID_HOME=/opt/android-sdk && ./gradlew assembleDebug crashlyticsUploadDistributionDebug"
        }
    }
}

