#ifndef MOTION_CONTROL_H_
#define MOTION_CONTROL_H_

#define DEBUG	1

#include "Singleton.hpp"
#include "Motor.h"
#include "pid.hpp"
#include <math.h>
#include "delay.h"
#include "misc.h"
#include "Counter.h"
#include <Uart.hpp>

#define PI 3.14159265
//#define PI_TIC 24809 // pi/TICK_TO_RADIAN
#define PI_TIC 2171

/**
 * 123,825 mm : diametre des roues
 * 12000 ticks par tour de roue
 */
#define PERIMETER_MM 123.825*PI
//#define TICK_TO_MM 0.0324173 // PERIMETER_MM/12000
#define TICK_TO_MM 0.2077
//#define TICK_TO_RADIAN 0.00012663 // TICK_TO_MM/256 : entre roues de 25.6cm
#define TICK_TO_RADIAN 0.0014468

#define NB_SPEED 4 //Nombre de vitesses diff�rentes g�r�es par l'asservissement
#define NB_CTE_ASSERV 4 //Nombre de variables constituant un asservissement : pwmMAX, kp, ki, kd

#if DEBUG
#define TRACKER_SIZE 1024
#else
#define TRACKER_SIZE 1
#endif

extern Uart<1> serial;

class MotionControlSystem : public Singleton<MotionControlSystem> {
private:
	Motor leftMotor;
	Motor rightMotor;
	volatile bool translationControlled;
	volatile bool rotationControlled;
	PID translationPID;
	PID rotationPID;

	volatile float originalAngle;

	//Consignes � atteindre en tick
	volatile int32_t rotationSetpoint;
	volatile int32_t translationSetpoint;

	volatile int16_t pwmRotation;
	volatile int16_t pwmTranslation;
	volatile int16_t maxPWMtranslation;
	volatile int16_t maxPWMrotation;
	float balance; //Pour tout PWM on a : balance = PWM_moteur_droit/PWM_moteur_gauche
	volatile float x;
	volatile float y;
	volatile bool moving;
	volatile bool moveAbnormal;
	float translationTunings[NB_SPEED][NB_CTE_ASSERV];
	float rotationTunings[NB_SPEED][NB_CTE_ASSERV];


	/*
	 * Dispositif d'enregistrement de l'�tat du syst�me pour permettre le d�bug
	 * La valeur de TRACKER_SIZE d�pend de la valeur de DEBUG.
	 */

	struct trackerType
	{
		float x;
		float y;
		float angle;
		int consigneTranslation;
		int consigneRotation;
		int translationCourante;
		int rotationCourante;
		bool asservTranslation;
		bool asservRotation;
		uint8_t pwmTranslation;
		uint8_t pwmRotation;
		uint8_t tailleBufferReception;
	};

	trackerType trackArray[TRACKER_SIZE];

	void applyControl();
	bool isPhysicallyStopped(int);//Indique si le robot est immobile, avec une certaine tol�rance pass�e en argument, exprimm�e en ticks*[fr�quence d'asservissement]


	/*
	 * Constantes de r�glage de la d�tection de blocage physique
	 */
	unsigned int delayToStop;//En ms
	int toleranceInTick;//Nombre de ticks de tol�rance pour consid�rer qu'on est arriv� � destination
	int pwmMinToMove;//PWM minimal en dessous duquel le robot ne bougera pas
	int minSpeed;//Vitesse en dessous de laquelle on consid�re la vitesse comme nulle (en tick*[freq d'asserv])


public:

	MotionControlSystem();
    MotionControlSystem (const MotionControlSystem&);

	volatile int32_t currentDistance;
	volatile int32_t currentAngle;
	void init(int16_t maxPWMtranslation, int16_t maxPWMrotation);

	void control();
	void updatePosition();
	void manageStop();

	void track();///Stock les valeurs de d�bug
	void printTrackingOXY();///Affiche les x,y,angle du tableau de tracking
	void printTrackingAll();///Affiche l'int�gralit� du tableau de tracking
	void printTrackingLocomotion();
	void printTrackingSerie();

	int getPWMTranslation() const;
	int getPWMRotation() const;
	int getTranslationGoal() const;
	int getRotationGoal() const;
	int getLeftEncoder() const;
	int getRightEncoder() const;

	void enable(bool);
	void enableTranslationControl(bool);
	void enableRotationControl(bool);

	void orderTranslation(int32_t);
	void orderRotation(float);
	void orderRawPwm(Side,int16_t);
	void stop();
	static int32_t optimumAngle(int32_t,int32_t);

	void setTranslationTunings(float, float, float);
	void setRotationTunings(float, float, float);
	void getTranslationTunings(float &,float &,float &) const;
	void getRotationTunings(float &,float &,float &) const;

	float getAngleRadian() const;
	void setOriginalAngle(float);
	float getX() const;
	float getY() const;
	void setX(float);
	void setY(float);
	float getBalance() const;
	void setBalance(float newBalance);
	int16_t getMaxPWMtranslation() const;
	int16_t getMaxPWMrotation() const;
	void setMaxPWMtranslation(int16_t);
	void setMaxPWMrotation(int16_t);

	/*
	 * R�glage des constantes d'asservissement et du pwm
	 * � partir du pwm donn� en argument et de la base de
	 * donn�e de constantes compatibles qui associent
	 * chaque pwm � des constanes d'asservissement.
	 */
	void setSmartTranslationTunings();
	void setSmartRotationTunings();
	int getBestTuningsInDatabase(int16_t pwm, float[NB_SPEED][NB_CTE_ASSERV]) const;

	bool isMoving() const;
	bool isMoveAbnormal() const;
};

#endif /* MOTION_CONTROL_H_ */
