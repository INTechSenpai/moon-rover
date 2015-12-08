
#include "Motor.h"
#include "pid.hpp"
#include "average.hpp"

#define AVERAGE_SPEED_SIZE 25

enum MOVING_DIRECTION {FORWARD, BACKWARD, NONE};

	Motor leftMotor(Side::LEFT);
	Motor rightMotor(Side::RIGHT);

/*
 * 		Définition des variables d'état du système (position, vitesse, consigne, ...)
 *
 * 		Les unités sont :
 * 			Pour les distances		: ticks
 * 			Pour les vitesses		: ticks/seconde
 * 			Pour les accélérations	: ticks/seconde^2
 * 			Ces unités seront vraies pour une fréquence d'asservissement de 2kHz,
 * 			si l'on souhaite changer la fréquence d'asservissement il faut adapter le calcul de la vitesse
 * 			autrement les unitées ci-dessus ne seront plus valables.
 */


	//	Asservissement en vitesse du moteur droit
	volatile int32_t rightSpeedSetpoint;	// ticks/seconde
	volatile int32_t currentRightSpeed;		// ticks/seconde
	volatile int32_t rightPWM;
	PID rightSpeedPID(&currentRightSpeed, &rightPWM, &rightSpeedSetpoint);

	//	Asservissement en vitesse du moteur gauche
	volatile int32_t leftSpeedSetpoint;		// ticks/seconde
	volatile int32_t currentLeftSpeed;		// ticks/seconde
	volatile int32_t leftPWM;
	PID leftSpeedPID(&currentLeftSpeed, &leftPWM, &leftSpeedSetpoint);

	//	Asservissement en position : translation
	volatile int32_t translationSetpoint;	// ticks
	volatile int32_t currentDistance;		// ticks
	volatile int32_t translationSpeed;		// ticks/seconde
	PID translationPID(&currentDistance, &translationSpeed, &translationSetpoint);
	//	Asservissement en position : rotation

	volatile int32_t rotationSetpoint;		// angle absolu visé (en ticks)
	volatile int32_t currentAngle;			// ticks
	volatile int32_t rotationSpeed;			// ticks/seconde
	PID rotationPID(&currentAngle, &rotationSpeed, &rotationSetpoint);

	//	Limitation de vitesses
	volatile int32_t maxSpeed; 				// definit la vitesse maximal des moteurs du robot
	volatile int32_t maxSpeedTranslation;	// definit la consigne max de vitesse de translation envoiée au PID (trapèze)
	volatile int32_t maxSpeedRotation;		// definit la consigne max de vitesse de rotation envoiée au PID (trapèze)

	//	Limitation d'accélération
	volatile int32_t maxAcceleration;

	//	Pour faire de jolies courbes de réponse du système, la vitesse moyenne c'est mieux !
	Average<int32_t, AVERAGE_SPEED_SIZE> averageLeftSpeed;
	Average<int32_t, AVERAGE_SPEED_SIZE> averageRightSpeed;

	// Définit la vitesse à utiliser pour les tests d'asservissement (testSpeed et testSpeedReverse)
	volatile int32_t speedTest;


/*
 * 	Variables de positionnement haut niveau (exprimmées en unités pratiques ^^)
 *
 * 	Toutes ces variables sont initialisées à 0. Elles doivent donc être règlées ensuite
 * 	par le haut niveau pour correspondre à son système de coordonnées.
 * 	Le bas niveau met à jour la valeur de ces variables mais ne les utilise jamais pour
 * 	lui même, il se contente de les transmettre au haut niveau.
 */
	volatile float x;				// Positionnement 'x' (mm)
	volatile float y;				// Positionnement 'y' (mm)
	volatile float originalAngle;	// Angle d'origine	  (radians)
	// 'originalAngle' représente un offset ajouté à l'angle courant pour que nos angles en radians coïncident avec la représentation haut niveau des angles.


	// Variables d'état du mouvement
	volatile bool moving;
	volatile MOVING_DIRECTION direction;
	volatile bool moveAbnormal;

	// Variables d'activation des différents PID
	volatile bool translationControlled;
	volatile bool rotationControlled;
	volatile bool leftSpeedControlled;
	volatile bool rightSpeedControlled;

	// Variables de réglage de la détection de blocage physique
	unsigned int delayToStop;//En ms
	//Nombre de ticks de tolérance pour considérer qu'on est arrivé à destination
	int toleranceTranslation;
	int toleranceRotation;
