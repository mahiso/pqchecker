/*--------------------------------------------------------------------
pqChecker, shared library plug-in for OpenLDAP server / ppolicy overlay
Checking of password quality.
Copyright (C) 2014, Abdelhamid MEDDEB (abdelhamid@meddeb.net)  

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
---------------------------------------------------------------------*/

#include <ctype.h>
#include <stdio.h>
#include <unistd.h>
#include <syslog.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <stdlib.h>
#include <portable.h>
#include <errno.h>
#include <locale.h>
#include <string.h>

#include <shmap.h>
#include <comm.h>

static char readbuffer[SHMFIELDSIZE];

void broadcastData(const char *data) {
  syslog(LOG_DEBUG, _("Broadcast data, javaVM: %p"), javaVM);
  JNIEnv * env;
  int callstatus = (*javaVM)->GetEnv(javaVM, (void **)&env, JNI_VERSION_1_6);
  switch (callstatus) {
    case JNI_EDETACHED:
      syslog(LOG_INFO, _("Env detached, attaching..: %d"), callstatus);
      int attachstatus = (*javaVM)->AttachCurrentThread(javaVM, (void **)&env, NULL);
      if (attachstatus != 0) {
        syslog(LOG_INFO, _("Error: %d"), attachstatus);
        return;
      } else syslog(LOG_INFO, _("Env OK"));
      break;
    case JNI_EVERSION:
      syslog(LOG_ERR, _("Version error: %d"), callstatus);
      return;
      break;
    case JNI_OK:
      syslog(LOG_INFO, _("Env OK: %d"), callstatus);
      break;
    default :
      syslog(LOG_ERR, _("Broadcast data error: %d"), callstatus);
      return;
      break;
  }
  jclass cls = (*env)->FindClass(env,"net/meddeb/pqmessenger/JNIGateway");
  if (cls == NULL) {
   syslog(LOG_ERR, _("Cannot find class"));
   return;
  }
  jmethodID mid = (*env)->GetMethodID(env, cls, "sendPwd", "(Ljava/lang/String;Ljava/lang/String;)V");
  if (mid == 0) {
   syslog(LOG_ERR, _("Cannot find method"));
   return;
  }
  jobject obj = (*env)->AllocObject(env, cls);
  if (obj != 0) {
    syslog(LOG_DEBUG, _("object ID: %p"), obj);
    const char *cPwdStr;
    const char *cUserStr;
    cPwdStr = data;
    cUserStr = "anonymous";
    jstring jPwdStr = (*env)->NewStringUTF(env, cPwdStr);
    jstring jUserStr = (*env)->NewStringUTF(env, cUserStr);
    (*env)->CallVoidMethod(env, obj, mid, jPwdStr, jUserStr);
  }
}

bool processrequest(int client_socketfd, char *readbuffer) {
  bool rslt = false;
  int readcount = read(client_socketfd, readbuffer, SHMFIELDSIZE);
  if (readcount > 0) {
    syslog(LOG_DEBUG, _("Reveived message: %s"), readbuffer);
    rslt = (strcmp(readbuffer, "quit") == 0);
    if (!rslt) broadcastData(readbuffer);
  } else rslt = true;
  return rslt;
}

bool doListen() {
  struct sockaddr_un addr;
  int socketfd;

  if ((socketfd = socket(AF_UNIX, SOCK_STREAM, 0)) == -1) {
    syslog(LOG_ERR, _("Socket error %d"), errno);
    return false;
  }
  memset(&addr, 0, sizeof(struct sockaddr_un));
  addr.sun_family = AF_UNIX;
  strncpy(addr.sun_path, SCKPATH, sizeof(addr.sun_path)-1);
  unlink(SCKPATH);
  if (bind(socketfd, (struct sockaddr*)&addr, sizeof(struct sockaddr_un)) == -1) {
    const char* strerr = strerror(errno);
    syslog(LOG_ERR, _("Bind error %s"), strerr);
    return false;
  }
  if (listen(socketfd, 5) == -1) {
    syslog(LOG_ERR, _("Socket listen error %d"), errno);
    return false;
  }
  syslog(LOG_DEBUG, _("Listen started!!"));
  struct sockaddr_un client_addr;
  socklen_t client_addr_len;
  bool stoplisten = false;
  int client_socketfd;
  while (!stoplisten) {
    syslog(LOG_DEBUG, _("Listening.."));
    memset(&client_addr, 0, sizeof(struct sockaddr_un));
    client_socketfd = accept(socketfd,(struct sockaddr *) &client_addr, &client_addr_len);
    if (client_socketfd < 0) {
      syslog(LOG_ERR, _("Socket listen error %d"), errno);
    } else stoplisten = processrequest(client_socketfd, readbuffer);
    memset(&readbuffer, 0, SHMFIELDSIZE);
    close(client_socketfd);
  }
  syslog(LOG_DEBUG, _("Listen stopped!!"));
  unlink(SCKPATH);
}

bool stopListen() {
  bool rslt = doSend("quit");
  return rslt;
}

bool doSend(char *data) {
  struct sockaddr_un addr;
  bool rslt = false;
  int fd;
  int rc = strlen(data);
  if ((fd = socket(AF_UNIX, SOCK_STREAM, 0)) == -1) {
    syslog(LOG_ERR, _("Socket error %d"), errno);
    return rslt;
  }
  memset(&addr, 0, sizeof(addr));
  addr.sun_family = AF_UNIX;
  strncpy(addr.sun_path, SCKPATH, sizeof(addr.sun_path)-1);
  if (connect(fd, (struct sockaddr*)&addr, sizeof(addr)) == -1) {
    syslog(LOG_ERR, _("Connection error %d"), errno);
    return rslt;
  }
  if (write(fd, data, rc) != rc) {
    if (rc > 0) syslog(LOG_ERR, _("Partial data write"));
    else {
      syslog(LOG_ERR, _("Write error"));
      return rslt;
    }
  }
  rslt =true;
  return rslt;
}

void doCacheData(char *pwd, char *user) {
  syslog(LOG_DEBUG, _("Caching data.."));
  char cdata[SHMFIELDSIZE];
  unsigned int size = strlen(user);
  memcpy(cdata, (char*)&size, sizeof(unsigned int));
  memcpy(cdata, &user, size);
  size = strlen(pwd);
  memcpy(cdata, &pwd, size);
  shmPush(cdata);
  syslog(LOG_INFO, _("Can't broadcast, data cached locally"));
}

void doBroadcastCacheData() {
  syslog(LOG_DEBUG, _("Broadcasting cached data.."));
  char cdata[SHMFIELDSIZE];
  bool sendSuccess = true;
  while (shmGet(cdata) && sendSuccess) {
    sendSuccess = doSend(cdata);
    if (sendSuccess)  {
      shmPop();
      syslog(LOG_DEBUG, _("Broadcast data."));
    } else syslog(LOG_DEBUG, _("Broadcast fail."));
  }
  syslog(LOG_INFO, _("Done"));
}

void sendPassword(char *pwd, char *user)
{
  syslog(LOG_DEBUG, _("Sending modified password.."));
  if (cacheData) doCacheData(pwd, user); 
  else if (doSend(pwd)) syslog(LOG_DEBUG, _("Modified password successfully sent.."));
      else doCacheData(pwd, user);
}
