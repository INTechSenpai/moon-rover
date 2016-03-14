#ifndef ASSER
#define ASSER

#include "Motor.h"
#include "pid.hpp"
#include "PIDvitesse.hpp"
#include "average.hpp"
#include "global.h"
#include <cmath>
#include "math.h"

#define VITESSE_LINEAIRE_MAX (300 / MM_PAR_TICK / FREQUENCE_ODO_ASSER) // vitesse max en tick / appel asser
#define ACCELERATION_LINEAIRE_MAX (300 / MM_PAR_TICK / FREQUENCE_ODO_ASSER) // acc�l�ration max en tick / (appel asser)^2
#define VITESSE_ROTATION_MAX (300 / MM_PAR_TICK / FREQUENCE_ODO_ASSER) // vitesse max en tick / appel asser
#define ACCELERATION_ROTATION_MAX (300 / MM_PAR_TICK / FREQUENCE_ODO_ASSER) // acc�l�ration max en tick / (appel asser)^2
#define VITESSE_ROUE_MAX (300 / MM_PAR_TICK / FREQUENCE_ODO_ASSER) // vitesse max en tick / appel asser
#define ACCELERATION_ROUE_MAX (300 / MM_PAR_TICK / FREQUENCE_ODO_ASSER) // acc�l�ration max en tick / (appel asser)^2

enum MOVING_DIRECTION {FORWARD, BACKWARD, NONE};

	Motor leftMotor(MOTOR_LEFT);
	Motor rightMotor(MOTOR_RIGHT);

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

	// CONSIGNES MODIFI�ES DEPUIS D'AUTRES THREADS

	// Consigne en position qui permet de calculer les erreurs en distance et en rotation
	volatile int32_t consigneX;
	volatile int32_t consigneY;
	volatile uint32_t rotationSetpoint;		// angle absolu vis� (en ticks)

	int32_t currentRightAcceleration;
	int32_t currentLeftAcceleration;

	//	Asservissement en vitesse du moteur droit
	int32_t currentRightSpeed;		// ticks/seconde
	int32_t errorRightSpeed;
	int32_t rightPWM;
	PID rightSpeedPID(&errorRightSpeed, &rightPWM);

	//	Asservissement en vitesse du moteur gauche
	int32_t currentLeftSpeed;		// ticks/seconde
	int32_t errorLeftSpeed;
	int32_t leftPWM;
	PID leftSpeedPID(&errorLeftSpeed, &leftPWM);

	//	Asservissement en vitesse lin�aire et en courbure
	volatile int32_t vitesseLineaireReelle;
	volatile int32_t courbureReelle;
	volatile int32_t consigneVitesseLineaire;
	volatile int32_t consigneCourbure;
	PIDvitesse PIDvit(&vitesseLineaireReelle, &courbureReelle, &leftPWM, &rightPWM, &consigneVitesseLineaire, &consigneCourbure);

	//	Asservissement en position : translation
	int32_t currentDistance;		// distance � parcourir, en ticks
	int32_t translationSpeed;		// ticks/seconde
	int32_t errorTranslation;		// ticks
	PID translationPID(&errorTranslation, &translationSpeed);

	//	Asservissement en position : rotation

	uint32_t currentAngle = 0;
	int32_t errorAngle;
	int32_t rotationSpeed;			// ticks/seconde
	PID rotationPID(&errorAngle, &rotationSpeed);

	//	Pour faire de jolies courbes de r�ponse du syst�me, la vitesse moyenne c'est mieux !
