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

#include <stdbool.h>

#define SHMDEFAULTNBRECORDS 50000
#define SHMFIELDSIZE 196
#define SHMNAME "000h01e02d04i15l21.shm"

bool shmInit(const unsigned int nbRecords);
void getShmSize(unsigned int size);
bool shmPush(const char *data);
bool shmPop();
bool shmGet(char *data);
void setCacheData(bool cacheData);
bool isCacheData();
bool isShmInitialized();
