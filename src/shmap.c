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

static char *shmref;

bool isBufferUsable(const char *data) {
  bool rslt = strlen(data) == SHMFIELDSIZE;
  if (!rslt) syslog(LOG_ERR, _("SHM: Buffer size error"));
  return rslt;
}

void writeSize(unsigned int size) {
  memcpy(shmref, (char*)&size, sizeof(unsigned int));
}

bool shmInit(const unsigned int nbRecords) {
  bool rslt = false;
  unsigned int size = nbRecords * SHMFIELDSIZE + sizeof(unsigned int);
  mode_t accessmode = S_IRUSR | S_IWUSR;
  mode_t orig_umask = umask(0);
  int fd = shm_open(SHMNAME, O_RDWR | O_CREAT, accessmode);
  if (fd >= 0) {
    size_t memsize = size;
    if (ftruncate(fd, memsize) == 0) {
      //off_t offset = SHMPSIZE & ~(sysconf(_SC_PAGE_SIZE) - 1);
      off_t offset = 0;
      shmref = mmap(NULL, memsize, PROT_WRITE, MAP_SHARED, fd, offset);
      rslt = shmref != MAP_FAILED;
      if (!rslt) syslog(LOG_ERR, _("SHM region map failed, error: %d"), errno);
    } else syslog(LOG_ERR, _("SHM truncate error: %d"), errno);
    close(fd);
  } else syslog(LOG_ERR, _("Cannot make SHM region, error: %d"), errno);
  umask(orig_umask);
  return rslt;
}
    
unsigned int getShmSize() {
  unsigned int nbRecords;
  memcpy((char*)&nbRecords, shmref, sizeof(unsigned int));
  return nbRecords;
}

bool shmPop(char *data) {
  if (!isBufferUsable(data)) return false;
  bool rslt = false;
  unsigned int size = getShmSize();
  if (size > 0) {
    unsigned int read_offset = (size - 1) * SHMFIELDSIZE + sizeof(unsigned int);
    strncpy(data, shmref + read_offset, SHMFIELDSIZE);
    size--;
    writeSize(size);
    rslt = true;
  } else syslog(LOG_WARNING, _("shmPop: SHM region empty"));
  return rslt;
}

bool shmPush(const char *data) {
  if (!isBufferUsable(data)) return false;
  bool rslt = false;
  unsigned int size = getShmSize();
  if (size < SHMDEFAULTNBRECORDS) {
    unsigned int write_offset = size * SHMFIELDSIZE + sizeof(unsigned int);
    strncpy(shmref +write_offset, data, SHMFIELDSIZE);
    size++;
    writeSize(size);
    rslt = true;
  } else syslog(LOG_ERR, _("shmPop: SHM region full"));
  return rslt;
}
