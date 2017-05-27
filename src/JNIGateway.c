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


#include <jni.h>
#include <syslog.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/mman.h>
#include <sys/stat.h>  
#include <fcntl.h>
#include <errno.h>
#include <libintl.h>
#include <portable.h>
#include <locale.h>

#undef PACKAGE_BUGREPORT
#undef PACKAGE_NAME
#undef PACKAGE_STRING
#undef PACKAGE_TARNAME
#undef PACKAGE_VERSION

#include <config.h>

#include <net_meddeb_pqmessenger_JNIGateway.h>
#include <pparamio.h>
#include <pqchecker.h>
#include <shmap.h>
#include <comm.h>

JavaVM *javaVM = NULL;

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
  if (javaVM == NULL) {
    setlocale (LC_ALL, "");
    bindtextdomain (PACKAGE, LOCALEDIR);
    textdomain (PACKAGE);
    openlog(PACKAGE, LOG_CONS | LOG_PID | LOG_NDELAY, LOG_LOCAL5);
    javaVM = vm;
  }
  syslog(LOG_DEBUG, _("OnLoad called, javaVM: %p"), vm);
  unsigned int shmSize = SHMDEFAULTNBRECORDS;
  if (!shmInit(shmSize))
  {
    syslog(LOG_ERR, _("Open shared memory region: %d"), errno);
  }
  return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved)
{
  syslog(LOG_DEBUG, _("!!!OnUnLoad called, javaVM: %p"), javaVM);
}

JNIEXPORT jstring JNICALL 
Java_net_meddeb_pqmessenger_JNIGateway_getParams(JNIEnv *env, jobject thiObj, jstring jFmt)
{
  const char *cFmt = (*env)->GetStringUTFChars(env, jFmt, NULL);
  char strParams[PARAMS_DATA_MAXLEN+1]; 
  bool rslt = false;
  rslt = get_params(strParams, cFmt);
  (*env)->ReleaseStringUTFChars(env, jFmt, cFmt);
  if (rslt)
  {
    return (*env)->NewStringUTF(env, strParams);
  } else return NULL;
}

JNIEXPORT jint JNICALL 
Java_net_meddeb_pqmessenger_JNIGateway_setParams(JNIEnv *env, jobject thisObj, jstring jParams, jstring jFmt){
  const char *cFmt = (*env)->GetStringUTFChars(env, jFmt, NULL);
  const char *cParams = (*env)->GetStringUTFChars(env, jParams, NULL);
  bool rslt = false;
  rslt = set_params(cParams, cFmt);
  (*env)->ReleaseStringUTFChars(env, jFmt, cFmt);
  (*env)->ReleaseStringUTFChars(env, jParams, cParams);
  return  rslt;
}

JNIEXPORT void JNICALL 
Java_net_meddeb_pqmessenger_JNIGateway_setCacheData(JNIEnv *env, jobject thisObj, jboolean jcacheData){
  bool cacheData = jcacheData;
  setCacheData(cacheData);
  syslog(LOG_DEBUG, _("Set cache data to %d"), cacheData);
  if (!cacheData) doBroadcastCacheData();
}

JNIEXPORT void JNICALL 
Java_net_meddeb_pqmessenger_Listener_doListen(JNIEnv *env, jobject thisObj){
  syslog(LOG_DEBUG, _("Start listen request"));
  doListen();
}

JNIEXPORT void JNICALL 
Java_net_meddeb_pqmessenger_JNIGateway_stopListen(JNIEnv *env, jobject thisObj){
  syslog(LOG_DEBUG, _("Stop listen request"));
  stopListen();
}
