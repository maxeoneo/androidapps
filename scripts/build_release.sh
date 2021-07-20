#!/bin/bash

appname=""

if [ $# == 1 ]
then
  if [ $1 == "--help" ]
  then
    echo "useage: build_release.sh <appname>"
    echo "useage: create_ney_key.sh <appname>"
  else
    appname=$1
  fi  
else
  echo "Wrong call: exactle one parameter appname needed"
  exit
fi

cd $appname
./gradlew assembleRelease
cd ..

./scripts/sign_apk.sh $appname