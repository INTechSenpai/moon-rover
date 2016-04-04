#ifndef ASSER
#define ASSER

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
#define RAYON_DE_COURBURE_MIN_EN_MM 500
#define COURBURE_MAX (1. / RAYON_DE_COURBURE_MIN_EN_MM)

#define FONCTION_VITESSE_MAX(x) (5)
#define FONCTION_COURBURE_MAX(x) (5)

#define TAILLE_MAX_TRAJECTOIRE 256 // et comme �a on utilise un indice sur un uint_8
#define TAILLE_MAX_ARRET 16

#define MOTEUR_DROIT (TIM8->CCR1)
#define MOTEUR_GAUCHE (TIM8->CCR2)


// Tuning de pid : https://en.wikipedia.org/wiki/Ziegler%E2%80%93Nichols_method

typedef struct
{
int16_t x;
int16_t y;
int16_t dir_x;
int16_t dir_y;
uint32_t orientation;
float courbure;
uint8_t vitesse;
} pointAsserCourbe;

volatile pointAsserCourbe trajectoire[TAILLE_MAX_TRAJECTOIRE];
volatile pointAsserCourbe* arcsArret[TAILLE_MAX_ARRET];

volatile int8_t indiceTrajectoireLecture = 0;
volatile int8_t indiceTrajectoireEcriture = 0;

volatile int8_t indiceArretLecture = 0;
volatile int8_t indiceArretEcriture = 0;

enum MOVING_DIRECTION {FORWARD, BACKWARD, NONE};

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

