#!/bin/bash
# pqMessenger Middleware for pqChecker installation utility
# v2.0
# (C) Abdelhamid MEDDEB <abdelhamid@meddeb.net>
#
VERSION=2.0.0
JARFILE=pqmessenger-$VERSION.jar
BOOTFILE=pqmessenger.boot
PARAMFILE=pqmessenger.params
TMPCONFFILE=pqmessenger.conf.tmp
LOGCONFFILE=pqmessenger.conf.rsyslog
LOG4JFILE=log4j2.xml
CONFFILE=config.xml
CONFFILESDIR=sys-resources
PARAMDIR=
UNINSTALL=1

showHeader() {
  echo ""
  echo "pqMessenger Middleware for pqChecker version $VERSION"
  echo ""
}
showUsage() {
  echo ""
  echo "Usage: $1"
  echo "          Install pqMessenger"
  echo "Or"
  echo "       $1 --uninstall"
  echo "          Uninstall pqMessenger"
  echo "Or"
  echo "       $1 --help|-h"
  echo "          Show this help message"
  echo ""
}

checkUser() {
  USERID=$(id -u)
  if [ $USERID -ne 0 ]; then
    echo "Error, must be run as root user"
    echo ""
    exit 1
  fi
}

checkArg() {
  if [ $2 -gt 1 ]; then
    showUsage $1
    exit 1
  fi
  if [ "$3" == "-h" ] || [ "$3" == "--help" ]; then
    showUsage $1
    exit 0
  fi
  if [ "$3" == "--uninstall" ]; then
    UNINSTALL=0
    return
  fi
  if [ ! -z $3 ]; then
    showUsage $1
    exit 1
  fi
}

checkFiles() {
  local PWD=$(pwd)
  local RSLT=0
  local FILENAME=$(find $PWD -name "$JARFILE" | grep -v tmp | head -1)
  if [ -z $FILENAME ]; then
    echo "$JARFILE not found."
    RSLT=1
    else
    JARFILE=$FILENAME
  fi
  PWD=$PWD/$CONFFILESDIR
  FILENAME=$(find $PWD -name "$BOOTFILE" | head -1)
  if [ -z $FILENAME ]; then
    echo "$BOOTFILE not found."
    RSLT=1
    else
    BOOTFILE=$FILENAME
  fi
  FILENAME=$(find $PWD -name "$PARAMFILE" | head -1)
  if [ -z $FILENAME ]; then
    echo "$PARAMFILE not found."
    RSLT=1
    else
    PARAMFILE=$FILENAME
  fi
  FILENAME=$(find $PWD -name "$LOGCONFFILE" | head -1)
  if [ -z $FILENAME ]; then
    echo "$LOGCONFFILE not found."
    RSLT=1
    else
    LOGCONFFILE=$FILENAME
  fi
  FILENAME=$(find $PWD -name "$TMPCONFFILE" | head -1)
  if [ -z $FILENAME ]; then
    echo "$TMPCONFFILE not found."
    RSLT=1
    else
    TMPFILE=$FILENAME
  fi
  FILENAME=$(find $PWD -name "$LOG4JFILE" | head -1)
  if [ -z $FILENAME ]; then
    echo "$LOG4JFILE not found."
    RSLT=1
    else
    LOG4JFILE=$FILENAME
  fi
  FILENAME=$(find $PWD -name "$CONFFILE" | head -1)
  if [ -z $FILENAME ]; then
    echo "$CONFFILE not found."
    RSLT=1
    else
    CONFFILE=$FILENAME
  fi
  return $RSLT
}

checkOSInstall() {
  local RSLT=1
  PARAMDIR=$(find /etc -name "pqparams.dat")
  if [ ! -z $PARAMDIR ]; then
    RSLT=0
    PARAMDIR=$(dirname $PARAMDIR)
    else
    echo "Look like pqChecker not installed yet."  
    return $RSLT  
  fi  
  local JSVC=$(command -v jsvc)
  if [ -z $JSVC ]; then
    echo "The 'jsvc' utility is not installed."
    echo "Please install it before"
   else
    RSLT=0
  fi 
  return $RSLT
}

stopMessenger() {
  local RUNNING=$(ps -ef | grep -i pqmessenger | grep -v grep)
  if [ ! -z "RUNNING" ]; then
    /etc/init.d/pqmessenger stop
  fi
}

