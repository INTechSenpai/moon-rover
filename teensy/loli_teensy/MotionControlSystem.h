// MotionControlSystem.h

#ifndef _MOTIONCONTROLSYSTEM_h
#define _MOTIONCONTROLSYSTEM_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Singleton.h"
#include "Motor.h"
#include "PID.h"
#include "Average.h"
#include "Encoder.h"
#include "Position.h"
#include "Trajectory.h"
#include "DirectionController.h"
#include "Log.h"
#include "BlockingMgr.h"
#include <math.h>
#include <EEPROM.h>


#define FREQ_ASSERV			1000	// Fréquence d'asservissement (en Hz)
#define AVERAGE_SPEED_SIZE	50		// Nombre de valeurs à utiliser dans le calcul de la moyenne glissante permettant de lisser la mesure de vitesse


class MotionControlSystem : public Singleton<MotionControlSystem>
{
private:
	Motor motor;
	Encoder leftMotorEncoder;
	Encoder rightMotorEncoder;

	Encoder leftFreeEncoder;
	Encoder rightFreeEncoder;

	DirectionController & direction;

	// Trajectoire en cours de parcours
	TrajectoryPoint currentTrajectory[UINT8_MAX + 1];
	
	// Point de la trajectoire courante sur lequel le robot se situe actuellement
	volatile uint8_t trajectoryIndex;

	// Prochain point d'arrêt sur la trajectoire courante. Tant qu'aucun point d'arrêt n'a été reçu, nextStopPoint vaut MAX_UINT_16.
	volatile uint16_t nextStopPoint;

	// Position absolue du robot sur la table (en mm et radians)
	volatile Position position;


	/*
	* 		Définition des variables d'état du système (position, vitesse, consigne, ...)
	*
	* 		Les unités sont :
	* 			Pour les distances		: ticks
	* 			Pour les vitesses		: ticks/seconde
	* 			Ces unités seront vraies pour une fréquence d'asservissement égale à FREQ_ASSERV
	*/

	//	Asservissement en vitesse du moteur droit
	PID rightSpeedPID;
	volatile int32_t rightSpeedSetpoint;	// ticks/seconde
	volatile int32_t currentRightSpeed;		// ticks/seconde
	volatile int32_t rightPWM;
	BlockingMgr rightMotorBlockingMgr;

	//	Asservissement en vitesse du moteur gauche
	PID leftSpeedPID;
	volatile int32_t leftSpeedSetpoint;		// ticks/seconde
	volatile int32_t currentLeftSpeed;		// ticks/seconde
	volatile int32_t leftPWM;
	BlockingMgr leftMotorBlockingMgr;

	//	Asservissement en position : translation
	//  (Ici toutes les grandeurs sont positives, le sens de déplacement sera donné par maxMovingSpeed)
	PID translationPID;
	volatile int32_t translationSetpoint;	// ticks
	volatile int32_t currentDistance;		// ticks
	volatile int32_t movingSpeedSetpoint;	// ticks/seconde
	
	StoppingMgr endOfMoveMgr;
	volatile int32_t currentMovingSpeed;	// ticks/seconde

	//  Asservissement sur trajectoire
	volatile float curvatureOrder;	// Consigne de courbure, en m^-1
	float curvatureCorrectorK1;		// Coefficient du facteur "erreur de position"
	float curvatureCorrectorK2;		// Coefficient du facteur "erreur d'orientation"
	
	//  Vitesse (algébrique) de translation maximale : une vitesse négative correspond à une marche arrière
	volatile int32_t maxMovingSpeed;	// en ticks/seconde

	//  Pour le calcul de l'accélération :
	volatile int32_t previousMovingSpeed;	// en ticks.s^-2

	//  Accélération maximale (variation maximale de movingSpeedSetpoint)
	int32_t maxAcceleration;	// ticks*s^-2

