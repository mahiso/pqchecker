#!/bin/bash
# pqMessenger Middleware for pqChecker installation tool
# v2.0
# (C) Abdelhamid MEDDEB <abdelhamid@meddeb.net>
#
VERSION=2.0.0
JARFILE=pqmessenger-$VERSION.jar
BOOTFILE=pqmessenger.boot
PARAMFILE=pqmessenger.params
TMPCONFFILE=pqmessenger.conf.tmp
LOGCONFFILE=pqmessenger.conf.rsyslog
SRVCCONFFILE=pqmessenger.service
MANFILE=pqmessenger.3
MANDIR="/usr/local/share/man/man3"
LOG4JFILE=log4j2.xml
CONFFILE=config.xml
KEYSTOREFILE=keystore.jks
CONFFILESDIR=sys-resources
INSTALLDIR=/opt/pqmessenger
LOGDIR=/var/log/pqmessenger
PARAMDIR=
JMSLOGIN=
JMSPWD=
UNINSTALL=1
JMSMODIFIED=1
INITYPE="SYSD"

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
  local FILENAME=$(find $PWD -name "$JARFILE" 2>/dev/null | grep -v tmp | head -1)
  if [ -z $FILENAME ]; then
    echo "$JARFILE not found."
    RSLT=1
    else
    JARFILE=$FILENAME
  fi
  PWD=$PWD/$CONFFILESDIR
  FILENAME=$(find $PWD -name "$BOOTFILE" 2>/dev/null | head -1)
  if [ -z $FILENAME ]; then
    echo "$BOOTFILE not found."
    RSLT=1
    else
    BOOTFILE=$FILENAME
  fi
  FILENAME=$(find $PWD -name "$PARAMFILE" 2>/dev/null | head -1)
  if [ -z $FILENAME ]; then
    echo "$PARAMFILE not found."
    RSLT=1
    else
    PARAMFILE=$FILENAME
  fi
  FILENAME=$(find $PWD -name "$LOGCONFFILE" 2>/dev/null | head -1)
  if [ -z $FILENAME ]; then
    echo "$LOGCONFFILE not found."
    RSLT=1
    else
    LOGCONFFILE=$FILENAME
  fi
  FILENAME=$(find $PWD -name "$TMPCONFFILE" 2>/dev/null | head -1)
  if [ -z $FILENAME ]; then
    echo "$TMPCONFFILE not found."
    RSLT=1
    else
    TMPFILE=$FILENAME
  fi
  FILENAME=$(find $PWD -name "$LOG4JFILE" 2>/dev/null | head -1)
  if [ -z $FILENAME ]; then
    echo "$LOG4JFILE not found."
    RSLT=1
    else
    LOG4JFILE=$FILENAME
  fi
  FILENAME=$(find $PWD -name "$CONFFILE" 2>/dev/null | head -1)
  if [ -z $FILENAME ]; then
    echo "$CONFFILE not found."
    RSLT=1
    else
    CONFFILE=$FILENAME
  fi
  FILENAME=$(find $PWD -name "$SRVCCONFFILE" 2>/dev/null | head -1)
  if [ -z $FILENAME ]; then
    echo "$SRVCCONFFILE not found."
    RSLT=1
    else
    SRVCCONFFILE=$FILENAME
  fi
  return $RSLT
}

checkOSInstall() {
  local RSLT=1
  PARAMDIR=$(find /etc -name "pqparams.dat")
  if [ -n "$PARAMDIR" ]; then
    PARAMDIR=$(dirname $PARAMDIR)
    else
    echo "Look like pqChecker not installed yet."  
    return $RSLT  
  fi  
  local JSVC=$(command -v jsvc)
  if [ -z "$JSVC" ]; then
    echo "The 'jsvc' utility is not installed."
    echo "Please install it before"
   else
    RSLT=0
  fi 
  return $RSLT
}

setIniType() {
  local OSVER=0
  INITYPE="DEBIAN"
  if [ -e /etc/debian_version ]; then
      OSVER=$(head -1 /etc/debian_version 2>/dev/null | tr -d -c 0-9 | cut -c 1)
      if [ $OSVER -gt 7 ]; then
        INITYPE="SYSD"
      fi
  elif [ -e /etc/redhat-release ]; then
      OSVER=$(head -1 /etc/redhat-release 2>/dev/null | tr -d -c 0-9 | cut -c 1)
      if [ $OSVER -gt 6 ]; then
        INITYPE="SYSD"
        else
        INITYPE="RHEL"
      fi
  fi
}

disableBoot() {
  local RUNNING=$(ps -ef | grep -i java | grep -i pqmessenger | grep -v grep)
  case "$INITYPE" in
  SYSD):
    CMD=$(command -v systemctl)
    if [ -n "$RUNNING" ]; then
      $CMD stop pqmessenger
    fi
    $CMD disable pqmessenger
    rm -f /usr/bin/pqmessenger
    rm -f /etc/rsyslog.d/pqmessenger.conf
    rm -f /etc/systemd/system/pqmessenger.service
    $CMD restart rsyslog
    ;;
  DEBIAN):
    CMD=$(command -v service)
    if [ -n "$RUNNING" ]; then
      if [ -n "$CMD" ]; then
        $CMD pqmessenger stop
      else
        /etc/init.d/pqmessenger stop
      fi
    fi
    rm -f /etc/rsyslog.d/pqmessenger.conf
    $CMD rsyslog restart
    CMD=$(command -v update-rc.d)
    $CMD pqmessenger remove 2>/dev/null 
    rm -f /etc/init.d/pqmessenger
    ;;
  RHEL):
    CMD=$(command -v service)
    if [ -n "$RUNNING" ]; then
      if [ -n "$CMD" ]; then
        $CMD pqmessenger stop
      else
        /etc/init.d/pqmessenger stop
      fi
    fi
    rm -f /etc/rsyslog.d/pqmessenger.conf
    $CMD rsyslog restart
    CMD=$(command -v chkconfig)
    $CMD --del pqmessenger 2>/dev/null 
    rm -f /etc/init.d/pqmessenger
    ;;
  esac
}

