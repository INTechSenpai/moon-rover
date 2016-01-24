#include "global.h"

bool isSymmetry;
bool marcheAvant;
Uart<2> serial_rb;
double x_odo, y_odo; // abscisse et ordonnée exprimées en mm
double orientation_odo; // exprimé en radians
uint32_t orientationTick;
SemaphoreHandle_t serial_rb_mutex = xSemaphoreCreateMutex();
double cos_orientation_odo, sin_orientation_odo;
bool asserEnable;
