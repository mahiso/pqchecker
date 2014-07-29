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

#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <stdbool.h>
#include <stdlib.h>
#include <syslog.h>
#include <errno.h>

#include <pqcheck.h>
#include <pparamio.h>

bool set_params(const char *params, const char *fmt)
{
  openlog("pqchecker", LOG_PID, LOG_LOCAL4);
  syslog(LOG_ERR, "set_params call.");
  if (!params || !fmt) 
  {
    syslog(LOG_ERR, "No parameters data or format string supplied found.");
    return false;
  }
  bool rslt = false;
  if (isFormatOperable(fmt) && isParamOperable(params))
  {
    char fmtParams[PARAMS_DATA_MAXLEN+1]; 
    strcpy(fmtParams, params);
    int i = 0;
    for (i=0; i<4; i++)
    {
      switch (fmt[i])
      {
        case 'U':
          fmtParams[0] = params[i * 2];
          fmtParams[1] = params[(i * 2) + 1];
          break;
        case 'L':
          fmtParams[2] = params[i * 2];
          fmtParams[3] = params[(i * 2) + 1];
          break;
        case 'D':
          fmtParams[4] = params[i * 2];
          fmtParams[5] = params[(i * 2) + 1];
          break;
        case 'S':
          fmtParams[6] = params[i * 2];
          fmtParams[7] = params[(i * 2) + 1];
          break;
      }
    }
    FILE *fp;
    fp = fopen(PARAMS_DATA_FILE,"r+");
    if (fp == NULL) 
    {
      syslog(LOG_ERR, "Can't open parameters file storage: %s, %s", PARAMS_DATA_FILE, strerror(errno));
      rslt = false;
    } else rslt = true;
    if (rslt)
    {
      bool trv = false;                                    
      int i;
      char line[PARAMS_DATA_MAXLEN];
      int nbLine = 0;
      int iTrv = 0;
      char flines[PARAMS_DATA_MAXLEN][PARAMS_DATA_MAXLEN];
      while (fgets(line,PARAMS_DATA_MAXLEN,fp)!=NULL)
      {
        if (!trv) 
        {
          for (i=0; line[i]; i++)
          {
            if ((line[i] != ' ') && (line[i] != '\t'))
            {
              if (line[i] != '#')
              {
                trv = true;
                iTrv = nbLine;
              }
              break;
            }
          }
        }
        strcpy(flines[nbLine], line);
        nbLine++;  
      }                                                        
      if (trv) strcpy(flines[iTrv], fmtParams);
      fseek(fp, 0, SEEK_SET);
      for (i=0; i<nbLine; i++)
      {
        fputs(flines[i], fp);
      }  
      fputs("\n", fp);
      fclose(fp);
    }
  }
  return rslt;
}

bool get_params(char *params, const char *fmt)
{
  openlog("pqchecker", LOG_PID, LOG_LOCAL4);
  if (!params || !fmt) 
  {
    syslog(LOG_ERR, "No parameters container or format string supplied found.");
    return false;
  }
  bool rslt = false;
  if (isFormatOperable(fmt))
  {
    char strParams[PARAMS_DATA_MAXLEN+1]; 
    char fmtParams[8]; 
    if (readParams(strParams)) 
    {
      int i = 0;
      for (i=0; i<4; i++)
      {
        switch (fmt[i])
        {
          case 'U':
            fmtParams[i * 2] = strParams[0];
            fmtParams[(i * 2) + 1] = strParams[1];
            break;
          case 'L':
            fmtParams[i * 2] = strParams[2];
            fmtParams[(i * 2) + 1] = strParams[3];
            break;
          case 'D':
            fmtParams[i * 2] = strParams[4];
            fmtParams[(i * 2) + 1] = strParams[5];
            break;
          case 'S':
            fmtParams[i * 2] = strParams[6];
            fmtParams[(i * 2) + 1] = strParams[7];
            break;
        }
      }
      for (i=0; i<8; i++) strParams[i] = fmtParams[i];
      strcpy(params, strParams);
      rslt = true;
    } else syslog(LOG_DEBUG, "Can't read parameters from file.");
  } else syslog(LOG_DEBUG, "The format string [%s] supplied isn't operable.", fmt);  
  return rslt;
}
  
/*------------------------------------------------
* Read parameters data from file
* Only the first line who is not a comment is read
--------------------------------------------------*/
bool readParams(char *rslt)
{
  if (!rslt) return false;
  FILE *fp;
  char line[PARAMS_DATA_MAXLEN];
  fp = fopen(PARAMS_DATA_FILE,"r");
  if (fp == NULL) 
  {
    syslog(LOG_ERR, "Cannot open parameters file storage: %s, %s", PARAMS_DATA_FILE, strerror(errno));
    return false;
  }
  bool trv = false;                                    
  int i;
  while (fgets(line,PARAMS_DATA_MAXLEN,fp)!=NULL)
  {
    for (i=0; line[i]; i++)
    {
      if ((line[i] != ' ') && (line[i] != '\t'))
      {
        if (line[i] != '#') trv = true;
        break;
      }
    }
    if (trv) break;
  }                                                       
  fclose(fp);
  if (trv) 
  {
    int iRslt = 0;
    for (i=0; line[i]; i++)
    {
      if ((line[i] != ' ') && (line[i] != '\t'))
      {
        rslt[iRslt++] = line[i];
      }
      rslt[iRslt] = '\0';

    }
    return true;
  } else 
  {
    syslog(LOG_ERR, "No data found in parameters file storage: %s", PARAMS_DATA_FILE);
    return false;
  }
}
