#include <stdbool.h>
#include <jni.h>

#define SCKPATH "/var/run/pqmessenger/sck"
#define MAXREAD 256

extern JavaVM *javaVM;
extern bool cacheData;

bool doListen();
bool stopListen();
bool doSend(char* data);
void sendPassword(char *pwd, char *user);
