#ifndef DEF_GLOBAL
#define DEF_GLOBAL

#include "Uart.hpp"

extern Uart<2> serial_rb;
extern double x_odo, y_odo; // abscisse et ordonn�e exprim�es en mm
extern double orientation_odo; // exprim� en radians

#endif
