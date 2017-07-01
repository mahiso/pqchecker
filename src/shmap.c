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

#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <syslog.h>
#include <locale.h>
#include <portable.h>
#include <errno.h>
#include <stdio.h>
#include <unistd.h>

#include <shmap.h>

static char * shmref = NULL;

// set records number in the shared memory region
void setNbRecords(unsigned int nb) {
  memcpy(shmref, (char*)&nb, sizeof(unsigned int));
}

// get records number in the shared memory region
unsigned int getNbRecords() {
  unsigned int nb;
  memcpy(&nb, shmref, sizeof(unsigned int));
  return nb;
}
    
// unmap shared memory region
void shmUnmap() {
  unsigned int size = SHMDEFAULTNBRECORDS * SHMFIELDSIZE + sizeof(unsigned int);
  size_t memsize = size;
  if (munmap(shmref, memsize) != 0) syslog(LOG_ERR, _("SHM region unmap failed, error: %d"), errno);
}
    
// map shared memory region
bool shmMap() {
  bool rslt = false;
  unsigned int size = SHMDEFAULTNBRECORDS * SHMFIELDSIZE + sizeof(unsigned int);
  mode_t accessmode = S_IRUSR | S_IWUSR;
  int fd = shm_open(SHMNAME, O_RDWR, accessmode);
  if (fd >= 0) {
    size_t memsize = size;
    off_t offset = 0;
    shmref = mmap(NULL, memsize, PROT_WRITE, MAP_SHARED, fd, offset);
    rslt = shmref != MAP_FAILED;
    if (!rslt) syslog(LOG_ERR, _("SHM region map failed, error: %d"), errno);
    close(fd);
  } else syslog(LOG_ERR, _("SHM region open failed, error: %d"), errno);
  return rslt;
}

//store cacheData flag value in the shared memory 
void setCacheData(bool cacheData) {
  unsigned int icacheData = 1;
  if (cacheData) icacheData = 0;
  shmMap();
  memcpy(shmref + sizeof(unsigned int), (char*)&icacheData, sizeof(unsigned int));
  shmUnmap();
}

//get shared memory region initialized flag 
bool isShmInitialized() {
  bool rslt = shmMap();
  if (rslt) shmUnmap();
  return rslt;
}

//get cacheData flag value from the shared memory 
bool isCacheData() {
  unsigned int icacheData;
  shmMap();
  memcpy(&icacheData, shmref + sizeof(unsigned int), sizeof(unsigned int));
  shmUnmap();
  return icacheData == 0;
}

// initialize shared memory region
bool shmInit(const unsigned int nbRecords) {
  bool rslt = false;
  if (access(SHMNAME, F_OK) == 0 ) return true; // already initialized
  unsigned int size = nbRecords * SHMFIELDSIZE + (2 * sizeof(unsigned int));
  mode_t accessmode = S_IRUSR | S_IWUSR;
  mode_t orig_umask = umask(0);
  int fd = shm_open(SHMNAME, O_RDWR | O_CREAT | O_EXCL, accessmode);
  if (fd >= 0) {
    size_t memsize = size;
    if (ftruncate(fd, memsize) == 0) {
      off_t offset = 0;
      shmref = mmap(NULL, memsize, PROT_WRITE, MAP_SHARED, fd, offset);
      rslt = shmref != MAP_FAILED;
      if (rslt) {
        size = 0;
        setNbRecords(size);
        shmUnmap();
      } else syslog(LOG_ERR, _("SHM region map failed, error: %d"), errno);
    } else syslog(LOG_ERR, _("SHM truncate error: %d"), errno);
    close(fd);
  } else if (errno != EEXIST) syslog(LOG_ERR, _("Cannot make SHM region, error: %d"), errno);
          else rslt = true;
  umask(orig_umask);
  return rslt;
}

// get last field from the shared memory region
bool shmGet(char *data) {
  bool rslt = false;
  if (!shmMap()) return rslt;
  unsigned int nb = getNbRecords(shmref);
  if (nb > 0) {
    unsigned int read_offset = (nb - 1) * SHMFIELDSIZE + (2* sizeof(unsigned int));
    char ldata[SHMFIELDSIZE];
    memcpy(ldata, shmref + read_offset, SHMFIELDSIZE);
    int i = 0;
    for (i=0; i<SHMFIELDSIZE; i++) if (ldata[i] != 0) data[i] = ~ldata[i];
    rslt = true;
  }
  shmUnmap();
  return rslt;
}

// erase last field from the shared memory region
bool shmPop() {
  bool rslt = false;
  if (!shmMap()) return rslt;
  unsigned int nb = getNbRecords(shmref);
  if (nb > 0) {
    char data[SHMFIELDSIZE];
    memset(&data, 0, SHMFIELDSIZE);
    unsigned int data_offset = (nb - 1) * SHMFIELDSIZE + (2 * sizeof(unsigned int));
    memcpy(shmref + data_offset, data, SHMFIELDSIZE);
    nb--;
    setNbRecords(nb);
    rslt = true;
  }
  shmUnmap();
  return rslt;
}

// add a field to the shared memory region
bool shmPush(const char *data) {
  bool rslt = false;
  if (!shmMap()) return rslt;
  unsigned int nb = getNbRecords();
  if (nb < SHMDEFAULTNBRECORDS) {
    unsigned int write_offset = nb * SHMFIELDSIZE + (2 * sizeof(unsigned int));
    char ldata[SHMFIELDSIZE];
    int i = 0;
    for (i=0; i<SHMFIELDSIZE; i++) if (data[i] != 0) ldata[i] = ~data[i];
    memcpy(shmref +write_offset, ldata, SHMFIELDSIZE);
    nb++;
    setNbRecords(nb);
    rslt = true;
  } else syslog(LOG_ERR, _("shmPush: shared memory full"));
  shmUnmap();
  return rslt;
}
