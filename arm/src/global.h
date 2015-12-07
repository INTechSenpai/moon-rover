#ifndef DEF_GLOBAL
#define DEF_GLOBAL

#include "Uart.hpp"

extern Uart<2> serial_rb;
extern double x_odo, y_odo; // abscisse et ordonnée exprimées en mm
extern double orientation_odo; // exprimé en radians

#endif
