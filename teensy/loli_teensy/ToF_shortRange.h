#ifndef _TOF_SHORT_RANGE_H
#define _TOF_SHORT_RANGE_H

#if defined(ARDUINO) && ARDUINO >= 100
	#include "Arduino.h"
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
		ptp_offset = 0;
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
		if (distance > 255)
		{
			distance = 255;
		}
		return distance;
	}

	void standby()
	{
		pinMode(pinStandby, OUTPUT);
		digitalWrite(pinStandby, LOW);
		isON = false;
	}

	void powerON(const char* name = "")
	{
		Serial.print("PowerOn ToF ");
		Serial.print(name);
		Serial.print("...");
		pinMode(pinStandby, INPUT);
		delay(50);
		if (vlSensor.init())
		{
			vlSensor.configureDefault();
			vlSensor.setAddress(i2cAddress);

			vlSensor.writeReg(VL6180X::SYSRANGE__MAX_CONVERGENCE_TIME, 10);

			vlSensor.stopContinuous();
			delay(50);
			vlSensor.startRangeContinuous(20);
			isON = true;
			Serial.println("OK");
		}
		else
		{
			Serial.println("FAILED");
			Log::critical(53, "Capteur ToF short range deconnecte");
		}
	}

	void setPtpOffset(uint8_t offset)
	{
		ptp_offset = offset;
	}

private:
	uint8_t i2cAddress, pinStandby;
	uint32_t distance;
	bool isON;
	VL6180X vlSensor;
	uint8_t ptp_offset;
};

#endif

