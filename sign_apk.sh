#!/bin/bash

appname=""

if [ $# == 1 ]
  then
    appname=$1
  else
    echo "Wrong call: exactle one parameter appname needed"
    exit
fi

cd $appname/app/build/outputs/apk/release

eval "KEYPASS=\"\$$appname\""
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $KEYSTORE -storepass $KEYSTORE_PASSWORD -keypass $KEYPASS app-release-unsigned.apk $appname


SDK_VERSION=$(ls -t ${ANDROID_SDK_ROOT}/build-tools/ | head -n 1)
${ANDROID_SDK_ROOT}/build-tools/${SDK_VERSION}/zipalign -p -f -v 4 app-release-unsigned.apk $appname.apk