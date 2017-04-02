#ifndef _STARTUPMGR_h
#define _STARTUPMGR_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Singleton.h"
#include "pin_mapping.h"
#include "LedMgr.h"

class StartupMgr : public Singleton<StartupMgr>
{
public:
	StartupMgr():
		ledMgr(LedMgr::Instance())
	{
		state = WAIT_FOR_COLOR_SETUP;
		side = UNKNOWN;
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

			//Serial.println(analogRead(PIN_GET_JUMPER));
			if (state == WAIT_FOR_COLOR_SETUP)
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
					}
					else
					{
						side = YELLOW;
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

private:
	enum State
	{
		WAIT_FOR_COLOR_SETUP,
		SIGNAL_SETUP_END,
		READY
	};

	Side side;
	State state;

	uint32_t setupEnd_startTime; //ms

	LedMgr & ledMgr;
};

#endif

