#ifndef DEF_GLOBAL
#define DEF_GLOBAL

#include "Uart.hpp"
#include "FreeRTOS.h"
#include "semphr.h"
#include <vector>
#include "Hook.h"

#define SERIE_TIMEOUT 10
#define TAILLE_BUFFER_ECRITURE_SERIE	50

#define ATTENTE_MUTEX_MS 10
#define FREQUENCE_ODO_ASSER 200 // en appel / s
#define MEMOIRE_MESURE 25

#define TICKS_PAR_TOUR_CODEUSE 4000
#define RAYON_CODEUSE_EN_MM 25
#define MM_PAR_TICK ((2 * M_PI * RAYON_CODEUSE_EN_MM) / TICKS_PAR_TOUR_CODEUSE) // 0.039, 1/25 environ
#define TICK_CODEUR_DROIT TIM5->CNT
#define TICK_CODEUR_GAUCHE TIM2->CNT
#define LONGUEUR_CODEUSE_A_CODEUSE_EN_MM 360

#define TICKS_PAR_TOUR_ROBOT ((2 *M_PI * LONGUEUR_CODEUSE_A_CODEUSE_EN_MM) / MM_PAR_TICK)
#define FRONTIERE_MODULO (TICKS_PAR_TOUR_ROBOT + (4294967296 - TICKS_PAR_TOUR_ROBOT) / 2)
#define TICK_TO_RAD(x) ((x / TICKS_PAR_TOUR_ROBOT) * 2 * M_PI)
#define RAD_TO_TICK(x) ((x * TICKS_PAR_TOUR_ROBOT) / (2 * M_PI))
#define TICK_TO_MM(x) (x * MM_PAR_TICK / 2)

#define DELAI_ERREUR_MECA_MS	100 // durant combien de ms faut-il qu'il y ait un problème mécanique pour annuler un mouvement ?
#define DELAI_ERREUR_MECA_APPEL (DELAI_ERREUR_MECA_MS * FREQUENCE_ODO_ASSER)

enum MODE_ASSER {ASSER_OFF, // pas d'asser
	STOP, // le robot doit s'arrêter le plus vite possible
	ROTATION, // le robot doit tourner
	VA_AU_POINT, // le robot doit arriver à un point. Il n'y a aucun asservissement à la trajectoire : mieux vaut utiliser ce mode pour des mouvements rectilignes
	COURBE}; // le robot est asservi à une trajectoire courbe en clothoïde
enum DirectionStrategy {FORCE_BACK_MOTION, FORCE_FORWARD_MOTION, FASTEST};

extern std::vector<Hook*> listeHooks;
extern bool isSymmetry;
extern bool marcheAvant;
extern Uart<2> serial_rb;
extern DirectionStrategy strategy;
extern volatile bool ping;

/**
 * x_odo, y_odo et orientation_odo sont exprimés dans le repère symétrisé, qui n'est pas forcément le repère réel
 */
extern double x_odo, y_odo; // abscisse et ordonnée exprimées en mm
extern double orientation_odo; // exprimé en radians
extern double cos_orientation_odo, sin_orientation_odo;
extern double courbure_odo; // en mm^-1

// MUTEX
extern SemaphoreHandle_t odo_mutex;
extern SemaphoreHandle_t consigneAsser_mutex;
extern Uart<6> serial_ax;
extern AX<Uart<6>>* ax12;

extern TIM_Encoder_InitTypeDef encoder, encoder2;
extern TIM_HandleTypeDef timer, timer2, timer3;
extern volatile bool startOdo;
extern volatile bool matchDemarre;
extern bool debugMode;
extern bool needArrive;
extern MODE_ASSER modeAsserActuel;

#endif
