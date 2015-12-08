#ifndef LTOA_H
#define LTOA_H

#include <stdlib.h>
#include <string.h>

#define BUFSIZE (sizeof(long) * 8 + 1)

char *ltoa(long N, char *str, int base);

#endif
