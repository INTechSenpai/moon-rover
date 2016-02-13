#ifndef DEF_GLOBAL
#define DEF_GLOBAL

#include "Uart.hpp"
#include "FreeRTOS.h"
#include "semphr.h"

#define SERIE_TIMEOUT 10
#define TAILLE_BUFFER_ECRITURE_SERIE	50

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

#define MEMOIRE_VITESSE 25
enum ModeAsser {PAS_ASSER, ASSER_TRANSLATION, ASSER_ROTATION, ASSER_TRAJECTOIRE, ASSER_STOP_ROBOT};
enum DirectionStrategy {FORCE_BACK_MOTION, FORCE_FORWARD_MOTION, FASTEST};

extern bool isSymmetry;
extern bool marcheAvant;
extern Uart<2> serial_rb;
extern DirectionStrategy strategy;

/**
 * x_odo, y_odo et orientation_odo sont exprimés dans le repère symétrisé, qui n'est pas forcément le repère réel
 */
extern double x_odo, y_odo; // abscisse et ordonnée exprimées en mm
extern double orientation_odo; // exprimé en radians
extern double cos_orientation_odo, sin_orientation_odo;
extern double vd_odo, vg_odo; // vitesses exprimées en tick / ms
extern double courbure_odo; // en mm^-1

// MUTEX
extern SemaphoreHandle_t odo_mutex;

#endif
