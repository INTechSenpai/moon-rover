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
		jumperInside = false;
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
				if (jumperInside)
				{// Jumper inséré
					state = READY;
					if (analogRead(PIN_GET_COLOR) > 500)
					{
						Serial.println("Color: BLUE");
						side = BLUE;
						motionControlSystem.setPosition(Position(X_BLEU, Y_BLEU, O_BLEU));
					}
					else
					{
						Serial.println("Color: YELLOW");
						side = YELLOW;
						motionControlSystem.setPosition(Position(X_JAUNE, Y_JAUNE, O_JAUNE));
					}
				}
			}
			else
			{
				ledMgr.statusLed_greenBlink();
			}
		}
	}

	Side getSide()
	{
		return side;
	}

	void jumperPluggedIn()
	{
		jumperInside = true;
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
		READY
	};

	Side side;
	State state;

	bool hlAlive;
	bool jumperInside;

	uint32_t setupEnd_startTime; //ms

	LedMgr & ledMgr;
	MotionControlSystem & motionControlSystem;
};

#endif

