#!/bin/bash

appname=""

if [ $# == 1 ]
then
  if [ $1 == "--help" ]
  then
    echo "call from the root folder of the repo"
    echo "useage: create_ney_key.sh <appname>"
  else
    appname=$1
  fi  
else
  echo "Wrong call: exactle one parameter appname needed"
  exit
fi

# cd $appname/app/build/outputs/apk/release
cd $appname/app/build/outputs/aab/release

eval "KEYPASS=\"\$$appname\""
# jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $KEYSTORE -storepass $KEYSTORE_PASSWORD -keypass $KEYPASS app-release-unsigned.apk $appname
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore xample.jks app-release.aab $appname  
keytool -printcert -jarfile my-signed-binary.aab

#SDK_VERSION=$(ls -t ${ANDROID_SDK_ROOT}/build-tools/ | head -n 1)
#${ANDROID_SDK_ROOT}/build-tools/${SDK_VERSION}/zipalign -p -f -v 4 app-release-unsigned.apk $appname.apk
