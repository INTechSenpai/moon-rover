#ifndef _ACTUATORMGR_h
#define _ACTUATORMGR_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Singleton.h"
#include "utils.h"
#include "ControlerNet.h"
#include "DynamixelMotor.h"
#include "Dynamixel.h"
#include "InterfaceAX12.h"
#include "ax12config.h"


/* Positions de l'AX12, en degrés */
#define	ANGLE_DOWN		65
#define ANGLE_HALFWAY	80
#define ANGLE_UP		155
#define ANGLE_RELEASE	150

/* Durée maximale des mouvements de l'AX12, en ms */
#define AX12_NET_TIMEOUT	2000

/* Vitesses des mouvements de l'AX12, valeur comprise entre 1 et 1023, 0 correspond à une vitesse non régulée égale au maximum possible */
#define SLOW_SPEED	300		// Utilisée pour les mouvements vers le bas

#define TOLERANCE_ANGLE	5

#define FAN_STARTUP_DURATION	2000 // ms
#define FUNNY_ACTION_DURATION	5000 // ms


/*
	Chaque méthode est à exécuter en boucle, la permière fois avec l'argument 'launch' à true.
	Elle renvoie 'true' si l'action est ternimée, 'false' sinon.
	Si la méthode peut donner un retour sur la réussite de l'action, elle renvoie un uint8_t prenant les valeurs:
	SUCCESS:0x00	FAILURE:0x01	RUNNING:0xFF
*/
class ActuatorMgr : public Singleton<ActuatorMgr>
{
public:
	ActuatorMgr():
		ax12interface(InterfaceAX12::Instance()),
		ax12net(ax12interface.serial, ID_NET_AX12)
	{
		pinMode(PIN_VENTILATEUR, OUTPUT);
		digitalWrite(PIN_VENTILATEUR, LOW);
		ax12net.init();
		ax12net.enableTorque();
		ax12net.jointMode();
	}

	ActuatorStatus pullDownNet(bool launch)
	{
		static uint32_t lastCommTime;
		static uint32_t startTime = 0;
		if (timeoutCheck(launch, startTime))
		{
			return FAILURE;
		}
		return moveTheAX12(launch, ANGLE_DOWN, TOLERANCE_ANGLE, lastCommTime, SLOW_SPEED);
	}

	ActuatorStatus putNetHalfway(bool launch)
	{
		static uint32_t lastCommTime;
		static uint32_t startTime = 0;
		if (timeoutCheck(launch, startTime))
		{
			return FAILURE;
		}
		return moveTheAX12(launch, ANGLE_HALFWAY, TOLERANCE_ANGLE, lastCommTime, SLOW_SPEED);
	}

	ActuatorStatus pullUpNet(bool launch)
	{
		static uint32_t lastCommTime;
		static uint32_t startTime = 0;
		if (timeoutCheck(launch, startTime))
		{
			return FAILURE;
		}
		return moveTheAX12(launch, ANGLE_UP, TOLERANCE_ANGLE, lastCommTime);
	}

	ActuatorStatus openNet(bool launch)
	{
		return controlerNet.openNet(launch);
	}

	ActuatorStatus closeNet(bool launch)
	{
		return controlerNet.closeNet(launch);
	}

	ActuatorStatus lockNet(bool launch)
	{
		return controlerNet.lockNet(launch);
	}

	ActuatorStatus ejectLeftSide(bool launch)
	{
		return controlerNet.ejectLeftSide(launch);
	}

	ActuatorStatus rearmLeftSide(bool launch)
	{
		return controlerNet.rearmLeftSide(launch);
	}

	ActuatorStatus ejectRightSide(bool launch)
	{
		return controlerNet.ejectRightSide(launch);
	}

	ActuatorStatus rearmRightSide(bool launch)
	{
		return controlerNet.rearmRightSide(launch);
	}

	bool funnyAction(bool launch)
	{
		static uint32_t startTime;
		static uint32_t fanStartTime;
		static uint32_t lastCommTime;
		static uint8_t stage;
		if (launch)
		{
			controlerNet.stop();
			stage = 0;
			moveTheAX12(true, ANGLE_UP, TOLERANCE_ANGLE, lastCommTime);
			startTime = millis();
			return false;
		}
		else
		{
			if (stage == 0)
			{
				if (moveTheAX12(false, ANGLE_UP, TOLERANCE_ANGLE, lastCommTime))
				{ // AX12 en place pour bloquer la fusée
					digitalWrite(PIN_VENTILATEUR, HIGH);
					fanStartTime = millis();
					stage = 1;
				}
			}
			else if (stage == 1)
			{
				if (millis() - fanStartTime > FAN_STARTUP_DURATION)
				{
					moveTheAX12(true, ANGLE_RELEASE, TOLERANCE_ANGLE, lastCommTime);
					stage = 2;
				}
			}

			if (millis() - startTime > FUNNY_ACTION_DURATION)
			{
				digitalWrite(PIN_VENTILATEUR, LOW);
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	uint8_t crossFlipFlop()
	{
		// TODO
	}

	void setAngleNet(uint8_t angle)
	{
		ax12net.goalPositionDegree(angle);
	}

private:
	ControlerNet controlerNet;
	InterfaceAX12 ax12interface;
	DynamixelMotor ax12net;

	ActuatorStatus moveTheAX12(bool launch, uint16_t goalPosition, uint16_t tolerance, uint32_t & lastCommunicationTime, uint16_t speed = 0, uint16_t torque = 1023)
	{
		static bool orderFailed = false;
		if (launch)
		{
			orderFailed = false;
			if (ax12net.speed(speed) != DYN_STATUS_OK)
			{
				orderFailed = true;
			}
			if (ax12net.torqueLimit(torque) != DYN_STATUS_OK)
			{
				orderFailed = true;
			}
			if (ax12net.goalPositionDegree(goalPosition) != DYN_STATUS_OK)
			{
				orderFailed = true;
			}
			lastCommunicationTime = millis();
			return RUNNING;
		}
		else if (orderFailed)
		{
			return FAILURE;
		}
		else if (millis() - lastCommunicationTime > 100)
		{
			lastCommunicationTime = millis();
			int32_t currentPosition = ax12net.currentPositionDegree();
			if (ABS(currentPosition - (int32_t)goalPosition) <= (int32_t)tolerance)
			{
				return SUCCESS;
			}
			else
			{
				return RUNNING;
			}
			
		}
		else
		{
			return RUNNING;
		}
	}

	bool timeoutCheck(bool launch, uint32_t & startTime)
	{
		if (launch)
		{
			startTime = millis();
			return false;
		}
		else if (millis() - startTime > AX12_NET_TIMEOUT)
		{
			ax12net.enableTorque(); // En cas de mise en protection de l'AX12, on le réactive
			uint16_t currentPosition = ax12net.currentPositionDegree();
			if (currentPosition <= 300)
			{
				ax12net.goalPositionDegree(currentPosition);
			}
			return true;
		}
		else
		{
			return false;
		}
	}
};


#endif

