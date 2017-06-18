#!/bin/bash
#
# Keystore creator tool for pqMessenger 
# (c) 2015-2017 Abdelhamid MEDDEB <abdelhamid@meddeb.net>
CMD=$(command -v keytool)
if [ -z "$CMD" ]; then
  echo "Error! keytool not found"
  exit 1
fi
echo ""
echo "Keystore generation tool for pqMessenger"
echo ""
echo "keystore creation.."
$CMD -genkey -alias pqmessenger -keyalg RSA -keystore keystore.jks -dname "CN=pqMessenger, OU=pqChecker, O=PPolicy, L=LDAPPPolicy, S=IDF, C=FR" -storepass mdmanager -keypass mdmanager
if [ $? -eq 0 ]; then 
  echo "done."
  echo "File created keystore.jks"
  else
  echo "An eror occured"
fi
echo ""
