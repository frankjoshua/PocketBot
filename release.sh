#!/bin/sh
#Should be able to move this to gradle
./gradlew clean
./gradlew incrementReleaseVersion
./gradlew assembleRelease
./gradlew uploadToGooglePlay
