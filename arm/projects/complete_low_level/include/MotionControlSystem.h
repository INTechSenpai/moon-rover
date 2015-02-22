#ifndef MOTION_CONTROL_H_
#define MOTION_CONTROL_H_

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

#define NB_SPEED 4 //Nombre de vitesses différentes gérées par l'asservissement
#define NB_CTE_ASSERV 4 //Nombre de variables constituant un asservissement : pwmMAX, kp, ki, kd

#define TRACKER_SIZE 1024

extern Uart<1> serial;

class MotionControlSystem : public Singleton<MotionControlSystem> {
private:
	Motor leftMotor;
	Motor rightMotor;
	bool translationControlled;
	bool rotationControlled;
	PID translationPID;
	PID rotationPID;

	float originalAngle;

	//Consignes à atteindre en tick
	int32_t rotationSetpoint;
	int32_t translationSetpoint;

	//Angle et distance en tick au dernier refresh


	int16_t pwmRotation;
	int16_t pwmTranslation;
	int16_t maxPWMtranslation;
	int16_t maxPWMrotation;
	float balance; //Pour tout PWM on a : balance = PWM_moteur_droit/PWM_moteur_gauche
	float x;
	float y;
	bool moving;
	bool moveAbnormal;
	float translationTunings[NB_SPEED][NB_CTE_ASSERV];
	float rotationTunings[NB_SPEED][NB_CTE_ASSERV];

	float trackArray[TRACKER_SIZE][5];

	void applyControl();
	bool isPhysicallyStopped(int);//Indique si le robot est immobile, avec une certaine tolérance passée en argument, exprimmée en ticks*[fréquence d'asservissement]

public:

	MotionControlSystem();
    MotionControlSystem (const MotionControlSystem&): leftMotor(Side::LEFT), rightMotor(Side::RIGHT),translationControlled(
			true), rotationControlled(true), translationPID(
			&currentDistance, &pwmTranslation, &translationSetpoint), rotationPID(
			&currentAngle, &pwmRotation, &rotationSetpoint), originalAngle(0.0),rotationSetpoint(
			0), translationSetpoint(0), x(
			0), y(0), moving(false), moveAbnormal(false) {
    }
	int32_t currentDistance;
	int32_t currentAngle;
	void init(int16_t maxPWMtranslation, int16_t maxPWMrotation);

	void control();
	void updatePosition();
	void manageStop();
	void track();///Stock les valeurs de position et de pwm dans un tableau
	void printTracking();///Affiche le tableau de positions et pwm enregistées
	void clearTracking();///Vider le tableau des positions et pwm

	int getPWMTranslation();
	int getPWMRotation();
	int getTranslationGoal();
	int getRotationGoal();
	int getLeftEncoder();
	int getRightEncoder();

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
	float getX();
	float getY();
	void setX(float);
	void setY(float);
	float getBalance();
	void setBalance(float newBalance);
	int16_t getMaxPWMtranslation();
	int16_t getMaxPWMrotation();
	void setMaxPWMtranslation(int16_t);
	void setMaxPWMrotation(int16_t);

	/*
	 * Règlage des constantes d'asservissement et du pwm
	 * à partir du pwm donné en argument et de la base de
	 * donnée de constantes compatibles qui associent
	 * chaque pwm à des constanes d'asservissement.
	 */
	void setSmartTranslationTunings();
	void setSmartRotationTunings();
	int getBestTuningsInDatabase(int16_t pwm, float[NB_SPEED][NB_CTE_ASSERV]);

	bool isMoving();
	bool isMoveAbnormal();
};

#endif /* MOTION_CONTROL_H_ */
