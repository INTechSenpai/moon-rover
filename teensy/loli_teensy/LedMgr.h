#ifndef _LEDMGR_h
#define _LEDMGR_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
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
					blink_s2.toggle();
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
			//motionControlSystem.getMaxMovingSpeed();
			//directionControler.getRealCurvature();

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
			}
		}

		void on()
		{
			if (!blinkMem)
			{
				digitalWrite(pin, HIGH);
				blinkMem = true;
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

	MotionControlSystem & motionControlSystem;
	DirectionController & directionControler;
};


#endif

