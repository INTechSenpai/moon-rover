#ifndef DEF_GLOBAL
#define DEF_GLOBAL

#include "Uart.hpp"
#include "FreeRTOS.h"
#include "semphr.h"
#include <vector>
#include "Hook.h"

#define SERIE_TIMEOUT 10
#define TAILLE_BUFFER_ECRITURE_SERIE	50

#define ATTENTE_MUTEX_MS 1
#define FREQUENCE_ODO_ASSER 200 // en appel / s
#define MEMOIRE_MESURE_FLOAT 25.
#define MEMOIRE_MESURE_INT 25
#define FREQUENCE_PWM 10000 // 10 kHz
#define PWM_MAX 1024

#define TICKS_PAR_TOUR_CODEUSE 4000
#define RAYON_CODEUSE_EN_MM (32.3/2)
#define MM_PAR_TICK ((2 * M_PI * RAYON_CODEUSE_EN_MM) / TICKS_PAR_TOUR_CODEUSE) // 0.025, 1/40 environ
#define TICK_CODEUR_DROIT TIM3->CNT
#define TICK_CODEUR_GAUCHE TIM2->CNT
#define LONGUEUR_CODEUSE_A_CODEUSE_EN_MM 173

#define TICKS_PAR_TOUR_ROBOT ((2 * M_PI * LONGUEUR_CODEUSE_A_CODEUSE_EN_MM) / MM_PAR_TICK) // 43500
#define FRONTIERE_MODULO (TICKS_PAR_TOUR_ROBOT + (4294967296 - TICKS_PAR_TOUR_ROBOT) / 2)
#define RAD_PAR_TICK ((2 * M_PI) / TICKS_PAR_TOUR_ROBOT) // 1/6920
#define TICK_TO_RAD(x) ((x / TICKS_PAR_TOUR_ROBOT) * 2 * M_PI)
#define RAD_TO_TICK(x) ((x * TICKS_PAR_TOUR_ROBOT) / (2 * M_PI))
#define TICK_TO_MM(x) (x * MM_PAR_TICK / 2)

#define DELAI_ERREUR_MECA_MS	3000 // durant combien de ms faut-il qu'il y ait un problème mécanique pour annuler un mouvement ?
#define DELAI_ERREUR_MECA_APPEL ((DELAI_ERREUR_MECA_MS * FREQUENCE_ODO_ASSER) / 1000) // 1000 pour passer des ms aux s

enum MODE_ASSER {ASSER_OFF, // pas d'asser
	STOP, // le robot doit s'arrêter le plus vite possible
	ROTATION, // le robot doit tourner
	VA_AU_POINT, // le robot doit arriver à un point. Il n'y a aucun asservissement à la trajectoire : mieux vaut utiliser ce mode pour des mouvements rectilignes
	ASSER_VITESSE, // le robot doit avancer à vitesse constante. utilisé pour debugger l'asser en vitesse
	COURBE}; // le robot est asservi à une trajectoire courbe en clothoïde
enum DirectionStrategy {FORCE_BACK_MOTION, FORCE_FORWARD_MOTION, FASTEST};

extern std::vector<Hook*> listeHooks;
extern volatile bool isSymmetry;
extern volatile bool marcheAvant;
extern Uart<2> serial_rb;
extern DirectionStrategy strategy;
extern volatile bool ping;

/**
 * x_odo, y_odo et orientation_odo sont exprimés dans le repère symétrisé, qui n'est pas forcément le repère réel
 */
extern volatile double x_odo, y_odo; // abscisse et ordonnée exprimées en mm
extern volatile double orientation_odo; // exprimé en radians
extern volatile double cos_orientation_odo, sin_orientation_odo;
extern volatile double courbure_odo; // en mm^-1

extern volatile int16_t asserVitesseGauche;
extern volatile int16_t asserVitesseDroite;

// MUTEX
extern SemaphoreHandle_t odo_mutex;
extern SemaphoreHandle_t consigneAsser_mutex;
extern Uart<3> serial_ax;
extern AX<Uart<3>>* ax12;

extern volatile bool startOdo;
extern volatile bool matchDemarre;
extern volatile bool debugMode;
extern volatile bool needArrive;
extern MODE_ASSER modeAsserActuel;

#endif
