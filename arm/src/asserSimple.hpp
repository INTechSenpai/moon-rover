#ifndef ASSER
#define ASSER

#include "Motor.h"
#include "pid.hpp"
#include "PIDvitesse.hpp"
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

	// Consigne en position qui permet de calculer les erreurs en distance et en rotation
	volatile int32_t consigneX;
	volatile int32_t consigneY;

	//	Asservissement en vitesse du moteur droit
	int32_t rightSpeedSetpoint;	// ticks/seconde
	int32_t currentRightSpeed;		// ticks/seconde
	volatile int32_t errorRightSpeed;
	volatile int32_t rightPWM;
	PID rightSpeedPID(&errorRightSpeed, &rightPWM);

	//	Asservissement en vitesse du moteur gauche
	int32_t leftSpeedSetpoint;		// ticks/seconde
	int32_t currentLeftSpeed;		// ticks/seconde
	volatile int32_t errorLeftSpeed;
	volatile int32_t leftPWM;
	PID leftSpeedPID(&errorLeftSpeed, &leftPWM);

	//	Asservissement en vitesse linéaire et en courbure
	volatile int32_t vitesseLineaireReelle;
	volatile int32_t courbureReelle;
	volatile int32_t consigneVitesseLineaire;
	volatile int32_t consigneCourbure;
	PIDvitesse PIDvit(&vitesseLineaireReelle, &courbureReelle, &leftPWM, &rightPWM, &consigneVitesseLineaire, &consigneCourbure);

	//	Asservissement en position : translation
	int32_t currentDistance;		// distance à parcourir, en ticks
	volatile int32_t translationSpeed;		// ticks/seconde
	volatile int32_t errorTranslation;		// ticks/seconde
	PID translationPID(&errorTranslation, &translationSpeed);

	//	Asservissement en position : rotation

	uint32_t rotationSetpoint;		// angle absolu visé (en ticks)
	uint32_t orientationTick_odo = 0;
	volatile int32_t errorAngle;
	volatile int32_t rotationSpeed;			// ticks/seconde
	PID rotationPID(&errorAngle, &rotationSpeed);

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

	void setRotationSetpoint(int32_t rotationSetpointTmp)
	{
		// gestion de la rotation (on va au plus court)
		if(rotationSetpointTmp < orientationTick_odo - TICKS_PAR_TOUR_ROBOT / 2)
			rotationSetpoint = rotationSetpointTmp + TICKS_PAR_TOUR_ROBOT;
		else if(rotationSetpointTmp > orientationTick_odo + TICKS_PAR_TOUR_ROBOT / 2)
			rotationSetpoint = rotationSetpointTmp - TICKS_PAR_TOUR_ROBOT;
		else
			rotationSetpoint = rotationSetpointTmp;
	}

	void controlTranslation(int16_t delta_tick_droit, int16_t delta_tick_gauche, uint32_t orientationMoyTick)
	{
		/*
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
		*/
	}

	/**
	 * Asservissement, à la fois sur la distance et l'angle
	 * On suppose que les erreurs sont à jours
	 */
	void controlDistanceAngle()
	{

	}

    void controlRotation()
    {
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
//	projection(&xR, &yR);
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

    // Sommes-nous arrivés ?
    bool checkArrivee()
    {
        return ABS(leftPWM) < 5 && ABS(rightPWM) < 5 && ABS(rotationSpeed) < 10 && ABS(translationSpeed) < 10;
    }

    /**
     * Utilise consigneX, consigneY, x_odo et y_odo pour calculer rotationSetpoint
     */
    void updateRotationSetpoint()
    {
    	double tmp = RAD_TO_TICK(atan2(consigneY - y_odo, consigneX - x_odo));
    	if(tmp >= 0)
    		rotationSetpoint = (int32_t) tmp;
    	else
    		rotationSetpoint = (int32_t) (TICKS_PAR_TOUR_ROBOT - tmp);
    }

    /**
     * Utilise rotationSetpoint et orientationTick_odo pour calculer errorAngle
     */
    void updateErrorAngle()
    {
    	uint32_t e;
    	if(rotationSetpoint > orientationTick_odo)
    	{
    		e = rotationSetpoint - orientationTick_odo;
    		if(e < TICKS_PAR_TOUR_ROBOT/2)
    			errorAngle = e;
    		else
    			errorAngle = -(TICKS_PAR_TOUR_ROBOT - e);
    	}
    	else
    	{
    		e = orientationTick_odo - rotationSetpoint;
    		if(e < TICKS_PAR_TOUR_ROBOT/2)
    			errorAngle = -e;
    		else
    			errorAngle = TICKS_PAR_TOUR_ROBOT - e;
    	}
    }

    void updateErrorTranslation()
    {
    	int32_t e = (int32_t) hypot(x_odo - consigneX, y_odo - consigneY) * MM_PAR_TICK;
    	// faut-il aller en marche arrière ?
    	if(cos_orientation_odo * (consigneX - x_odo) + sin_orientation_odo * (consigneY - y_odo) < 0)
    		e = -e;
    	errorTranslation = e;
    }


#endif
