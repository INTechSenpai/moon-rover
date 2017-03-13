#ifndef _INFRAREDSENSOR_h
#define _INFRAREDSENSOR_h

#include "Wire\Wire.h"

class InfraredSensor
{
public:
	InfraredSensor(uint8_t address)
	{
		this->address = address >> 1;
		distance = 0;
		shift = 0x02;
		distanceRegister = 0x5E;
		initialized = false;
	}

	void init()
	{
		Serial.println("IR Init");
		// Lecture du "SHIFT bit" du capteur
		Wire.beginTransmission(address);
		Wire.write(0x35);	// L'adresse du SHIFT bit est 0x35
		Wire.endTransmission();

		Wire.requestFrom(address, (uint8_t)1);
		while (Wire.available() == 0) { ; }
		shift = Wire.read();
		initialized = true;
	}

	uint32_t getMesure()
	{
		if (initialized)
		{
			Wire.beginTransmission(address);
			Wire.write(distanceRegister);
			Wire.endTransmission();

			Wire.requestFrom(address, (uint8_t)2);

			while (Wire.available() < 2) { ; }

			/* Lecture de la distance mesurée par le capteur */
			high = Wire.read();
			low = Wire.read();

			/* Conversion en cm */
			distance = high << 4;
			distance += low;
			distance = distance >> (4 + shift);

			return distance;
		}
		else
		{
			return 0;
		}
	}

private:
	uint8_t address, distanceRegister;
	int distance;
	int low, high, shift;
	bool initialized;
};

#endif

