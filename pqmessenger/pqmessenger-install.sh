#!/bin/bash
# pqMessenger Middleware for pqChecker installation utility
# v2.0.0
# (C) Abdelhamid MEDDEB <abdelhamid@meddeb.net>
#
VERSION=2.0.0
JARFILE=pqmessenger-$VERSION.jar
BOOTFILE=pqmessenger
PARAMFILE=pqmessenger.params
CONFFILE=pqmessenger.conf

showHeader() {
  echo ""
  echo "---------------------------------------------------------------"
  echo "pqMessenger Middleware for pqChecker version $VERSION installation"
  echo "---------------------------------------------------------------"
  echo ""
}

checkUser() {
  USERID=$(id -u)
  if [ $USERID -ne 0 ]; then
    echo "Error, must be run as root user."
    echo ""
    exit 1
  fi
}

checkFiles() {
}

## main
showHeader
checkUser
