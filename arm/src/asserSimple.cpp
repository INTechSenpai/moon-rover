
#include "Motor.h"
#include "pid.hpp"
#include "average.hpp"
#include "global.h"
#include <cmath>
#include "math.h"

#define AVERAGE_SPEED_SIZE 10

enum MOVING_DIRECTION {FORWARD, BACKWARD, NONE};

	Motor leftMotor(MOTOR_LEFT);
	Motor rightMotor(MOTOR_RIGHT);

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

	int32_t const zero = 0;

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

	//	Asservissement en vitesse linéaire et en courbure
	volatile int32_t vitesseLineaireReelle;
	volatile int32_t courbureReelle;
	volatile int32_t consigneVitesseGauche;
	volatile int32_t consigneCourbure;
	PIDvitesse PIDvit(&vitesseLineaireReelle, &courbureReelle, &leftPWM, &rightPWM, &consigneVitesseLineaire, &consigneCourbure);

	//	Asservissement en position : translation
	volatile int32_t currentDistance;		// distance à parcourir, en ticks
	volatile int32_t translationSpeed;		// ticks/seconde
	PID translationPID(&currentDistance, &translationSpeed, &zero);

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

	int32_t previousLeftSpeedSetpoint = 0;
	int32_t previousRightSpeedSetpoint = 0;

	double x_consigne, y_consigne;

	/**
	 * Donne la consigne sous forme (x, y) utilisée pour la translation
	 */
	void setConsigneTranslation(double x, double y)
	{
		x_consigne = x;
		y_consigne = y;
	}

	void setConsigneRotation(uint32_t orientation)
	{
		rotationSetpoint = orientation;
	}

	void setRotationSetpoint(int32_t rotationSetpointTmp)
	{
		// gestion de la rotation (on va au plus court)
		if(rotationSetpointTmp < currentAngle - TICKS_PAR_TOUR_ROBOT / 2)
			rotationSetpoint = rotationSetpointTmp + TICKS_PAR_TOUR_ROBOT;
		else if(rotationSetpointTmp > currentAngle + TICKS_PAR_TOUR_ROBOT / 2)
			rotationSetpoint = rotationSetpointTmp - TICKS_PAR_TOUR_ROBOT;
		else
			rotationSetpoint = rotationSetpointTmp;
	}

	void controlTranslation(int16_t delta_tick_droit, int16_t delta_tick_gauche, uint32_t orientationMoyTick)
	{
		currentAngle = (int32_t) orientationMoyTick;

		currentLeftSpeed = delta_tick_gauche * FREQUENCE_ODO_ASSER; // (nb-de-tick-passés)*(freq_asserv) (ticks/sec)
		currentRightSpeed = delta_tick_droit * FREQUENCE_ODO_ASSER;

		averageLeftSpeed.add(currentLeftSpeed);
		averageRightSpeed.add(currentRightSpeed);

		currentLeftSpeed = averageLeftSpeed.value(); // On utilise pour l'asserv la valeur moyenne des dernieres current Speed
		currentRightSpeed = averageRightSpeed.value();

		// calcul de la consigne en translation et en rotation
		currentDistance = (int32_t)(hypot(x_odo - x_consigne, y_odo - y_consigne) * (1 / MM_PAR_TICK));
		translationPID.compute();	// Actualise la valeur de 'translationSpeed'

        if(!marcheAvant)
            translationSpeed = -translationSpeed;

		// pas de correction de l'orientation si on est très proche (3cm ou moins)
		if(currentDistance > 30 / MM_PAR_TICK)
		{
			setRotationSetpoint(RAD_TO_TICK(atan2(y_consigne - y_odo, x_consigne - x_odo)));
            if(!marcheAvant)
            {
                // on inverse la consigne (puisqu'on va en marche arrière)
                rotationSetpoint += TICKS_PAR_TOUR_ROBOT / 2;
                if(rotationSetpoint > TICKS_PAR_TOUR_ROBOT)
                    rotationSetpoint -= TICKS_PAR_TOUR_ROBOT;
            }
			rotationPID.compute();		// Actualise la valeur de 'rotationSpeed'
			// gestion de la symétrie pour les déplacements
			if(isSymmetry)
				rotationSpeed = -rotationSpeed;
		}
		else
			rotationSpeed = 0;

		// Limitation de la consigne de vitesse en translation
		if(translationSpeed > maxSpeedTranslation)
			translationSpeed = maxSpeedTranslation;
		else if(translationSpeed < -maxSpeedTranslation)
			translationSpeed = -maxSpeedTranslation;

		// Limitation de la consigne de vitesse en rotation
		if(rotationSpeed > maxSpeedRotation)
			rotationSpeed = maxSpeedRotation;
		else if(rotationSpeed < -maxSpeedRotation)
			rotationSpeed = -maxSpeedRotation;

		leftSpeedSetpoint = translationSpeed - rotationSpeed;
		rightSpeedSetpoint = translationSpeed + rotationSpeed;

		// Limitation de la vitesses
		if(leftSpeedSetpoint > maxSpeed)
			leftSpeedSetpoint = maxSpeed;
		else if(leftSpeedSetpoint < -maxSpeed)
			leftSpeedSetpoint = -maxSpeed;
		if(rightSpeedSetpoint > maxSpeed)
			rightSpeedSetpoint = maxSpeed;
		else if(rightSpeedSetpoint < -maxSpeed)
			rightSpeedSetpoint = -maxSpeed;

		// Limitation de l'accélération du moteur gauche
		if(leftSpeedSetpoint - previousLeftSpeedSetpoint > maxAcceleration)
			leftSpeedSetpoint = previousLeftSpeedSetpoint + maxAcceleration;
		else if(leftSpeedSetpoint - previousLeftSpeedSetpoint < -maxAcceleration)
			leftSpeedSetpoint = previousLeftSpeedSetpoint - maxAcceleration;

		// Limitation de l'accélération du moteur droit
		if(rightSpeedSetpoint - previousRightSpeedSetpoint > maxAcceleration)
			rightSpeedSetpoint = previousRightSpeedSetpoint + maxAcceleration;
		else if(rightSpeedSetpoint - previousRightSpeedSetpoint < -maxAcceleration)
			rightSpeedSetpoint = previousRightSpeedSetpoint - maxAcceleration;

		previousLeftSpeedSetpoint = leftSpeedSetpoint;
		previousRightSpeedSetpoint = rightSpeedSetpoint;

		//serial.printfln("%d",(leftSpeedSetpoint - currentLeftSpeed));

		if(leftSpeedControlled)
			leftSpeedPID.compute();		// Actualise la valeur de 'leftPWM'
		if(rightSpeedControlled)
			rightSpeedPID.compute();	// Actualise la valeur de 'rightPWM'

		leftMotor.run(leftPWM);
		rightMotor.run(rightPWM);
	}

    void controlRotation(uint32_t orientationMoyTick)
    {
        currentAngle = (int32_t) orientationMoyTick;
        rotationPID.compute();      // Actualise la valeur de 'rotationSpeed'
        translationPID.compute();      // Actualise la valeur de 'rotationSpeed'
        // gestion de la symétrie pour les déplacements

        if(isSymmetry)
            rotationSpeed = -rotationSpeed;

        leftSpeedSetpoint = translationSpeed - rotationSpeed;
        rightSpeedSetpoint = translationSpeed + rotationSpeed;
        
        leftSpeedPID.compute();     // Actualise la valeur de 'leftPWM'
        rightSpeedPID.compute();    // Actualise la valeur de 'rightPWM'

        leftMotor.run(leftPWM);
        rightMotor.run(rightPWM);
    }

    void controlTrajectoire()
    {
        // TODO
	uint32_t xR, yR;
	projection(&xR, &yR);
	// calcul orientation en R, courbure en R, distance R-robot
	// calcul courbureSamson
	// calcul courbureConsigne
	// calcul vitesseConsigne
	// calcul vitesseLineaire avec pidTranslation
	// asser vitesse + courbure
    }

    // freine le plus rapidement possible. Note : on ne garantit rien sur l'orientation, si une roue freine plus vite que l'autre le robot va tourner.
    void controlStop()
    {
        leftSpeedSetpoint = 0;
        rightSpeedSetpoint = 0;

        leftSpeedPID.compute();     // Actualise la valeur de 'leftPWM'
        rightSpeedPID.compute();    // Actualise la valeur de 'rightPWM'
            
        leftMotor.run(leftPWM);
        rightMotor.run(rightPWM);
    }

    void setVitesseMaxTranslation(int16_t speedTr)
    {
        // TODO
    }
