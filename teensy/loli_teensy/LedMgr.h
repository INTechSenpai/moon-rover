#ifndef _LEDMGR_h
#define _LEDMGR_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "Arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Singleton.h"
#include "pin_mapping.h"
#include "MotionControlSystem.h"

class LedMgr : public Singleton<LedMgr>
{
public:
	LedMgr():
		blink_s1(PIN_DEL_STATUS_1, 300),
		blink_s2(PIN_DEL_STATUS_2, 300),
		blink_cd(PIN_CLIGNOTANT_D, 500),
		blink_cg(PIN_CLIGNOTANT_G, 500),
		blink_ff(PIN_FEUX_FREIN, 100),
		blink_fn(PIN_FEUX_NUIT, 100),
		blink_fr(PIN_FEUX_RECUL, 100),
		motionControlSystem(MotionControlSystem::Instance()),
		directionControler(DirectionController::Instance())
	{
		pinMode(PIN_DEL_STATUS_1, OUTPUT);
		pinMode(PIN_DEL_STATUS_2, OUTPUT);
		pinMode(PIN_FEUX_NUIT, OUTPUT);
		pinMode(PIN_CLIGNOTANT_D, OUTPUT);
		pinMode(PIN_FEUX_FREIN, OUTPUT);
		pinMode(PIN_CLIGNOTANT_G, OUTPUT);
		pinMode(PIN_FEUX_RECUL, OUTPUT);

		digitalWrite(PIN_FEUX_NUIT, HIGH);

		turningSide = NOT_TURNING;
		breaking = false;
		stopped = false;

		statusLed = OFF;
		statusBeforeBatteryLow = OFF;
	}

	void update()
	{
		static uint32_t lastUpdateTime = 0;
		if (millis() - lastUpdateTime > 100)
		{
			lastUpdateTime = millis();

			/* DELs du tableau de bord */
			switch (statusLed)
			{
			case LedMgr::OFF:
				blink_s1.off();
				blink_s2.off();
				break;
			case LedMgr::DOUBLE_BLINK:
				if (blink_s1.blink())
				{
					if (blink_s1.getState())
					{
						blink_s2.off();
					}
					else
					{
						blink_s2.on();
					}
				}
				break;
			case LedMgr::GREEN_BLINK:
				blink_s1.off();
				blink_s2.blink();
				break;
			case LedMgr::RED_BLINK:
				blink_s1.blink();
				blink_s2.off();
				break;
			default:
				break;
			}

			/* DELs des phares */

			float curvature = directionControler.getRealCurvature();
			if (curvature > 0.5 && turningSide != TURNING_LEFT)
			{
				turningSide = TURNING_LEFT;
				blink_cg.on();
				blink_cd.off();
			}
			else if (curvature < -0.5 && turningSide != TURNING_RIGHT)
			{
				turningSide = TURNING_RIGHT;
				blink_cg.off();
				blink_cd.on();
			}
			else if ((curvature >= -0.5 && curvature <= 0.5) &&  turningSide != NOT_TURNING)
			{
				turningSide = NOT_TURNING;
				blink_cg.off();
				blink_cd.off();
			}

			switch (turningSide)
			{
			case LedMgr::TURNING_LEFT:
				blink_cg.blink();
				break;
			case LedMgr::TURNING_RIGHT:
				blink_cd.blink();
				break;
			}

			bool cStopped = motionControlSystem.isStopped();
			if (cStopped && !stopped)
			{
				stopped = true;
				blink_ff.off();
				blink_fr.off();
			}
			else if (!cStopped && stopped)
			{
				stopped = false;
				blink_ff.off();
				blink_fr.off();
			}
			if (!stopped)
			{
				bool cBreaking = motionControlSystem.isBreaking();
				static uint32_t timer;
				if (cBreaking && !breaking)
				{
					breaking = true;
					blink_ff.on();
				}
				else if (!cBreaking && breaking)
				{
					breaking = false;
					timer = millis();
				}
				else if (!breaking && millis() - timer > 300)
				{
					blink_ff.off();
				}
				if (!stopped && motionControlSystem.getMaxMovingSpeed() < 0)
				{
					blink_fr.on();
				}
				else
				{
					blink_fr.off();
				}
			}
		}
	}


	/* Changer l'état des DELs de statut (tableau de bord) */

	void statusLed_doubleBlink()
	{
		if (statusLed != RED_BLINK)
		{
			if (statusLed != DOUBLE_BLINK)
			{
				blink_s1.on();
				blink_s2.off();
			}
			statusLed = DOUBLE_BLINK;
		}
		else
		{
			statusBeforeBatteryLow = DOUBLE_BLINK;
		}
	}

	void statusLed_greenBlink()
	{
		if (statusLed != RED_BLINK)
		{
			statusLed = GREEN_BLINK;
		}
		else
		{
			statusBeforeBatteryLow = GREEN_BLINK;
		}
	}

	void statusLed_lowBattery()
	{
		if (statusLed != RED_BLINK)
		{
			Serial.println("LOW BATTERY");
			statusBeforeBatteryLow = statusLed;
			statusLed = RED_BLINK;
		}
	}

	void statusLed_batteryOk()
	{
		if (statusLed == RED_BLINK)
		{
			Serial.println("BATTERY OK");
			statusLed = statusBeforeBatteryLow;
		}
	}

	void statusLed_off()
	{
		if (statusLed != RED_BLINK)
		{
			statusLed = OFF;
		}
		else
		{
			statusBeforeBatteryLow = OFF;
		}
	}



private:
	enum StatusLed
	{
		OFF,
		DOUBLE_BLINK,
		GREEN_BLINK,
		RED_BLINK
	};

	enum TurningSide
	{
		NOT_TURNING,
		TURNING_LEFT,
		TURNING_RIGHT
	};

	StatusLed statusLed;
	StatusLed statusBeforeBatteryLow;

	class Blink
	{
	public:
		Blink(uint8_t pin, uint32_t period)
		{
			this->pin = pin;
			this->period = period;
			lastBlinkTime = 0;
			blinkMem = false;
		}

		bool blink()
		{
			if (millis() - lastBlinkTime > period)
			{
				lastBlinkTime = millis();
				blinkMem = !blinkMem;
				digitalWrite(pin, blinkMem);
				return true;
			}
			else
			{
				return false;
			}
		}

		void setPeriod(uint32_t period)
		{
			this->period = period;
		}

		void off()
		{
			if (blinkMem)
			{
				digitalWrite(pin, LOW);
				blinkMem = false;
				lastBlinkTime = millis();
			}
		}

		void on()
		{
			if (!blinkMem)
			{
				digitalWrite(pin, HIGH);
				blinkMem = true;
				lastBlinkTime = millis();
			}
		}

		void toggle()
		{
			blinkMem = !blinkMem;
			digitalWrite(pin, blinkMem);
			lastBlinkTime = millis();
		}

		bool getState() const
		{
			return blinkMem;
		}

	private:
		uint8_t pin;
		bool blinkMem;
		uint32_t period;
		uint32_t lastBlinkTime;
	};

	Blink blink_s1; // status1
	Blink blink_s2; // status2
	Blink blink_fn; // feux nuit
	Blink blink_cd; // clignotantGauche
	Blink blink_cg; // clignotantDroit
	Blink blink_ff; // feux frein
	Blink blink_fr; // feux recul

	TurningSide turningSide;
	bool breaking;
	bool stopped;

	MotionControlSystem & motionControlSystem;
	DirectionController & directionControler;
};


#endif

