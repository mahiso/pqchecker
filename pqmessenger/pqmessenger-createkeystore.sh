#!/bin/bash
#
# Keystore creator tool for pqMessenger 
# (c) 2015-2017 Abdelhamid MEDDEB <abdelhamid@meddeb.net>

KSPWD=

readPassword() {
  local PWD1=""
  local PWD2=""
  while [ "$PWD1" == "" ] || [ "$PWD1" != "$PWD2" ]
  do 
    echo -ne "Enter keystore password: "
    read -s PWD1
    echo
    echo -ne "Confirm: "
    read -s PWD2
    echo
    if [ -z $PWD1 ] && [ -z $PWD2 ]; then
      PWD1=$JMSPWD
      PWD2=$JMSPWD
    fi
    echo ""
    if [ "$PWD1" == "" ]; then
      echo "Password cannot be empty!"
    elif [ "$PWD1" != "$PWD2" ]; then
      echo "Password confirmation error !"
    fi
    echo ""
  done
  KSPWD=$PWD1
}

CMD=$(command -v keytool)
if [ -z "$CMD" ]; then
  echo "Error! keytool not found"
  exit 1
fi
echo ""
echo "Keystore generation tool for pqMessenger"
echo ""
readPassword
echo "keystore creation.."
$CMD -genkey -alias pqmessenger -keyalg RSA -keystore keystore.jks -dname "CN=pqMessenger, OU=pqChecker, O=PPolicy, L=LDAPPPolicy, S=IDF, C=FR" -keysize 1024 -validity 365 -storepass $KSPWD -keypass $KSPWD
if [ $? -eq 0 ]; then 
  echo "done."
  echo ""
  echo "Keystore file created: keystore.jks"
  else
  echo ""
  echo "An eror occured"
fi
echo ""
