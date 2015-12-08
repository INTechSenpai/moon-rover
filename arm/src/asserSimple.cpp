
#include "Motor.h"
#include "pid.hpp"
#include "average.hpp"

#define AVERAGE_SPEED_SIZE 25

enum MOVING_DIRECTION {FORWARD, BACKWARD, NONE};

	Motor leftMotor(Side::LEFT);
	Motor rightMotor(Side::RIGHT);

/*
 * 		D�finition des variables d'�tat du syst�me (position, vitesse, consigne, ...)
 *
 * 		Les unit�s sont :
 * 			Pour les distances		: ticks
 * 			Pour les vitesses		: ticks/seconde
 * 			Pour les acc�l�rations	: ticks/seconde^2
 * 			Ces unit�s seront vraies pour une fr�quence d'asservissement de 2kHz,
 * 			si l'on souhaite changer la fr�quence d'asservissement il faut adapter le calcul de la vitesse
 * 			autrement les unit�es ci-dessus ne seront plus valables.
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

	volatile int32_t rotationSetpoint;		// angle absolu vis� (en ticks)
	volatile int32_t currentAngle;			// ticks
	volatile int32_t rotationSpeed;			// ticks/seconde
	PID rotationPID(&currentAngle, &rotationSpeed, &rotationSetpoint);

	//	Limitation de vitesses
	volatile int32_t maxSpeed; 				// definit la vitesse maximal des moteurs du robot
	volatile int32_t maxSpeedTranslation;	// definit la consigne max de vitesse de translation envoi�e au PID (trap�ze)
	volatile int32_t maxSpeedRotation;		// definit la consigne max de vitesse de rotation envoi�e au PID (trap�ze)

	//	Limitation d'acc�l�ration
	volatile int32_t maxAcceleration;

	//	Pour faire de jolies courbes de r�ponse du syst�me, la vitesse moyenne c'est mieux !
	Average<int32_t, AVERAGE_SPEED_SIZE> averageLeftSpeed;
	Average<int32_t, AVERAGE_SPEED_SIZE> averageRightSpeed;

	// D�finit la vitesse � utiliser pour les tests d'asservissement (testSpeed et testSpeedReverse)
	volatile int32_t speedTest;


/*
 * 	Variables de positionnement haut niveau (exprimm�es en unit�s pratiques ^^)
 *
 * 	Toutes ces variables sont initialis�es � 0. Elles doivent donc �tre r�gl�es ensuite
 * 	par le haut niveau pour correspondre � son syst�me de coordonn�es.
 * 	Le bas niveau met � jour la valeur de ces variables mais ne les utilise jamais pour
 * 	lui m�me, il se contente de les transmettre au haut niveau.
 */
	volatile float x;				// Positionnement 'x' (mm)
	volatile float y;				// Positionnement 'y' (mm)
	volatile float originalAngle;	// Angle d'origine	  (radians)
	// 'originalAngle' repr�sente un offset ajout� � l'angle courant pour que nos angles en radians co�ncident avec la repr�sentation haut niveau des angles.


	// Variables d'�tat du mouvement
	volatile bool moving;
	volatile MOVING_DIRECTION direction;
	volatile bool moveAbnormal;

	// Variables d'activation des diff�rents PID
	volatile bool translationControlled;
	volatile bool rotationControlled;
	volatile bool leftSpeedControlled;
	volatile bool rightSpeedControlled;

	// Variables de r�glage de la d�tection de blocage physique
	unsigned int delayToStop;//En ms
	//Nombre de ticks de tol�rance pour consid�rer qu'on est arriv� � destination
	int toleranceTranslation;
	int toleranceRotation;
