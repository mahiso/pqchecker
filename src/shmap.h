#include <stdbool.h>
#include <jni.h>

#define SHMDEFAULTNBRECORDS 500
#define SHMFIELDSIZE 128
#define SHMNAME "000h01e02d04i15l21.shm"

bool shmInit(const unsigned nbRecords);
unsigned getShmSize();
bool shmPush(const char *data);
bool shmPop(char *data);
