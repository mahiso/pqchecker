/*--------------------------------------------------------------------
pqChecker, shared library plug-in for OpenLDAP server / ppolicy overlay
Checking of passwords quality.
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

#undef PACKAGE_BUGREPORT
#undef PACKAGE_NAME
#undef PACKAGE_STRING
#undef PACKAGE_TARNAME
#undef PACKAGE_VERSION

#include <config.h>

#include <pparamio.h>

#define DEFAULT_DATA_FILE "/etc/ldap/pqparams.dat"
#define DEFAULT_PARAMS "01010101"
#define PARAMS_STORAGE_FORMAT "ULDS"
#ifndef PARAMS_DATA_FILE
 #define PARAMS_DATA_FILE DEFAULT_DATA_FILE
#endif

extern char sendPwdTag;

// password policy parameters for password quality
typedef struct {
  bool sendPwd;
  int upperMin;      //min uppercase characters
  int lowerMin;      //min lowercase characters
  int digitMin;      //min digits
  int specialMin;    //min special characters: not alphanumeric
  char forbiddens[PARAMS_DATA_MAXLEN - 8];  //forbidden characters list
} pp_params_t;

// password status for quality check
typedef struct {
  int upperNbr;      //Number of uppercase characters in checked password
  int lowerNbr;      //Number of lowercase characters in checked password
  int digitNbr;      //Number of digits in checked password
  int specialNbr;    //Number of special characters in checked password
  int forbiddenNbr;  //Number of forbidden characters in checked password
} pp_status_t;

pp_params_t
getParams(char *params, char *fmt);

pp_status_t
getStatus(char *pwd, char *forbiddens);

// check if params is operable
bool isParamOperable(const char * params);

// check if format is operable
bool isFormatOperable(const char * fmt);