//	Average<int32_t, AVERAGE_SPEED_SIZE> averageLeftSpeed;
//	Average<int32_t, AVERAGE_SPEED_SIZE> averageRightSpeed;

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

	int32_t previousLeftSpeedSetpoint = 0;
	int32_t previousRightSpeedSetpoint = 0;
	int32_t previousTranslationSpeedSetpoint = 0;
	int32_t previousRotationSpeedSetpoint = 0;



    /**
     * Utilise consigneX, consigneY, x_odo et y_odo pour calculer rotationSetpoint
     */
    void updateRotationSetpoint()
    {
    	int32_t tmp = (int32_t) RAD_TO_TICK(atan2(consigneY - y_odo, consigneX - x_odo));
    	if(tmp >= 0)
    		rotationSetpoint = tmp;
    	else
    		rotationSetpoint = (TICKS_PAR_TOUR_ROBOT + tmp);

		if(!marcheAvant)
		{
			// on inverse la consigne (puisqu'on va en marche arri�re)
			rotationSetpoint += TICKS_PAR_TOUR_ROBOT / 2;
			if(rotationSetpoint > TICKS_PAR_TOUR_ROBOT)
				rotationSetpoint -= TICKS_PAR_TOUR_ROBOT;
		}
    }

    /**
     * Utilise rotationSetpoint et orientationTick_odo pour calculer errorAngle
     */
    void updateErrorAngle()
    {
    	uint32_t e;
    	if(rotationSetpoint > currentAngle)
    	{
    		e = rotationSetpoint - currentAngle;
    		if(e < TICKS_PAR_TOUR_ROBOT/2)
    			errorAngle = e;
    		else
    			errorAngle = -(TICKS_PAR_TOUR_ROBOT - e);
    	}
    	else
    	{
    		e = currentAngle - rotationSetpoint;
    		if(e < TICKS_PAR_TOUR_ROBOT/2)
    			errorAngle = -e;
    		else
    			errorAngle = TICKS_PAR_TOUR_ROBOT - e;
    	}
    }

    /**
     * Restreint l'erreur � un demi-tour.
     * UNUSED
     */
    void updateErrorAngleDemiPlan()
    {
    	if(errorAngle < TICKS_PAR_TOUR_ROBOT/4)
    		errorAngle += TICKS_PAR_TOUR_ROBOT/2;
    	else if(errorAngle > TICKS_PAR_TOUR_ROBOT/4)
    		errorAngle -= TICKS_PAR_TOUR_ROBOT/2;
    }

    void updateErrorTranslation()
    {
    	int32_t e = (int32_t) (hypot(x_odo - consigneX, y_odo - consigneY) / MM_PAR_TICK);
    	// faut-il aller en marche arri�re ?
    	if(cos_orientation_odo * (consigneX - x_odo) + sin_orientation_odo * (consigneY - y_odo) < 0)
    		e = -e;
    	errorTranslation = e;
    }

	void controlTranslation()
	{
    	updateErrorTranslation();

		translationPID.compute();	// Actualise la valeur de 'translationSpeed'
		marcheAvant = errorTranslation >= 0;

    	// On ne met pas � jour l'orientation si on est � moins de 3 cm de l'arriv�e. Sinon, en d�passant la consigne le robot voudra se retourner�
		if(errorTranslation >= 30/MM_PAR_TICK)
		{
			updateRotationSetpoint(); // g�re la marche arri�re
			updateErrorAngle();
		}

		rotationPID.compute();		// Actualise la valeur de 'rotationSpeed'

		// gestion de la sym�trie pour les d�placements
		if(isSymmetry)
			rotationSpeed = -rotationSpeed;

		// Limitation de la consigne de vitesse en translation
		if(translationSpeed > VITESSE_LINEAIRE_MAX)
			translationSpeed = VITESSE_LINEAIRE_MAX;
		else if(translationSpeed < -VITESSE_LINEAIRE_MAX)
			translationSpeed = -VITESSE_LINEAIRE_MAX;

		// Limitation de l'acc�l�ration en vitesse lin�aire
		if(translationSpeed - previousTranslationSpeedSetpoint > ACCELERATION_LINEAIRE_MAX)
			translationSpeed = previousTranslationSpeedSetpoint + ACCELERATION_LINEAIRE_MAX;
		else if(translationSpeed - previousTranslationSpeedSetpoint < -ACCELERATION_LINEAIRE_MAX)
			translationSpeed = previousTranslationSpeedSetpoint - ACCELERATION_LINEAIRE_MAX;

		previousTranslationSpeedSetpoint = translationSpeed;

		// Limitation de la consigne de vitesse en rotation
		if(rotationSpeed > VITESSE_ROTATION_MAX)
			rotationSpeed = VITESSE_ROTATION_MAX;
		else if(rotationSpeed < -VITESSE_ROTATION_MAX)
			rotationSpeed = -VITESSE_ROTATION_MAX;

		// Limitation de l'acc�l�ration en rotation
		if(rotationSpeed - previousRotationSpeedSetpoint > ACCELERATION_ROTATION_MAX)
			rotationSpeed = previousRotationSpeedSetpoint + ACCELERATION_ROTATION_MAX;
		else if(rotationSpeed - previousRotationSpeedSetpoint < -ACCELERATION_ROTATION_MAX)
			rotationSpeed = previousRotationSpeedSetpoint - ACCELERATION_ROTATION_MAX;

		previousRotationSpeedSetpoint = rotationSpeed;

		int32_t leftSpeedSetpoint = translationSpeed - rotationSpeed;
		int32_t rightSpeedSetpoint = translationSpeed + rotationSpeed;

		// Limitation de la vitesses
		if(leftSpeedSetpoint > VITESSE_ROUE_MAX)
			leftSpeedSetpoint = VITESSE_ROUE_MAX;
		else if(leftSpeedSetpoint < -VITESSE_ROUE_MAX)
			leftSpeedSetpoint = -VITESSE_ROUE_MAX;
		if(rightSpeedSetpoint > VITESSE_ROUE_MAX)
			rightSpeedSetpoint = VITESSE_ROUE_MAX;
		else if(rightSpeedSetpoint < -VITESSE_ROUE_MAX)
			rightSpeedSetpoint = -VITESSE_ROUE_MAX;

		// Limitation de l'acc�l�ration du moteur gauche
		if(leftSpeedSetpoint - previousLeftSpeedSetpoint > ACCELERATION_ROUE_MAX)
			leftSpeedSetpoint = previousLeftSpeedSetpoint + ACCELERATION_ROUE_MAX;
		else if(leftSpeedSetpoint - previousLeftSpeedSetpoint < -ACCELERATION_ROUE_MAX)
			leftSpeedSetpoint = previousLeftSpeedSetpoint - ACCELERATION_ROUE_MAX;

		// Limitation de l'acc�l�ration du moteur droit
		if(rightSpeedSetpoint - previousRightSpeedSetpoint > ACCELERATION_ROUE_MAX)
			rightSpeedSetpoint = previousRightSpeedSetpoint + ACCELERATION_ROUE_MAX;
		else if(rightSpeedSetpoint - previousRightSpeedSetpoint < -ACCELERATION_ROUE_MAX)
			rightSpeedSetpoint = previousRightSpeedSetpoint - ACCELERATION_ROUE_MAX;

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

	/**
	 * Asservissement, � la fois sur la distance et l'angle
	 * On suppose que les erreurs sont � jours
	 */
	void controlDistanceAngle()
	{

	}

    void controlRotation()
    {
    	// Mise � jour des erreurs
    	updateErrorAngle();
    	updateErrorTranslation();

        rotationPID.compute();      // Actualise la valeur de 'rotationSpeed'
        translationPID.compute();      // Actualise la valeur de 'rotationSpeed'

        // gestion de la sym�trie pour les d�placements
        if(isSymmetry)
            rotationSpeed = -rotationSpeed;

        errorLeftSpeed = translationSpeed - rotationSpeed - currentLeftSpeed;
        errorRightSpeed = translationSpeed + rotationSpeed - currentRightSpeed;
        
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
    	errorRightSpeed = currentRightSpeed;
    	errorLeftSpeed = currentLeftSpeed;

        leftSpeedPID.compute();     // Actualise la valeur de 'leftPWM'
        rightSpeedPID.compute();    // Actualise la valeur de 'rightPWM'
            
        leftMotor.run(leftPWM);
        rightMotor.run(rightPWM);
    }

    // Sommes-nous arriv�s ?
    bool checkBlocageMecanique()
    {
        return false; // TODO
    }

    // Sommes-nous arriv�s ?
    bool checkArrivee()
    {
        return ABS(leftPWM) < 5 && ABS(rightPWM) < 5 && ABS(rotationSpeed) < 10 && ABS(translationSpeed) < 10; // TODO
    }



#endif
