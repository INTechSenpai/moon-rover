#ifndef DEF_GLOBAL
#define DEF_GLOBAL

#include "Uart.hpp"
#include "FreeRTOS.h"
#include "semphr.h"

#define ATTENTE_MUTEX_MS 10
#define FREQUENCE_ODO_ASSER 200

#define TICKS_PAR_TOUR_CODEUSE 4000
#define RAYON_CODEUSE_EN_MM 25
#define MM_PAR_TICK ((2 * M_PI * RAYON_CODEUSE_EN_MM) / TICKS_PAR_TOUR_CODEUSE)
#define TICK_CODEUR_DROIT TIM5->CNT
#define TICK_CODEUR_GAUCHE TIM2->CNT
#define LONGUEUR_CODEUSE_A_CODEUSE_EN_MM 360

#define TICKS_PAR_TOUR_ROBOT ((2 *M_PI * LONGUEUR_CODEUSE_A_CODEUSE_EN_MM) / MM_PAR_TICK)
#define FRONTIERE_MODULO (TICKS_PAR_TOUR_ROBOT + (4294967296 - TICKS_PAR_TOUR_ROBOT) / 2)
#define TICK_TO_RAD(x) ((x / TICKS_PAR_TOUR_ROBOT) * 2 * M_PI)
#define RAD_TO_TICK(x) ((x * TICKS_PAR_TOUR_ROBOT) / (2 * M_PI))
#define TICK_TO_MM(x) (x * MM_PAR_TICK / 2)

extern bool isSymmetry;
extern Uart<2> serial_rb;

/**
 * x_odo, y_odo et orientation_odo sont exprim�s dans le rep�re sym�tris�, qui n'est pas forc�ment le rep�re r�el
 */
extern double x_odo, y_odo; // abscisse et ordonn�e exprim�es en mm
extern double orientation_odo; // exprim� en radians
extern bool asserEnable;

// MUTEX
extern SemaphoreHandle_t serial_rb_mutex;

#endif