//	int32_t currentRightAcceleration;
//	int32_t currentLeftAcceleration;
	float leftSpeedSetpoint;
	float rightSpeedSetpoint;

	//	Asservissement en vitesse du moteur droit
	float currentRightSpeed;		// ticks/seconde
	float errorRightSpeed;
	float rightPWM = 0;
	PID rightSpeedPID(&errorRightSpeed, &rightPWM, 5);

	//	Asservissement en vitesse du moteur gauche
	float currentLeftSpeed;		// ticks/seconde
	float errorLeftSpeed;
	float leftPWM = 0;
	PID leftSpeedPID(&errorLeftSpeed, &leftPWM, 5);

	//	Asservissement en vitesse lin�aire et en courbure
	float vitesseLineaireReelle;
	float vitesseRotationReelle;
	float courbureReelle;
	volatile float consigneVitesseLineaire;
	volatile float consigneCourbure;
	PIDvitesse PIDvit(&vitesseLineaireReelle, &courbureReelle, &leftPWM, &rightPWM, &consigneVitesseLineaire, &consigneCourbure);

	//	Asservissement en position : translation
	float currentDistance;		// distance � parcourir, en ticks
	float translationSpeed;		// ticks/seconde
	float errorTranslation;		// ticks
	PID translationPID(&errorTranslation, &translationSpeed, 10);

	//	Asservissement en position : rotation

	float currentAngle = 0;
	float errorAngle;
	float rotationSpeed;			// ticks/seconde
	PID rotationPID(&errorAngle, &rotationSpeed, 0);

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

	volatile float k1;
	volatile float k2;

    /**
     * Utilise consigneX, consigneY, x_odo et y_odo pour calculer rotationSetpoint
     */
    void inline updateRotationSetpoint()
    {
    	float tmp = RAD_TO_TICK(atan2(consigneY - y_odo, consigneX - x_odo));
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
    void inline updateErrorAngle()
    {
    	float e;
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
    void inline updateErrorAngleDemiPlan()
    {
    	if(errorAngle < TICKS_PAR_TOUR_ROBOT/4)
    		errorAngle += TICKS_PAR_TOUR_ROBOT/2;
    	else if(errorAngle > TICKS_PAR_TOUR_ROBOT/4)
    		errorAngle -= TICKS_PAR_TOUR_ROBOT/2;
    }

    void inline updateErrorTranslation()
    {
    	// g�re aussi la marche arri�re
    	errorTranslation = (cos_orientation_odo * (consigneX - x_odo) + sin_orientation_odo * (consigneY - y_odo)) / MM_PAR_TICK;
    }

    void inline runPWM()
    {
		int32_t tmpRightPWM, tmpLeftPWM;

		if(leftPWM > PWM_MAX)
			leftPWM = PWM_MAX;
		else if(leftPWM < -PWM_MAX)
			leftPWM = -PWM_MAX;

		if(rightPWM > PWM_MAX)
			rightPWM = PWM_MAX;
		else if(rightPWM < -PWM_MAX)
			rightPWM = -PWM_MAX;

		// gestion de la sym�trie pour les d�placements
        if(isSymmetry)
        {
        	tmpRightPWM = leftPWM;
        	tmpLeftPWM = rightPWM;
        }
        else
        {
        	tmpRightPWM = rightPWM;
        	tmpLeftPWM = leftPWM;
        }

        // gestion marche avant / marche arri�re
		if(tmpRightPWM >= 0)
		{
			// marche avant
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12, GPIO_PIN_SET);
			MOTEUR_DROIT = tmpRightPWM;
		}
		else
		{
			// marche arri�re
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12, GPIO_PIN_RESET);
			MOTEUR_DROIT = -tmpRightPWM;
		}

		if(tmpLeftPWM >= 0)
		{
			// marche avant
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_10, GPIO_PIN_SET);
			MOTEUR_GAUCHE = tmpLeftPWM;
		}
		else
		{
			// marche arri�re
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_10, GPIO_PIN_RESET);
			MOTEUR_GAUCHE = -tmpLeftPWM;
		}
    }

    /**
     * Utilise les valeurs de errorLeftSpeed et errorRightSpeed pour mettre � jour les moteurs
     * S'occupe aussi de la sym�trisation
     */
	void inline computeAndRunPWM()
	{
		leftSpeedPID.compute();		// Actualise la valeur de 'leftPWM'
		rightSpeedPID.compute();	// Actualise la valeur de 'rightPWM'

		runPWM();
	}

	/**
	 * Limite la vitesse et l'acc�l�ration lin�aire et en rotation
	 */
	void inline limitTranslationRotationSpeed()
	{

		// Limitation de la consigne de vitesse en translation
		if(translationSpeed > VITESSE_LINEAIRE_MAX)
			translationSpeed = VITESSE_LINEAIRE_MAX;
		else if(translationSpeed < -VITESSE_LINEAIRE_MAX)
			translationSpeed = -VITESSE_LINEAIRE_MAX;

		// Limitation de l'acc�l�ration en vitesse lin�aire
		if(translationSpeed - vitesseLineaireReelle > ACCELERATION_LINEAIRE_MAX)
			translationSpeed = vitesseLineaireReelle + ACCELERATION_LINEAIRE_MAX;
		else if(translationSpeed - vitesseLineaireReelle < -ACCELERATION_LINEAIRE_MAX)
			translationSpeed = vitesseLineaireReelle - ACCELERATION_LINEAIRE_MAX;

		// Limitation de la consigne de vitesse en rotation
		if(rotationSpeed > VITESSE_ROTATION_MAX)
			rotationSpeed = VITESSE_ROTATION_MAX;
		else if(rotationSpeed < -VITESSE_ROTATION_MAX)
			rotationSpeed = -VITESSE_ROTATION_MAX;

		// Limitation de l'acc�l�ration en rotation
		if(rotationSpeed - vitesseRotationReelle > ACCELERATION_ROTATION_MAX)
			rotationSpeed = vitesseRotationReelle + ACCELERATION_ROTATION_MAX;
		else if(rotationSpeed - vitesseRotationReelle < -ACCELERATION_ROTATION_MAX)
			rotationSpeed = vitesseRotationReelle - ACCELERATION_ROTATION_MAX;
	}

	/**
	 * Limite la vitesse et l'acc�l�ration de chaque roue
	 */
	void inline limitLeftRightSpeed()
	{
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
		if(leftSpeedSetpoint - currentLeftSpeed > ACCELERATION_ROUE_MAX)
			leftSpeedSetpoint = currentLeftSpeed + ACCELERATION_ROUE_MAX;
		else if(leftSpeedSetpoint - currentLeftSpeed < -ACCELERATION_ROUE_MAX)
			leftSpeedSetpoint = currentLeftSpeed - ACCELERATION_ROUE_MAX;

		// Limitation de l'acc�l�ration du moteur droit
		if(rightSpeedSetpoint - currentRightSpeed > ACCELERATION_ROUE_MAX)
			rightSpeedSetpoint = currentRightSpeed + ACCELERATION_ROUE_MAX;
		else if(rightSpeedSetpoint - currentRightSpeed < -ACCELERATION_ROUE_MAX)
			rightSpeedSetpoint = currentRightSpeed - ACCELERATION_ROUE_MAX;
	}

	void inline controlVaAuPoint()
	{
    	updateErrorTranslation();

		translationPID.compute();	// Actualise la valeur de 'translationSpeed'
		marcheAvant = errorTranslation >= 0;

    	// On ne met pas � jour l'orientation si on est � moins de 3 cm de l'arriv�e. Sinon, en d�passant la consigne le robot voudra se retourner�
		if(errorTranslation >= 30/MM_PAR_TICK)
		{
//			updateRotationSetpoint(); // g�re la marche arri�re
//			updateErrorAngle();
//			rotationPID.compute();		// Actualise la valeur de 'rotationSpeed'
		}
		else // si on est trop proche, on ne tourne plus
		{
//			translationSpeed = 0;
//			rotationSpeed = 0;
		}
		rotationSpeed = 0;

//		limitTranslationRotationSpeed();

		leftSpeedSetpoint = translationSpeed - rotationSpeed;
		rightSpeedSetpoint = translationSpeed + rotationSpeed;

//		limitLeftRightSpeed();

        errorLeftSpeed = leftSpeedSetpoint - currentLeftSpeed;
		errorRightSpeed = rightSpeedSetpoint - currentRightSpeed;

		computeAndRunPWM();
	}

    void inline controlRotation()
    {
    	// Mise � jour des erreurs
    	updateErrorAngle();
    	updateErrorTranslation();

        rotationPID.compute();      // Actualise la valeur de 'rotationSpeed'
        translationPID.compute();      // Actualise la valeur de 'translationSpeed'

        limitTranslationRotationSpeed();

		leftSpeedSetpoint = translationSpeed - rotationSpeed;
		rightSpeedSetpoint = translationSpeed + rotationSpeed;

		limitLeftRightSpeed();

        errorLeftSpeed = leftSpeedSetpoint - currentLeftSpeed;
		errorRightSpeed = rightSpeedSetpoint - currentRightSpeed;

        computeAndRunPWM();
    }

    void inline controlVitesse()
    {
    	leftSpeedSetpoint = 0;
    	rightSpeedSetpoint = 100;

//        limitLeftRightSpeed();
        errorLeftSpeed = leftSpeedSetpoint - currentLeftSpeed;
		errorRightSpeed = rightSpeedSetpoint - currentRightSpeed;

        computeAndRunPWM();
    }

    void inline controlTrajectoire()
    {
    	// a-t-on d�pass� un point ?
    	if((x_odo - trajectoire[indiceTrajectoireLecture].x) * trajectoire[indiceTrajectoireLecture].dir_x + (y_odo - trajectoire[indiceTrajectoireLecture].y) * trajectoire[indiceTrajectoireLecture].dir_y)
    		indiceTrajectoireLecture++;

    	rotationSetpoint = trajectoire[indiceTrajectoireLecture].orientation;
    	updateErrorAngle();

    	// Produit scalaire. d est alg�brique
    	int16_t d = - (x_odo - trajectoire[indiceTrajectoireLecture].x) * trajectoire[indiceTrajectoireLecture].dir_y + (y_odo - trajectoire[indiceTrajectoireLecture].y) * trajectoire[indiceTrajectoireLecture].dir_x;

    	float kappaS = trajectoire[indiceTrajectoireLecture].courbure - k1*d - k2*errorAngle;
    	float consigneCourbure = FONCTION_COURBURE_MAX(vitesseLineaireReelle);
    	if(consigneCourbure > kappaS)
    		consigneCourbure = kappaS;
    	if(consigneCourbure > COURBURE_MAX)
    		consigneCourbure = COURBURE_MAX;
    	float consigneVitesseLineaire = FONCTION_VITESSE_MAX(kappaS);
        
    	// TODO : mettre � jour indiceArretLecture
    	consigneX = arcsArret[indiceArretLecture]->x;
    	consigneY = arcsArret[indiceArretLecture]->y;
        updateErrorTranslation();
        limitTranslationRotationSpeed();
        if(consigneVitesseLineaire > translationSpeed)
            consigneVitesseLineaire = translationSpeed;
    	if(consigneVitesseLineaire > VITESSE_LINEAIRE_MAX)
    		consigneVitesseLineaire = VITESSE_LINEAIRE_MAX;
    	PIDvit.compute();
        computeAndRunPWM();

	//	projection(&xR, &yR);
		// calcul orientation en R, courbure en R, distance R-robot
		// calcul courbureSamson
		// calcul courbureConsigne
		// calcul vitesseConsigne
		// calcul vitesseLineaire avec pidTranslation
		// asser vitesse + courbure
    }

    // freine le plus rapidement possible. Note : on ne garantit rien sur l'orientation, si une roue freine plus vite que l'autre le robot va tourner.
    void inline controlStop()
    {
    	leftSpeedSetpoint = 0;
    	rightSpeedSetpoint = 0;

        limitLeftRightSpeed(); // limite la d�c�l�ration en modifiant leftSpeedSetpoint et rightSpeedSetpoint
        errorLeftSpeed = leftSpeedSetpoint - currentLeftSpeed;
		errorRightSpeed = rightSpeedSetpoint - currentRightSpeed;

        computeAndRunPWM();
    }

    uint16_t nbErreurs = 0;

    /**
     * On est physiquement bloqu� si l'erreur en vitesse de change pas alors que les moteurs tournent
     */
    bool inline isPhysicallyStopped()
    {
    	return (ABS(leftPWM) > 5) && (ABS(rightPWM) > 5) && (ABS(leftSpeedPID.getDerivativeError())) < 5 && (ABS(rightSpeedPID.getDerivativeError()) < 5);
    }

    // Y a-t-il un probl�me m�canique ?
    bool inline checkBlocageMecanique()
    {
    	if(isPhysicallyStopped())
    	{
    		nbErreurs++;
			if(nbErreurs >= DELAI_ERREUR_MECA_APPEL)
			{
				nbErreurs = 0;
				return true;
			}
    	}
    	else // pas de blocage, tout va bien
    		nbErreurs = 0;

        return false;
    }


    /**
     * Sommes-nous arriv�s ?
     * On v�rifie que les moteurs ne tournent plus et que le robot est arr�t�
     */
    bool inline checkArrivee()
    {
    	// TODO�:�v�rifier aussi qu'on est bien arriv� � destination...
        return ABS(leftPWM) < 5 && ABS(rightPWM) < 5 && ABS(currentLeftSpeed) < 10 && ABS(currentRightSpeed) < 10;
    }

#endif
