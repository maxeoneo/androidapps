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
echo $KEYPASS
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $KEYSTORE -storepass $KEYSTORE_PASSWORD -keypass $KEYPASS app-release-signed.apk $appname


SDK_VERSION=$(ls -t ${ANDROID_SDK_ROOT}/build-tools/ | head -n 1)
echo $SDK_VERSION
${ANDROID_SDK_ROOT}/build-tools/${SDK_VERSION}/zipalign -p -f -v 4 app-release-signed.apk $appname.apk