#ifndef _CONTROLERNET_h
#define _CONTROLERNET_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "Arduino.h"
#else
	#include "WProgram.h"
#endif

#include "pin_mapping.h"
#include "SynchronousPWM.h"
#include "ActuatorStatus.h"


// ms
#define DELAY_OPEN_NET			1400
#define DELAY_CLOSE_NET			1400
#define DELAY_CLOSE_NET_FORCE	2000
#define DELAY_LOCK_NET			200
#define DELAY_EJECT_LEFT_SIDE	300
#define DELAY_EJECT_RIGHT_SIDE	300
#define DELAY_REARM_LEFT_SIDE	1000
#define DELAY_REARM_RIGHT_SIDE	1000

#define PWM_OPEN_NET	255
#define PWM_CLOSE_NET	255
#define PWM_LOCK_NET	150
#define PWM_EJECT_LEFT	255
#define PWM_EJECT_RIGHT	255
#define PWM_REARM_LEFT	255
#define PWM_REARM_RIGHT	255


/*
	Chaque méthode est à exécuter en boucle, la permière fois avec l'argument 'launch' à true.
	Elle renvoie 'true' si l'action est ternimée, 'false' sinon.
	Si la méthode peut donner un retour sur la réussite de l'action, elle renvoie un uint8_t prenant les valeurs:
	SUCCESS:0x00	FAILURE:0x01	RUNNING:0xFF
*/
class ControlerNet
{
public:
	ControlerNet():
		synchronousPWM(SynchronousPWM::Instance())
	{
		pinMode(PIN_BUTEE_G, INPUT_PULLUP);
		pinMode(PIN_BUTEE_D, INPUT_PULLUP);
	}
	
	void stop()
	{
		synchronousPWM.stop();
	}

	ActuatorStatus openNet(bool launch)
	{
		static uint32_t beginTime;
		if (launch)
		{
			synchronousPWM.net(PWM_OPEN_NET, true);
			beginTime = millis();
		}
		else if (millis() - beginTime > DELAY_OPEN_NET)
		{
			synchronousPWM.stop();
			return SUCCESS;
		}
		return RUNNING;
	}

	ActuatorStatus closeNet(bool launch)
	{
		static uint32_t beginTime;
		if (launch)
		{
			synchronousPWM.net(PWM_CLOSE_NET, false);
			beginTime = millis();
		}
		else if (millis() - beginTime > DELAY_CLOSE_NET)
		{
			synchronousPWM.stop();
			return SUCCESS;
		}
		return RUNNING;
	}

	ActuatorStatus closeNetForce(bool launch)
	{
		static uint32_t beginTime;
		if (launch)
		{
			synchronousPWM.net(PWM_CLOSE_NET, false);
			beginTime = millis();
		}
		else if (millis() - beginTime > DELAY_CLOSE_NET_FORCE)
		{
			synchronousPWM.stop();
			return SUCCESS;
		}
		return RUNNING;
	}

	ActuatorStatus lockNet(bool launch)
	{
		static uint32_t beginTime;
		if (launch)
		{
			synchronousPWM.net(PWM_LOCK_NET, false);
			beginTime = millis();
		}
		else if (millis() - beginTime > DELAY_LOCK_NET)
		{
			synchronousPWM.stop();
			return SUCCESS;
		}
		return RUNNING;
	}

	ActuatorStatus ejectLeftSide(bool launch)
	{
		static uint32_t beginTime;
		if (launch)
		{
			synchronousPWM.leftEject(PWM_EJECT_LEFT, true);
			beginTime = millis();
		}
		else if (millis() - beginTime > DELAY_EJECT_LEFT_SIDE)
		{
			synchronousPWM.stop();
			if (isLeftLocked())
			{
				return FAILURE;
			}
			else
			{
				return SUCCESS;
			}
		}
		return RUNNING;
	}

	ActuatorStatus rearmLeftSide(bool launch)
	{
		static uint32_t beginTime;
		if (launch)
		{
			synchronousPWM.leftEject(PWM_REARM_LEFT, false);
			beginTime = millis();
		}
		else if (millis() - beginTime > DELAY_REARM_LEFT_SIDE)
		{
			synchronousPWM.stop();
			return FAILURE;
		}
		else if (isLeftLocked())
		{
			synchronousPWM.stop();
			return SUCCESS;
		}
		return RUNNING;
	}

	ActuatorStatus ejectRightSide(bool launch)
	{
		static uint32_t beginTime;
		if (launch)
		{
			synchronousPWM.rightEject(PWM_EJECT_RIGHT, true);
			beginTime = millis();
		}
		else if (millis() - beginTime > DELAY_EJECT_RIGHT_SIDE)
		{
			synchronousPWM.stop();
			if (isRightLocked())
			{
				return FAILURE;
			}
			else
			{
				return SUCCESS;
			}
		}
		return RUNNING;
	}

	ActuatorStatus rearmRightSide(bool launch)
	{
		static uint32_t beginTime;
		if (launch)
		{
			synchronousPWM.rightEject(PWM_REARM_RIGHT, false);
			beginTime = millis();
		}
		else if (millis() - beginTime > DELAY_REARM_RIGHT_SIDE)
		{
			synchronousPWM.stop();
			return FAILURE;
		}
		else if (isRightLocked())
		{
			synchronousPWM.stop();
			return SUCCESS;
		}
		return RUNNING;
	}

private:
	SynchronousPWM & synchronousPWM;

	bool isLeftLocked()
	{
		return analogRead(PIN_BUTEE_G) > 500;
	}

	bool isRightLocked()
	{
		return analogRead(PIN_BUTEE_D) > 500;
	}

};


#endif

