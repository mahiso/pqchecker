#!/bin/bash
# chkconfig: 2345 55 25
#
# processname: pwmessenger
# pidfile: /var/run/pqmessenger/pqmessenger.pid
#
### BEGIN INIT INFO
# Provides:          pqmessenger
# Required-Start:    $syslog
# Required-Stop:     $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: pqMessenger daemon
### END INIT INFO
#
# pqMessenger starting shell script
# (c) 2015 Abdelhamid MEDDEB <abdelhamid@meddeb.net>

if [ -e /lib/lsb/init-functions ]; then
  . /lib/lsb/init-functions
fi

if [ -r /etc/default/pqmessenger ]; then
	. /etc/default/pqmessenger
fi

if [ ! -z $JAVA_HOME ]; then
  JBIN=$(which java)
  JAVA_HOME=$(/bin/readlink -f $JBIN | /bin/sed "s:/bin/java::" | /bin/sed "s:/jre::")
fi

PQMESSENGER_BOOTSTRAP="net.meddeb.pqmessenger.MsgDaemon"
PIDFILE="$RUN_HOME/pqmessenger.pid"
OUTFILE="$LOG_HOME/pqmessenger.out"
CLASSPATH="$PQMESSENGER_HOME/*.jar"
PQMESSENGER_ARGS="--config-path $CONFIG_HOME --config-file $CONFIG_FILE --config-log $CONFIGLOG_FILE --msg-server-id $MSGSERVER_ID --connection-retry-time $CONNECTION_RETRY_TIME"
JSVC_ARGS="-home $JAVA_HOME -user $PQMESSENGER_USER -pidfile $PIDFILE -outfile $OUTFILE -errfile &1 -cp $CLASSPATH"
JAVA_ARGS="-Djava.library.path=$NATIVE_LIB_HOME"
RETVAL=0
GRNTXT='\e[0;32m'
REDTXT='\e[0;31m'
REGTXT='\e[0m'

# return pqMessegner starting aguments
pqmessengerARGS() {
  ARGS=""
  if [ ! -z $CONFIG_HOME ]; then
    ARGS="--config-path $CONFIG_HOME"
  fi
}

# pid function, return pqMessenger pid
pqmessengerPid() {
  PID=$( ps -ef | grep "$PQMESSENGER_BOOTSTRAP" | grep -v grep | grep -v root | awk '{print $2}' )
  echo $PID
}

# print in red color
showRed() {
  echo -ne $REDTXT
  echo -e "$1$REGTXT"
}

# print in green color
showGreen() {
  echo -ne $GRNTXT
  echo -e "$1$REGTXT"
}

# start function
do_start(){
  if [ -z $JAVA_HOME ]; then
    showRed "Error."
    echo "Cannot find Java location."
    exit 1
  fi  
  JAVAVERSION=$(java -version 2>&1 | awk '/version/{print $NF}' | sed "s:\"::" | cut -d '.' -f2)
  if [ $JAVAVERSION -lt 7 ]; then
    showRed "Error."
    echo "pqMessenger require Java 1.7 or above."
    exit 1
  fi  
  DAEMONPID=$( pqmessengerPid )
  if [ -z $DAEMONPID ]; then
    echo -n "Starting pqMessenger... "
    $JSVC \
    $JSVC_ARGS \
    $JAVA_ARGS \
    $PQMESSENGER_BOOTSTRAP \
    $PQMESSENGER_ARGS
    RETVAL=$?
    if [ $RETVAL -eq 0 ]; then
      showGreen "Ok."
    else
      showRed "Error."
    fi 
  else
    showRed "Already running"
    RETVAL=0
  fi       
}

# stop function
do_stop(){
  DAEMONPID=$( pqmessengerPid )
  if [ ! -z $DAEMONPID ]; then
    echo -n "Stopping pqMessenger... "
    $JSVC \
    $JSVC_ARGS \
    -stop \
    $PQMESSENGER_BOOTSTRAP
    RETVAL=$?
    if [ $RETVAL -eq 0 ]; then
      showGreen "Ok."
    else
      showRed "Error."
    fi
  else      
    showRed "Not running"
    RETVAL=0
  fi
}

# status function
do_status(){
  DAEMONPID=$( pqmessengerPid )
  if [ -z $DAEMONPID ]; then
    showRed "Stopped."
   else
    showGreen "Running."
  fi    
}

# main
case "$1" in
  start)
    do_start;
    ;;
  stop)
    do_stop
    ;;
  restart)
 	  do_stop
	  sleep 1
	  do_start
	  ;;
  status)
    do_status
    ;;  
  *)
    echo "Usage: $0 start|stop|restart|status" >&2
    exit 3
    ;;
esac