uninstall() {
  echo "Uninstallation.."
  stopMessenger
  local CMD=$(command -v update-rc.d)
  if [ ! -z $CMD ]; then
    $CMD pqmessenger disable
    $CMD pqmessenger remove
    rm -f /etc/init.d/pqmessenger
    rm -f /etc/default/pqmessenger
  fi
  rm -rf /opt/pqmessenger
  local DIRNAME=($find /etc -name "pqparams.dat")
  if [ ! -z $DIRNAME ]; then
    DIRNAME=$(dirname $DIRNAME)
    if [ -d $DIRNAME ]; then
      rm -f $DIRNAME/$CONFFILE
      rm -f $DIRNAME/$LOG4JFILE
    fi
  fi
  rm -rf /var/log/pqmessenger
  rm -rf /var/run/pqmessenger
  rm -f /usr/lib/tmpfiles.d/pqmessenger.conf
  rm -f /etc/rsyslog.d/pqmessenger.conf
  /etc/init.d/rsyslog restart
  echo "Uninstallation done."
}

chownDirs() {
  local DIR=$(find /etc -name 'slapd.d')
  local USER=""
  if [ ! -z $DIR ]; then
    USER=$(ls -ld /etc/ldap/slapd.d/ | awk '{print $3}')
    if [ -z $USER ]; then
      USER=openldap
    fi
  fi
  USER=$(id -un $USER 2>/dev/null)
  if [ -z $USER ]; then
    USER=ldap
    USER=$(id -un $USER 2>/dev/null)
  fi
  if [ -z $USER ]; then
    echo "Cannot found openlidap system user."
    echo "To complete installation, you should manually change owner of:"
    echo " /var/log/pqmessenger"
    echo " /var/run/pqmessenger"
    echo " /opt/pqmessenger"
   else
    chown $USER:$USER -R /var/log/pqmessenger
    chown $USER:$USER -R /var/run/pqmessenger
    chown $USER:$USER -R /opt/pqmessenger
    local TAG=$(egrep "#PQMESSENGER_USER" /etc/default/pqmessenger | egrep "$USER")
    if [ ! -z $TAG ]; then
      sed -i 's/^PQMESSENGER_USER/# PQMESSENGER_USER/g' /etc/default/pqmessenger
      sed -i 's/#PQMESSENGER_USER/PQMESSENGER_USER/g' /etc/default/pqmessenger
      sed -i 's/# PQMESSENGER_USER/#PQMESSENGER_USER/g' /etc/default/pqmessenger
    fi
  fi
}

customizeParams() {
  local DIRNAME=$(find /etc -name "pqparams.dat")
  local TAG=
  if [ ! -z $DIRNAME ]; then
    DIRNAME=$(dirname $DIRNAME)
    TAG=$(egrep "#CONFIG_HOME" /etc/default/pqmessenger | egrep "$DIRNAME")
    if [ ! -z $TAG ]; then
      sed -i 's/^CONFIG_HOME/# CONFIG_HOME/g' /etc/default/pqmessenger
      sed -i 's/#CONFIG_HOME/CONFIG_HOME/g' /etc/default/pqmessenger
      sed -i 's/# CONFIG_HOME/#CONFIG_HOME/g' /etc/default/pqmessenger
    fi
  fi
}

install() {
  echo "Installation.."
  checkFiles
  local RSLT=$?
  if [ $RSLT -ne 0 ]; then
    echo "Error configuration file(s) not found."
    echo ""
    exit 1
  fi
  checkOSInstall
  RSLT=$?
  if [ $RSLT -ne 0 ]; then
    echo "Error, required installation not found."
    echo ""
    exit 1
  fi
  chmod +x $BOOTFILE
  cp -p $BOOTFILE /etc/init.d/pqmessenger
  cp -p $PARAMFILE /etc/default/pqmessenger
  mkdir -p /opt/pqmessenger
  cp -p $JARFILE /opt/pqmessenger
  mkdir -p /var/log/pqmessenger
  mkdir -p /var/run/pqmessenger
  cp -p $TMPFILE /usr/lib/tmpfiles.d/pqmessenger.conf
  cp -p $LOGCONFFILE /etc/rsyslog.d/pqmessenger.conf
  cp -p $LOG4JFILE $PARAMDIR
  cp -p $CONFFILE $PARAMDIR
  chownDirs
  customizeParams
  local CMDCTL=$(command -v update-rc.d)
  if [ -z $CMDCTL ]; then
    CMDCTL=$(command -v chkconfig)
    if [ ! -z $CMDCTL ]; then
      $CMDCTL pqmessenger on 
    fi
   else
    $CMDCTL pqmessenger defaults 
  fi
  /etc/init.d/rsyslog restart
  echo "Installation done."
}

## main
showHeader
checkUser
checkArg $0 $# $1
if [ $UNINSTALL -eq 0 ]; then
  uninstall
  else
  install
fi  
echo ""
