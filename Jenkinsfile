#!groovy​

def buildNumber = env.BUILD_NUMBER
def gitUrl = 'ssh://git@stash.tesseractmobile.net:7999/poc/pocketbot.git'
def branch = 'master'
def credentialsId = '9a65d10b-144b-4b63-9f93-91f9ef1ce543'

stage('Stage Build'){
    node(){
        deleteDir()
        git credentialsId: credentialsId, url: gitUrl, branch: branch
        sh "cd PocketBot && chmod +x ./gradlew"
        sh "cd PocketBot && ./gradlew clean assembleDebug"
    }
}


stage('Stage Unit Test'){
    node() {
        git credentialsId: credentialsId, url: gitUrl, branch: branch
        sh "cd PocketBot && ./gradlew test"
    }
}

stage('Stage Connected Test') {
    node('android') {
        git credentialsId: credentialsId, url: gitUrl, branch: branch
        sh "cd PocketBot && ./gradlew uninstallAll"
        sh "cd PocketBot && ./gradlew installDebug"
        sh "cd PocketBot && ./gradlew grantPermissions"
        sh "cd PocketBot && ./gradlew connectedDebugAndroidTest"
    }
}


stage('Stage Beta Release'){
    node() {
        git credentialsId: credentialsId, url: gitUrl, branch: branch
        sshagent([credentialsId]) {
            //Update build number
            sh "cd PocketBot && ./gradlew incrementVersion"
            sh "cd PocketBot && git config --global user.email \"jenkins@tesseractmobile.com\""
            sh "cd PocketBot && git config --global user.name \"Jenkins\""
            sh "cd PocketBot && git commit -a -m\"Updated version number from Jenkins build $buildNumber\""
            sh 'cd PocketBot && git tag $(cat version.txt)'
            sh "cd PocketBot && git pull origin master"
            sh "cd PocketBot && git push origin master"
            sh "cd PocketBot && git push --tags"
            sh "cd PocketBot && git checkout beta"
            sh "cd PocketBot && git merge origin/master"
            sh "cd PocketBot && git push origin beta"
            //Save Change Log
            sh "cd PocketBot && ./gradlew gitChangelogTask"
            //Upload to Crashlytics beta
            sh "cd PocketBot && ./gradlew assembleDebug crashlyticsUploadDistributionDebug"
        }
    }
}

