#!/bin/bash

appname=""

if [ $# == 1 ]
then
  if $1="--help"
  then
    echo "useage: create_ney_key.sh <appname>"
  else
    appname=$1
  fi
else
  echo "Wrong call: exactle one parameter appname needed"
  exit
fi

keytool -genkey -alias $appname -keystore $KEYSTORE -storetype PKCS12 -keyalg RSA -storepass $KEYSTORE_PASSWORD -validity 365000 -keysize 2048
keytool -keypasswd -keystore $KEYSTORE