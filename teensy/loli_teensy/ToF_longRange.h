#ifndef _TOF_LONGRANGE_h
#define _TOF_LONGRANGE_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "VL53L0X.h"

class ToF_longRange
{
public:
	ToF_longRange(uint8_t id, uint8_t pinStandby)
	{
		i2cAddress = id;
		this->pinStandby = pinStandby;
		standby();
		vlSensor.setTimeout(500);
	}

	uint32_t getMesure()
	{
		if (isON)
		{
			distance = vlSensor.readRangeContinuousMillimeters();
			if (vlSensor.timeoutOccurred())
			{
				distance = 0;
			}
		}
		else
		{
			distance = 0;
		}

		return distance / 10; /* Conversion en cm */
	}

	void standby()
	{
		pinMode(pinStandby, OUTPUT);
		digitalWrite(pinStandby, LOW);
		isON = false;
	}

	void powerON()
	{
		Serial.println("PowerOn ToF long");
		pinMode(pinStandby, INPUT);
		delay(50);
		vlSensor.init();
		vlSensor.setAddress(i2cAddress);

		vlSensor.stopContinuous();
		delay(50);
		vlSensor.startContinuous();
		isON = true;
	}

private:
	uint8_t i2cAddress, pinStandby;
	uint32_t distance;
	bool isON;
	VL53L0X vlSensor;
};



#endif

