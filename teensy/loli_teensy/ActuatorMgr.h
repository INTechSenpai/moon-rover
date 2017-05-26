#ifndef _ACTUATORMGR_h
#define _ACTUATORMGR_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "Arduino.h"
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
#define	ANGLE_DOWN		60
#define ANGLE_HALFWAY	80
#define ANGLE_UP		153
#define ANGLE_RELEASE	140

/* Durée maximale des mouvements de l'AX12, en ms */
#define AX12_NET_TIMEOUT	2500

/* Vitesses des mouvements de l'AX12, valeur comprise entre 1 et 1023, 0 correspond à une vitesse non régulée égale au maximum possible */
#define SLOW_SPEED	300		// Utilisée pour les mouvements vers le bas

#define TOLERANCE_ANGLE	5

#define FAN_STARTUP_DURATION	2000 // ms
#define FUNNY_ACTION_DURATION	5000 // ms

#define CROSS_FLIP_FLOP_SEUIL_1 600
#define CROSS_FLIP_FLOP_SEUIL_2 720
#define CROSS_FLIP_FLOP_SEUIL_3 820

#define CROSS_FLIP_FLOP_ANGLE_1 65
#define CROSS_FLIP_FLOP_ANGLE_2 70



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
			Serial.println("AX12 Timeout");
			return FAILURE;
		}
		return moveTheAX12(launch, ANGLE_DOWN, TOLERANCE_ANGLE, lastCommTime, SLOW_SPEED, 500);
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
		if (launch)
		{
			ax12net.alarmShutdown(0);
			ax12net.enableTorque();
			ax12net.alarmShutdown();
		}
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

	ActuatorStatus closeNetForce(bool launch)
	{
		return controlerNet.closeNetForce(launch);
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

	ActuatorStatus funnyAction(bool launch)
	{
		static uint32_t startTime;
		static uint32_t fanStartTime;
		static uint32_t lastCommTime;
		static uint8_t stage;
		if (launch)
		{
			stage = 0;
			moveTheAX12(true, ANGLE_UP, TOLERANCE_ANGLE, lastCommTime);
			startTime = millis();
			return RUNNING;
		}
		else
		{
			if (stage == 0)
			{
				if (moveTheAX12(false, ANGLE_UP, TOLERANCE_ANGLE, lastCommTime) != RUNNING)
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
				return SUCCESS;
			}
			else
			{
				return RUNNING;
			}
		}
	}

	/* Ne pas spammer les appels à cette méthode */
	ActuatorStatus crossFlipFlop(bool launch, float posX = 0)
	{
		static uint32_t lastCommTime;
		static uint8_t stage = 0;
		/*
			0 : avant la bascule 
			1 : filet abaissé au max (sur la bascule)
			2 : filet un peu relevé
			3 : remonte le filet completement
		*/

		if (launch)
		{
			stage = 0;
			return RUNNING;
		}
		else
		{
			switch (stage)
			{
			case 0:
				if (ABS(posX) > CROSS_FLIP_FLOP_SEUIL_1)
				{
					if (ax12net.speed(SLOW_SPEED) == DYN_STATUS_OK && ax12net.goalPositionDegree(CROSS_FLIP_FLOP_ANGLE_1) == DYN_STATUS_OK)
					{
						stage = 1;
					}
				}
				break;
			case 1:
				if (ABS(posX) > CROSS_FLIP_FLOP_SEUIL_2)
				{
					if (ax12net.speed(0) == DYN_STATUS_OK && ax12net.goalPositionDegree(CROSS_FLIP_FLOP_ANGLE_2) == DYN_STATUS_OK)
					{
						stage = 2;
					}
				}
				break;
			case 2:
				if (ABS(posX) > CROSS_FLIP_FLOP_SEUIL_3)
				{
					stage = 3;
					return moveTheAX12(true, ANGLE_UP, TOLERANCE_ANGLE, lastCommTime);
				}
				break;
			case 3:
				return moveTheAX12(false, ANGLE_UP, TOLERANCE_ANGLE, lastCommTime);
				break;
			default:
				break;
			}
			return RUNNING;
		}
	}

	void setAngleNet(uint8_t angle)
	{
		ax12net.goalPositionDegree(angle);
	}

private:
	ControlerNet controlerNet;
	InterfaceAX12 ax12interface;
	DynamixelMotor ax12net;

	ActuatorStatus moveTheAX12(bool launch, uint16_t goalPosition, uint16_t tolerance, uint32_t & lastCommunicationTime, uint16_t speed = 0, uint32_t postMoveDelay = 0)
	{
		static bool orderFailed = false;
		static bool moveCompleted = false;
		static uint32_t endMoveTime;
		if (launch)
		{
			orderFailed = false;
			moveCompleted = false;
			if (ax12net.speed(speed) != DYN_STATUS_OK)
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
		else if (moveCompleted)
		{
			if (millis() - endMoveTime > postMoveDelay)
			{
				return SUCCESS;
			}
			else
			{
				return RUNNING;
			}
		}
		else if (millis() - lastCommunicationTime > 100)
		{
			lastCommunicationTime = millis();
			int32_t currentPosition = ax12net.currentPositionDegree();
			if (ABS(currentPosition - (int32_t)goalPosition) <= (int32_t)tolerance)
			{
				moveCompleted = true;
				endMoveTime = millis();
			}
			return RUNNING;	
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

