#!/bin/bash

appname=""
password=""

if [ $# == 2 ]
  then
    appname=$1
    password=$2
  else
    echo "Wrong call: exactle two parameters needed"
    echo "First one is appname"
    echo "Second one is password"
    exit
fi

keytool -genkey -alias $appname -keystore $KEYSTORE -storetype PKCS12 -keyalg RSA -storepass $KEYSTORE_PASSWORD -keypass $password -validity 365000 -keysize 2048