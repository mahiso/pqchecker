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

#include <ctype.h>
#include <syslog.h>
#include <stdbool.h>
#include <portable.h>
#include <slap.h>
#include <pparamio.h>
#include <pqcheck.h>
#include <pqchecker.h>

int 
check_password(char *pPasswd, char **ppErrStr, Entry *e);

int 
check_password(char *pPasswd, char **ppErrStr, Entry *e)
{
  openlog("ppInspect", LOG_PID, LOG_LOCAL4);
  syslog(LOG_INFO, "Check password quality for %s", e->e_name.bv_val);
  int rslt = LDAP_OPERATIONS_ERROR;
  char strParams[PARAMS_DATA_MAXLEN+1]; 
  if (readParams(strParams)) 
  {
    pp_params_t params = getParams(strParams, PARAMS_STORAGE_FORMAT);
    pp_status_t pwdStatus = getStatus(pPasswd, params.forbiddens);
    if ((pwdStatus.upperNbr < params.upperMin) ||
        (pwdStatus.lowerNbr < params.lowerMin) ||
        (pwdStatus.digitNbr < params.digitMin) ||
        (pwdStatus.specialNbr < params.specialMin) ||
        (pwdStatus.forbiddenNbr > 0))
    {
      *ppErrStr = strdup("Password does not pass quality check.");
      syslog(LOG_DEBUG, "Password rejected.");
    } else {
      rslt = LDAP_SUCCESS;
      syslog(LOG_DEBUG, "Password accepted.");
    }
  } else rslt = LDAP_OPERATIONS_ERROR;
  closelog();
	return (rslt);
}