	//	Pour faire de jolies courbes de réponse du système, la vitesse moyenne c'est mieux !
	Average<int32_t, AVERAGE_SPEED_SIZE> averageLeftSpeed;
	Average<int32_t, AVERAGE_SPEED_SIZE> averageRightSpeed;
	Average<int32_t, AVERAGE_SPEED_SIZE> averageCurrentSpeed;

public:
	// Type décrivant l'état du mouvement
	enum MovingState
	{
		STOPPED,		// Robot à l'arrêt, à la position voulue.
		MOVE_INIT,		// L'ordre de mouvement a été reçu, mais le robot n'envoie pas encore un PWM aux moteurs de propulsion (il attend d'avoir les roues de direction en position)
		MOVING,			// Robot en mouvent vers la position voulue.
		EXT_BLOCKED,	// Robot bloqué par un obstacle extérieur (les roues patinent).
		INT_BLOCKED,	// Roues du robot bloquées.
		EMPTY_TRAJ		// La trajectoire courante est terminée, le dernier point n'étant pas un point d'arrêt.
	};

	// Type désignant le(s) PID en cours de réglage
	enum PIDtoSet
	{
		LEFT_SPEED,
		RIGHT_SPEED,
		SPEED,
		TRANSLATION
	};
	
private:
	volatile MovingState movingState;

	// Variables d'activation des différents PID
	bool positionControlled;	//  Asservissement en position
	bool leftSpeedControlled;	//	Asservissement en vitesse à gauche
	bool rightSpeedControlled;	//	Asservissement en vitesse à droite
	bool pwmControlled;		//	Mise à jour des PWM grâce à l'asservissement en vitesse

	PIDtoSet pidToSet; // Indique lequel des PID est en cours de réglage

public:
	MotionControlSystem();

	/* Asservissement (fonction à appeller dans l'interruption associée) */
	void control();
private:
	/* Mise à jour des variables :
		position
		currentRightSpeed (maj + filtrage)
		currentLeftSpeed (maj + filtrage)
		currentDistance
		currentMovingSpeed (maj + filtrage) */
	void updateSpeedAndPosition();

	/* Mise à jour des variables :
		trajectoryIndex
		nextStopPoint
		currentDistance (si trajectoryIndex a été incrémenté)
		translationSetpoint (si nextStopPoint a été modifié) */
	void updateTrajectoryIndex();

	void manageStop();
	void manageBlocking();
public:

	/* Activation et désactivation de l'asserv */
	void enablePositionControl(bool);
	void enableLeftSpeedControl(bool);
	void enableRightSpeedControl(bool);
	void enablePwmControl(bool);
	void getEnableStates(bool &, bool &, bool &, bool&);

	/* Gestion des déplacements */
	void addTrajectoryPoint(const TrajectoryPoint &, uint8_t);
	MovingState getMovingState() const;
	void gotoNextStopPoint();
	void stop();
	void setMaxMovingSpeed(int32_t);
	int32_t getMaxMovingSpeed() const;
	void setMaxAcceleration(int32_t);
	int32_t getMaxAcceleration() const;

	/* Setters et getters des constantes d'asservissement */
	void setCurrentPIDTunings(float, float, float);
	void getCurrentPIDTunings(float &, float &, float &) const;
	void setTranslationTunings(float, float, float);
	void setLeftSpeedTunings(float, float, float);
	void setRightSpeedTunings(float, float, float);
	void setTrajectoryTunings(float, float);
	void getTranslationTunings(float &, float &, float &) const;
	void getLeftSpeedTunings(float &, float &, float &) const;
	void getRightSpeedTunings(float &, float &, float &) const;
	void getTrajectoryTunings(float &, float &) const;

	void setPIDtoSet(PIDtoSet newPIDtoSet);
	PIDtoSet getPIDtoSet() const;
	void getPIDtoSet_str(char*, size_t) const;

	/* Setter et getter de la position */
	void setPosition(const Position &);
	void getPosition(Position &) const;
	uint8_t getTrajectoryIndex() const;
	void resetPosition(void);

	/* Setters et getters des gestionaires de blocage et d'arrêt */
	void setLeftMotorBmgrTunings(float, uint32_t);
	void setRightMotorBmgrTunings(float, uint32_t);
	void setEndOfMoveMgrTunings(uint32_t, uint32_t);
	void getLeftMotorBmgrTunings(float &, uint32_t &) const;
	void getRightMotorBmgrTunings(float &, uint32_t &) const;
	void getEndOfMoveMgrTunings(uint32_t &, uint32_t &) const;

	/* Getters de débug */
	void getTicks(int32_t &, int32_t &, int32_t &, int32_t &);

	/* Sauvegarde et restauration des paramètres de l'asservissement */
	void saveParameters();
	void loadParameters();
	void loadDefaultParameters();
};


#endif

