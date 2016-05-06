#ifndef DEF_GLOBAL
#define DEF_GLOBAL

#include "Uart.hpp"
#include "FreeRTOS.h"
#include "semphr.h"
#include <vector>
#include "Hook.h"

#define SERIE_TIMEOUT 10
#define TAILLE_BUFFER_ECRITURE_SERIE	50

#define AX12_AVANT_GAUCHE 4
#define AX12_AVANT_DROIT 3
#define AX12_ARRIERE_GAUCHE 1
#define AX12_ARRIERE_DROIT 2
#define AX12_PORTE_CANNE 5
#define AX12_LACHE_POISSON 6

#define AX12_GROS_1 0
#define AX12_GROS_2 7
#define AX12_GROS_3 8

#define NB_AX12 9

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
#define LONGUEUR_CODEUSE_A_CODEUSE_EN_MM 172
#define DEMI_DISTANCE_ROUES_PROPU_EN_MM 48

#define TICKS_PAR_TOUR_ROBOT ((2 * M_PI * LONGUEUR_CODEUSE_A_CODEUSE_EN_MM) / MM_PAR_TICK) // 43500
#define FRONTIERE_MODULO (TICKS_PAR_TOUR_ROBOT + (4294967296 - TICKS_PAR_TOUR_ROBOT) / 2)
#define RAD_PAR_TICK ((2 * M_PI) / TICKS_PAR_TOUR_ROBOT) // 1/6920
#define TICK_TO_RAD(x) ((x / TICKS_PAR_TOUR_ROBOT) * 2 * M_PI)
#define RAD_TO_TICK(x) ((x * TICKS_PAR_TOUR_ROBOT) / (2 * M_PI))
#define TICK_TO_MM(x) (x * MM_PAR_TICK / 2)

#define DELAI_ERREUR_MECA_MS	1000 // durant combien de ms faut-il qu'il y ait un probl�me m�canique pour annuler un mouvement ?
#define DELAI_ERREUR_MECA_APPEL ((DELAI_ERREUR_MECA_MS * FREQUENCE_ODO_ASSER) / 1000) // 1000 pour passer des ms aux s

enum MODE_ASSER {ASSER_OFF, // pas d'asser
	STOP, // le robot doit s'arr�ter le plus vite possible
	ROTATION, // le robot doit tourner
	VA_AU_POINT, // le robot doit arriver � un point. Il n'y a aucun asservissement � la trajectoire : mieux vaut utiliser ce mode pour des mouvements rectilignes
	SUR_PLACE, // asservissement sur place
	ASSER_VITESSE, // le robot doit avancer � vitesse constante. utilis� pour debugger l'asser en vitesse
	COURBE}; // le robot est asservi � une trajectoire courbe en clotho�de
enum DirectionStrategy {FORCE_BACK_MOTION, FORCE_FORWARD_MOTION, FASTEST};

extern std::vector<Hook*> listeHooks;
extern volatile bool isSymmetry;
extern volatile bool marcheAvant;
extern Uart<2> serial_rb;
extern DirectionStrategy strategy;
extern volatile bool ping;

/**
 * x_odo, y_odo et orientation_odo sont exprim�s dans le rep�re sym�tris�, qui n'est pas forc�ment le rep�re r�el
 */
extern volatile double x_odo, y_odo; // abscisse et ordonn�e exprim�es en mm
extern volatile double orientation_odo; // exprim� en radians
extern volatile double cos_orientation_odo, sin_orientation_odo;
extern volatile double courbure_odo; // en mm^-1

extern volatile int16_t asserVitesseGauche;
extern volatile int16_t asserVitesseDroite;

// MUTEX
extern SemaphoreHandle_t odo_mutex;
extern SemaphoreHandle_t consigneAsser_mutex;
extern Uart<3> serial_ax;
extern AX<Uart<3>>* ax12[NB_AX12];

extern volatile bool startOdo;
extern volatile bool pauseAsser;
extern volatile bool matchDemarre;
extern volatile bool debugMode;
extern volatile bool needArrive;
extern MODE_ASSER modeAsserActuel;

#endif
