#ifndef _BATTERYSENSOR_h
#define _BATTERYSENSOR_h

#include "pin_mapping.h"
#include "Singleton.h"

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

class BatterySensor : public Singleton<BatterySensor>
{
public:
	BatterySensor()
	{
		pinMode(PIN_GET_VOLTAGE, INPUT);
		updatePeriod = 1000; //ms
		lastUpdateTime = 0;
	}

	void update()
	{
		if (millis() - lastUpdateTime > updatePeriod)
		{
			float voltage = (float)analogRead(PIN_GET_VOLTAGE) / 60 - 0.2;

			//Serial.println(analogRead(PIN_GET_VOLTAGE));
			//Serial.println(voltage);
			
			float level = (voltage - 10.7) * 50;

			//Serial.println(level);
			// 10,7V <-> 0%
			// 11,1V <-> 20%
			// 12,7V <-> 100%
			if (level < 0)
			{
				level = 0;
			}
			else if (level > 100)
			{
				level = 100;
			}
			currentLevel = (uint8_t)level;

			lastUpdateTime = millis();

			//Hack
			if (currentLevel < 30)
			{
				digitalWrite(PIN_DEL_STATUS_1, HIGH);
			}
			else
			{
				digitalWrite(PIN_DEL_STATUS_1, LOW);
			}

		}
	}

	uint8_t getLevel()
	{
		return currentLevel;
	}

private:
	uint32_t lastUpdateTime; // ms
	uint8_t currentLevel; // pourcentage de batterie restant (de 0 à 100%)
	uint32_t updatePeriod; // ms
};


#endif

