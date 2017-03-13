#ifndef _CONTROLERNET_h
#define _CONTROLERNET_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "pin_mapping.h"
#include "SynchronousPWM.h"


// ms
#define DELAY_OPEN_NET			1400
#define DELAY_CLOSE_NET			1400
#define DELAY_EJECT_LEFT_SIDE	300
#define DELAY_EJECT_RIGHT_SIDE	300
#define DELAY_REARM_LEFT_SIDE	300
#define DELAY_REARM_RIGHT_SIDE	300

#define PWM_OPEN_NET	255
#define PWM_CLOSE_NET	255
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

	bool openNet(bool launch)
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
			return true;
		}
		return false;
	}

	bool closeNet(bool launch)
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
			return true;
		}
		return false;
	}

	uint8_t ejectLeftSide(bool launch)
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
			if (analogRead(PIN_BUTEE_G) > 512)
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

	uint8_t rearmLeftSide(bool launch)
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
		else if (analogRead(PIN_BUTEE_G) > 512)
		{
			synchronousPWM.stop();
			return SUCCESS;
		}
		return RUNNING;
	}

	uint8_t ejectRightSide(bool launch)
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
			if (analogRead(PIN_BUTEE_D) > 512)
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

	uint8_t rearmRightSide(bool launch)
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
		else if (analogRead(PIN_BUTEE_D) < 512)
		{
			synchronousPWM.stop();
			return SUCCESS;
		}
		return RUNNING;
	}

private:
	SynchronousPWM & synchronousPWM;

	enum Status
	{
		SUCCESS = 0x00,
		FAILURE = 0x01,
		RUNNING = 0xFF
	};

};


#endif

