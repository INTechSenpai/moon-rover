#ifndef UTILS_H
#define UTILS_H

#if defined(ARDUINO) && ARDUINO >= 100
#include "arduino.h"
#else
#include "WProgram.h"
#endif

#include "math.h"

#define MIN(x,y) (((x)<(y))?(x):(y))
#define MAX(x,y) (((x)>(y))?(x):(y))
#define ABS(x) (((x) > 0) ? (x) : -(x))
#define CONSTRAIN(x,y,z) ( ((x)<(y))?(y):( ((x)>(z))?(z):(x) ) )

#ifdef __cplusplus
extern "C" {
#endif

int modulo(int nombre, int modulo);
float fmodulo(float nombre, float modulo);
float square(float x);

#ifdef __cplusplus
}
#endif

#endif
