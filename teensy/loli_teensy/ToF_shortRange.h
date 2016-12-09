#ifndef _TOF_SHORT_RANGE_H
#define _TOF_SHORT_RANGE_H

#if defined(ARDUINO) && ARDUINO >= 100
#include "arduino.h"
#else
#include "WProgram.h"
#endif

#include "VL6180X.h"

class ToF_shortRange
{
public:
	ToF_shortRange(uint8_t id, uint8_t pinStandby)
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
			distance = vlSensor.readRangeContinuous();
			if (vlSensor.timeoutOccurred())
			{
				distance = 0;
			}
		}
		else
		{
			distance = 0;
		}

		return distance;
	}

	void standby()
	{
		pinMode(pinStandby, OUTPUT);
		digitalWrite(pinStandby, LOW);
		isON = false;
	}

	void powerON()
	{
		pinMode(pinStandby, INPUT);
		vlSensor.init();
		vlSensor.configureDefault();
		vlSensor.setAddress(i2cAddress);

		vlSensor.writeReg(VL6180X::SYSRANGE__MAX_CONVERGENCE_TIME, 10);

		vlSensor.stopContinuous();
		delay(50);
		vlSensor.startRangeContinuous(20);
		isON = true;
	}

private:
	uint8_t i2cAddress, pinStandby;
	uint32_t distance;
	bool isON;
	VL6180X vlSensor;
};

#endif

