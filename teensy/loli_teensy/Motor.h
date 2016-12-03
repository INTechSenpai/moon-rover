#ifndef _MOTOR_h
#define _MOTOR_h

#if defined(ARDUINO) && ARDUINO >= 100
#include "arduino.h"
#else
#include "WProgram.h"
#endif

#include "pin_mapping.h"

class Motor
{
public:
	Motor()
	{
		pinMode(PIN_PWM_LEFT_MOTOR, OUTPUT);
		pinMode(PIN_PWM_RIGHT_MOTOR, OUTPUT);
		pinMode(PIN_DIRECTION_LEFT_MOTOR, OUTPUT);
		pinMode(PIN_DIRECTION_RIGHT_MOTOR, OUTPUT);

		// La résolution des PWM est 10bits (0-1023)
		analogWriteResolution(10);

		// Réglage de la fréquence des PWM
		analogWriteFrequency(PIN_PWM_LEFT_MOTOR, 35156.25);
		analogWriteFrequency(PIN_PWM_RIGHT_MOTOR, 35156.25);

		// Initialisation : Moteurs arrêtés
		analogWrite(PIN_PWM_LEFT_MOTOR, 0);
		analogWrite(PIN_PWM_RIGHT_MOTOR, 0);
		digitalWrite(PIN_DIRECTION_LEFT_MOTOR, LOW);
		digitalWrite(PIN_DIRECTION_RIGHT_MOTOR, LOW);
	}

	void runLeft(int16_t pwm)
	{
		// TODO : régler le sens de rotation des moteurs
		if (pwm >= 0)
		{
			digitalWrite(PIN_DIRECTION_LEFT_MOTOR, HIGH);
		}
		else
		{
			digitalWrite(PIN_DIRECTION_LEFT_MOTOR, LOW);
			pwm = -pwm;
		}
		if (pwm > 1023)
			pwm = 1023;
		analogWrite(PIN_PWM_LEFT_MOTOR, pwm);
	}

	void runRight(int16_t pwm)
	{
		// TODO : régler le sens de rotation des moteurs
		if (pwm >= 0)
		{
			digitalWrite(PIN_DIRECTION_RIGHT_MOTOR, LOW);
		}
		else
		{
			digitalWrite(PIN_DIRECTION_RIGHT_MOTOR, HIGH);
			pwm = -pwm;
		}
		if (pwm > 1023)
			pwm = 1023;
		analogWrite(PIN_PWM_RIGHT_MOTOR, pwm);
	}
};

#endif