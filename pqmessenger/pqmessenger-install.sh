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
KEYSTOREFILE=keystore.jks
CONFFILESDIR=sys-resources
INSTALLDIR=/opt/pqmessenger
LOGDIR=/var/log/pqmessenger
RUNDIR=/var/run/pqmessenger
PARAMDIR=
JMSLOGIN=
JMSPWD=
UNINSTALL=1
JMSMODIFIED=1

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
    $CMD pqmessenger remove 2>/dev/null 
    rm -f /etc/init.d/pqmessenger
    rm -f /etc/default/pqmessenger
  fi
  rm -rf $INSTALLDIR
  CMD=$(command -v find)
  local DIRNAME=$($CMD /etc -name "pqparams.dat")
  if [ ! -z $DIRNAME ]; then
    DIRNAME=$(dirname $DIRNAME)
    if [ -d $DIRNAME ]; then
      rm -f $DIRNAME/$CONFFILE
      rm -f $DIRNAME/$LOG4JFILE
      rm -f $DIRNAME/$KEYSTOREFILE
    fi
  fi
  rm -rf $LOGDIR
  rm -rf $RUNDIR
  rm -f /usr/lib/tmpfiles.d/pqmessenger.conf
  rm -f /etc/rsyslog.d/pqmessenger.conf
  /etc/init.d/rsyslog restart
  echo "Uninstallation done."
}

createKeystore() {
  echo "Keystore install .."
  local CMD=$(command -v keytool)
  local KSFILE=$PARAMDIR/$KEYSTOREFILE
  if [ -z "$CMD" ]; then
    echo "Error! keytool not found"
    return
  fi
  $CMD -genkey -alias pqmessenger -keyalg RSA -keystore $KSFILE -dname "CN=pqMessenger, OU=pqChecker, O=PPolicy, L=LDAPPPolicy, S=IDF, C=FR" -keysize 1024 -validity 365 -storepass $JMSPWD -keypass $JMSPWD
  if [ $? -eq 0 ]; then 
    echo "done."
    echo "File created: $KSFILE"
    echo "+-------------------------------------------------------------------------------+"
    echo "|                                SECURITY WARNING!                              |"
    echo "+-------------------------------------------------------------------------------+"
    echo "| A certificate is generated within the keystore file, it has the settings:     |"
    echo "| Key size: 1024                                                                |"
    echo "| Validity: 365 days (must be renewed before expiration)                        |"
    echo "|                                                                               |"
    echo "| You may recreate it by hand (using the JDK keytool or using the               |"
    echo "| pqmessenger-createkeystore.sh script but THE SAME KEYSTORE MUST BE USED IN    |"
    echo "| THE JMS SERVER SIDE, you must copy it to the configuration location of this   |"
    echo "| server.                                                                       |"
    echo "+-------------------------------------------------------------------------------+"   
    else
    echo "An eror occured while generating keystore, try do this manually."
  fi
  echo ""
}

readPassword() {
  while [ "$PWD1" == "" ] || [ "$PWD1" != "$PWD2" ]
  do 
    echo -ne "Password <$JMSPWD>: "
    read -s PWD1
    echo
    echo -ne "Confirm <$JMSPWD>: "
    read -s PWD2
    echo
    if [ -z $PWD1 ] && [ -z $PWD2 ]; then
      PWD1=$JMSPWD
      PWD2=$JMSPWD
    fi
    if [ "$PWD1" == "" ]; then
      echo "Password cannot empty!"
    elif [ "$PWD1" != "$PWD2" ]; then
      echo "Password confirmation error, reedit !"
    fi
    echo ""
  done
  if [ "$LOGIN" != "$JMSLOGIN" ]; then
    JMSMODIFIED=0
  fi
  JMSPWD=$PWD1
}

