#include "global.h"

bool isSymmetry;
bool marcheAvant;
Uart<2> serial_rb;
double x_odo, y_odo; // abscisse et ordonn�e exprim�es en mm
double orientation_odo; // exprim� en radians
double cos_orientation_odo, sin_orientation_odo;
bool asserEnable;
SemaphoreHandle_t odo_mutex = xSemaphoreCreateMutex();
double courbure_odo;