uninstall() {
  echo "Uninstallation.."
  disableBoot
  rm -f /etc/default/pqmessenger
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
  rm -f $MANDIR/$MANFILE
  rm -f /usr/lib/tmpfiles.d/pqmessenger.conf
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
    echo "| A certificate is generated within the keystore.jks file, it has the settings: |"
    echo "| - Key size: 1024                                                              |"
    echo "| - Validity: 365 days (must be renewed before expiration)                      |"
    echo "| You may recreate it by hand (using the Java 'keytool' tool or using the       |"
    echo "| 'pqmessenger-createkeystore.sh' script but THE SAME KEYSTORE MUST BE USED IN  |"
    echo "| THE JMS SERVER SIDE. So, you must copy it to the configuration location of    |"
    echo "| this server.                                                                  |"
    echo "+-------------------------------------------------------------------------------+"   
    else
    echo "An eror occured while generating keystore, try do this manually."
  fi
  echo ""
}

readPassword() {
  local PWDLEN=0
  while [ "$PWD1" == "" ] || [ "$PWD1" != "$PWD2" ] || [ $PWDLEN -lt 6 ]
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
    PWDLEN=${#PWD2}
    if [ "$PWD1" == "" ]; then
      echo "Password cannot empty!"
    elif [ "$PWD1" != "$PWD2" ]; then
      echo "Password confirmation error, reedit !"
    elif [ $PWDLEN -lt 6 ]; then
      echo "Password size must be greater than 6 chars"  
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
  USER=$(ls -ld /etc/ldap/slapd.d/ 2>/dev/null | awk '{print $3}')
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
    echo " $INSTALLDIR"
   else
    chown $USER:$USER -R $LOGDIR
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

installMan() {
  local PWD=$(pwd)
  local FULLMAN=$(find $PWD -name $MANFILE 2>/dev/null | head -1)
  if [ -z "$FULLMAN" ]; then
    PWD="$PWD/.."
    FULLMAN=$(find $PWD -name $MANFILE 2>/dev/null | head -1)
  fi
  if [ ! -z "$FULLMAN" ]; then
    cp -p $FULLMAN $MANDIR
  fi
}

install() {
  echo "Installation.."
  checkFiles
  local RSLT=$?
  if [ $RSLT -ne 0 ]; then
    echo "Error, configuration file(s) not found."
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
  cp -p $PARAMFILE /etc/default/pqmessenger
  mkdir -p $INSTALLDIR
  cp -p $JARFILE $INSTALLDIR
  mkdir -p $LOGDIR
  cp -p $TMPFILE /usr/lib/tmpfiles.d/pqmessenger.conf
  cp -p $LOGCONFFILE /etc/rsyslog.d/pqmessenger.conf
  cp -p $LOG4JFILE $PARAMDIR
  cp -p $CONFFILE $PARAMDIR
  installMan
  customizeParams
  chownDirs
  case "$INITYPE" in
  SYSD):
    CMD=$(command -v systemctl)
    cp -p $BOOTFILE /usr/bin/pqmessenger
    cp -p $SRVCCONFFILE /etc/systemd/system/pqmessenger.service
    $CMD enable pqmessenger
    $CMD restart rsyslog
    $CMD start pqmessenger
    ;;
  DEBIAN):
    cp -p $BOOTFILE /etc/init.d/pqmessenger
    CMD=$(command -v update-rc.d)
    if [ -n "$CMD" ]; then
      $CMD pqmessenger defaults 2>/dev/null 
    fi
    CMD=$(command -v service)
    if [ -n "$CMD" ]; then
      $CMD pqmessenger start
      $CMD rsyslog restart
    else
      /etc/init.d/pqmessenger start
      if [ -x /etc/init.d/rsyslog ]; then
        /etc/init.d/rsyslog restart
      fi
    fi
    ;;
  RHEL):
    cp -p $BOOTFILE /etc/init.d/pqmessenger
    CMD=$(command -v chkconfig)
    if [ -n "$CMD" ]; then
      $CMD pqmessenger on 2>/dev/null 
    fi
    CMD=$(command -v service)
    if [ -n "$CMD" ]; then
      $CMD pqmessenger start
      $CMD rsyslog restart
    else
      /etc/init.d/pqmessenger start
      if [ -x /etc/init.d/rsyslog ]; then
        /etc/init.d/rsyslog restart
      fi
    fi
    ;;
  esac
  echo "Installation done."
}

## main
showHeader
checkUser
checkArg $0 $# $1
setIniType
if [ $UNINSTALL -eq 0 ]; then
  uninstall
  else
  install
fi  
echo ""