setJMSCredential() {
  JMSLOGIN=$(grep Login $CONFFILE | awk -F '<Login>' '{ print $2 }'| awk -F '<' '{ print $1 }')
  JMSPWD=$( grep Password $CONFFILE | awk -F '<Password encrypted="false">' '{ print $2 }'| awk -F '<' '{ print $1 }')
  local LOGIN=""
  while [ "$LOGIN" == "" ]
  do 
    LOGIN=$JMSLOGIN
    echo -ne "Login <$JMSLOGIN>: "
    read LOGIN
    echo
    if [ -z $LOGIN ]; then
      LOGIN=$JMSLOGIN
    fi
  done
  if [ "$LOGIN" != "$JMSLOGIN" ]; then
    JMSMODIFIED=0
  fi
  JMSLOGIN=$LOGIN
  readPassword
  if [ $JMSMODIFIED -eq 0 ]; then
    local CFILE=$(basename $CONFFILE)
    CFILE=$PARAMDIR/$CFILE
    echo "Default credentails modified, modiying settings in $CFILE .."
    sed -i "s/<Login>.*<\/Login>/<Login>$JMSLOGIN<\/Login>/g" $CFILE
    sed -i "s/<Password encrypted=\"false\">.*<\/Password>/<Password encrypted=\"false\">$JMSPWD<\/Password>/g" $CFILE
    echo "Modification done."
  fi
  createKeystore
}

setJMSConfig() {
  echo "Communication with the JMS server settings:"
  setJMSCredential
}

chownDirs() {
  local DIR=$(find /etc -name 'slapd.d')
  local USER=""
  local PRMDIR=$(find /etc -name "pqparams.dat")
  if [ ! -z $PRMDIR ]; then
    PRMDIR=$(dirname $PRMDIR)
  fi
  USER=$(ls -ld /etc/ldap/slapd.d/ | awk '{print $3}')
  if [ -z $USER ]; then
    USER=openldap
  fi
  USER=$(id -un $USER 2>/dev/null)
  if [ -z $USER ]; then
    USER=ldap
    USER=$(id -un $USER 2>/dev/null)
  fi
  if [ -z $USER ]; then
    echo "Cannot found openldap system user."
    echo "To complete installation, you should manually change owner of:"
    echo " $LOGDIR"
    echo " $LOGDIR"
    echo " $INSTALLDIR"
   else
    chown $USER:$USER -R $LOGDIR
    chown $USER:$USER -R $RUNDIR
    chown $USER:$USER -R $INSTALLDIR
    if [ ! -z $PRMDIR ]; then
      chown $USER:$USER -R $PRMDIR
    fi
    local TAG=$(egrep "#PQMESSENGER_USER" /etc/default/pqmessenger | egrep "$USER")
    if [ ! -z $TAG ]; then
      sed -i 's/^PQMESSENGER_USER/# PQMESSENGER_USER/g' /etc/default/pqmessenger
      sed -i 's/#PQMESSENGER_USER/PQMESSENGER_USER/g' /etc/default/pqmessenger
      sed -i 's/# PQMESSENGER_USER/#PQMESSENGER_USER/g' /etc/default/pqmessenger
    fi
  fi
}

customizeParams() {
  setJMSConfig
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
  mkdir -p $INSTALLDIR
  cp -p $JARFILE $INSTALLDIR
  mkdir -p $LOGDIR
  mkdir -p $RUNDIR
  cp -p $TMPFILE /usr/lib/tmpfiles.d/pqmessenger.conf
  cp -p $LOGCONFFILE /etc/rsyslog.d/pqmessenger.conf
  cp -p $LOG4JFILE $PARAMDIR
  cp -p $CONFFILE $PARAMDIR
  customizeParams
  chownDirs
  local CMDCTL=$(command -v update-rc.d)
  if [ -z $CMDCTL ]; then
    CMDCTL=$(command -v chkconfig)
    if [ ! -z $CMDCTL ]; then
      $CMDCTL pqmessenger on 2>/dev/null
    fi
   else
    $CMDCTL pqmessenger defaults 2>/dev/null
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
