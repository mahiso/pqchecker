/*--------------------------------------------------------------------
ppInspect, shared library plug-in for OpenLDAP server / ppolicy overlay
Control of password quality.
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
#include <net_meddeb_pqmessenger_JNIGateway.h>
#include <pqchecker.h>

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
