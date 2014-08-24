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

#define _POSIX_SOURCE 1

#include <ctype.h>
#include <string.h>
#include <stdbool.h>
#include <portable.h>
#include <syslog.h>
#include <stdio.h>
#include <libintl.h>
#include <locale.h>

#include <pqcheck.h>

pp_status_t getStatus(char *pwd, char *forbiddens)
{
  pp_status_t rslt;
  rslt.upperNbr = 0;
  rslt.lowerNbr = 0;
  rslt.digitNbr = 0;
  rslt.specialNbr = 0;
  rslt.forbiddenNbr = 0;
	int pwdLen = strlen(pwd);
	int frbdLen = strlen(forbiddens);
  int i = 0;
  int j = 0;
	for (i = 0; i<pwdLen; i++) 
  {
    if (isupper(pwd[i]))
    {
      rslt.upperNbr++;
    }else
    if (islower(pwd[i]))
    {
      rslt.lowerNbr++;
    }else
    if (isdigit(pwd[i]))
    {
      rslt.digitNbr++;
    }else
    if (ispunct(pwd[i]))
    {
      rslt.specialNbr++;
    }
    for (j = 0; j<frbdLen; j++)
    {
      if (pwd[i] == forbiddens[j]) rslt.forbiddenNbr++;
    }  
  }
  return rslt;
}
/*-------------------------------------------------------------
* Return pp_params_t from char* params according the fmt format
* When params is not operable all fields is set to -1
---------------------------------------------------------------*/
pp_params_t getParams(char *params, char *fmt)
{
  pp_params_t rslt;
  rslt.upperMin = -1;
  rslt.lowerMin = -1;
  rslt.digitMin = -1;
  rslt.specialMin = -1;
  rslt.forbiddens[0] = '\0';
  if (isParamOperable(params))
  {
    int i =0;
    // set mins characters
    char minParam[3];
    minParam[2] = '\0';
    for (i=0; i<4; i++)
    {
      minParam[0] = params[i*2];
      minParam[1] = params[i*2+1];
      switch (fmt[i])
      {
        case 'U':
          rslt.upperMin = atoi(minParam);
          break;
        case 'S':
          rslt.specialMin = atoi(minParam);
          break;  
        case 'D':
          rslt.digitMin = atoi(minParam);
          break;  
        case 'L':
          rslt.lowerMin = atoi(minParam);
          break;  
      }
    }
    if ((rslt.upperMin > -1) &&
        (rslt.lowerMin > -1) &&
        (rslt.digitMin > -1) &&
        (rslt.specialMin > -1))
    {  
      // set forbidden characters
      i = 8;
      int paramsLen = strlen(params) - 1;
      while (i < PARAMS_DATA_MAXLEN)
      {
        if (i < paramsLen) rslt.forbiddens[i - 8] = params[i];
        else rslt.forbiddens[i-8] = '\0';
        i++;
      }
    }
  }   
  return rslt;
}

// check if params is operable
bool isParamOperable(const char * params)
{
  if (!params) return false;
  int i = 0;
  int paramsLen = strlen(params);
  bool rslt = (paramsLen > 7);
  while ((i < 8) && (rslt))
  {
    rslt = isdigit(params[i]);
    i++;
  }
  if (!rslt) syslog(LOG_ERR, _("The parameter [%s] does not operable"),params);
  return rslt;
}

// check if format is operable
bool isFormatOperable(const char * fmt)
{
  if (!fmt) return false;
  int i = 0;
  int count = 0;
  int fmtLen = strlen(fmt);
  bool rslt = (fmtLen == 4);
  while ((i < 4) && (rslt))
  {
    switch (fmt[i])
    {
      case 'U':
        count++;
        break;
      case 'S':
        count = count + 10;
        break;  
      case 'D':
        count = count + 20;
        break;  
      case 'L':
        count = count + 30;
        break;  
      default : rslt = false;
    }
    i++;
  }
  if (rslt) rslt = (count == 61);
  if (!rslt) syslog(LOG_ERR,_("The format [%s] does not operable"), fmt);
  return rslt;
}
