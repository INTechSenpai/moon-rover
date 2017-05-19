#ifndef _STARTUPMGR_h
#define _STARTUPMGR_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "Arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Singleton.h"
#include "pin_mapping.h"
#include "LedMgr.h"
#include "MotionControlSystem.h"
#include "Position.h"

class StartupMgr : public Singleton<StartupMgr>
{
public:
	StartupMgr():
		ledMgr(LedMgr::Instance()),
		motionControlSystem(MotionControlSystem::Instance())
	{
		state = WAIT_FOR_HL;
		side = UNKNOWN;
		hlAlive = false;
	}

	enum Side
	{
		BLUE = 0x00,
		YELLOW = 0x01,
		UNKNOWN = 0x02
	};

	void update()
	{
		static uint32_t lastUpdateTime = 0;
		if (millis() - lastUpdateTime > 20)
		{
			lastUpdateTime = millis();
			if (state == WAIT_FOR_HL)
			{
				ledMgr.statusLed_greenIdle();
				if (hlAlive)
				{
					state = WAIT_FOR_COLOR_SETUP;
				}
			}
			else if (state == WAIT_FOR_COLOR_SETUP)
			{
				ledMgr.statusLed_doubleBlink();
				if (analogRead(PIN_GET_JUMPER) > 750)
				{// Jumper inséré
					state = SIGNAL_SETUP_END;
					setupEnd_startTime = millis();
				}
			}
			else if (state == SIGNAL_SETUP_END)
			{
				ledMgr.statusLed_greenBlink();
				if (analogRead(PIN_GET_JUMPER) < 750)
				{// Jumper retiré
					state = WAIT_FOR_COLOR_SETUP;
				}
				else if (millis() - setupEnd_startTime > 2000)
				{
					state = READY;
					if (analogRead(PIN_GET_COLOR) > 750)
					{
						side = BLUE;
						motionControlSystem.setPosition(Position(X_BLEU, Y_BLEU, O_BLEU));
					}
					else
					{
						side = YELLOW;
						motionControlSystem.setPosition(Position(X_JAUNE, Y_JAUNE, O_JAUNE));
					}
				}
			}
			else
			{
				ledMgr.statusLed_off();
			}
		}
	}

	Side getSide()
	{
		return side;
	}

	bool isReady()
	{
		return state == READY;
	}

	void hlIsAlive()
	{
		hlAlive = true;
	}

private:
	enum State
	{
		WAIT_FOR_HL,
		WAIT_FOR_COLOR_SETUP,
		SIGNAL_SETUP_END,
		READY
	};

	Side side;
	State state;

	bool hlAlive;

	uint32_t setupEnd_startTime; //ms

	LedMgr & ledMgr;
	MotionControlSystem & motionControlSystem;
};

#endif

