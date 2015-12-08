#ifndef DEF_GLOBAL
#define DEF_GLOBAL

#include "Uart.hpp"
#include "FreeRTOS.h"
#include "semphr.h"

#define ATTENTE_MUTEX_MS 10

extern Uart<2> serial_rb;
extern double x_odo, y_odo; // abscisse et ordonn�e exprim�es en mm
extern double orientation_odo; // exprim� en radians

// MUTEX
extern SemaphoreHandle_t serial_rb_mutex;

#endif
