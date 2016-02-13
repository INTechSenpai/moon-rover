#include "global.h"

bool isSymmetry;
bool marcheAvant;
Uart<2> serial_rb;
double x_odo, y_odo; // abscisse et ordonnée exprimées en mm
double orientation_odo; // exprimé en radians
double cos_orientation_odo, sin_orientation_odo;
bool asserEnable;
SemaphoreHandle_t odo_mutex = xSemaphoreCreateMutex();
double courbure_odo;
