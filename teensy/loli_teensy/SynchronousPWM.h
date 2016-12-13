/*
	Permet de contrôler les ponts en H du filet (3 moteurs) avec seulement 4 GPIO (et le cablage approprié)
	Les mouvements possibles sont :
		- Arrêt de tous les moteurs
		- Actionner le filet (ouverture ou fermeture)
		- Actionner l'éjecteur de balles gauche (éjecter ou ranger)
		- Actionner l'éjecteur de balles droit (éjecter ou ranger)
	Les mouvements ne peuvent pas être combinés, un appel à un mouvement entraîne l'arrêt de l'autre mouvement en cours.
	Chaque mouvement peut être effectué avec un PWM codé sur (8-log(PWM_RESOLUTION_PRESCALER)) bits
*/

#ifndef _SYNCHRONOUSPWM_h
#define _SYNCHRONOUSPWM_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Singleton.h"
#include "pin_mapping.h"

#define PWM_RESOLUTION_PRESCALER	4

class SynchronousPWM : public Singleton<SynchronousPWM>
{
public:
	SynchronousPWM()
	{
		pin_motor_net = LOW;
		pin_left_motor = LOW;
		pin_right_motor = LOW;
		pin_com = LOW;
		pwm = 0;
		counter = 0;
		pinMode(PIN_MOTOR_NET, OUTPUT);
		digitalWrite(PIN_MOTOR_NET, LOW);
		pinMode(PIN_RIGHT_MOTOR_NET, OUTPUT);
		digitalWrite(PIN_RIGHT_MOTOR_NET, LOW);
		pinMode(PIN_LEFT_MOTOR_NET, OUTPUT);
		digitalWrite(PIN_LEFT_MOTOR_NET, LOW);
		pinMode(PIN_NET_COM, OUTPUT);
		digitalWrite(PIN_NET_COM, LOW);
	}

	void inline update()
	{
		if (counter < pwm)
		{// pulse ON
			digitalWriteFast(PIN_MOTOR_NET, pin_motor_net);
			digitalWriteFast(PIN_LEFT_MOTOR_NET, pin_left_motor);
			digitalWriteFast(PIN_RIGHT_MOTOR_NET, pin_right_motor);
			digitalWriteFast(PIN_NET_COM, pin_com);
		}
		else
		{// pulse OFF
			digitalWriteFast(PIN_MOTOR_NET, LOW);
			digitalWriteFast(PIN_LEFT_MOTOR_NET, LOW);
			digitalWriteFast(PIN_RIGHT_MOTOR_NET, LOW);
			digitalWriteFast(PIN_NET_COM, LOW);
		}
		counter += PWM_RESOLUTION_PRESCALER;
	}

	void stop()
	{
		noInterrupts();
		pin_motor_net = LOW;
		pin_left_motor = LOW;
		pin_right_motor = LOW;
		pin_com = LOW;
		pwm = 0;
		interrupts();
	}

	// TODO vérifier le sens de rotation de chaque moteur
	void net(uint8_t _pwm, bool open)
	{
		noInterrupts();
		if (open)
		{
			pin_motor_net = HIGH;
			pin_left_motor = LOW;
			pin_right_motor = LOW;
			pin_com = LOW;
		}
		else
		{
			pin_motor_net = LOW;
			pin_left_motor = HIGH;
			pin_right_motor = HIGH;
			pin_com = HIGH;
		}
		pwm = _pwm;
		interrupts();
	}

	void leftEject(uint8_t _pwm, bool eject)
	{
		noInterrupts();
		if (eject)
		{
			pin_motor_net = LOW;
			pin_left_motor = HIGH;
			pin_right_motor = LOW;
			pin_com = LOW;
		}
		else
		{
			pin_motor_net = HIGH;
			pin_left_motor = LOW;
			pin_right_motor = HIGH;
			pin_com = HIGH;
		}
		pwm = _pwm;
		interrupts();
	}

	void rightEject(uint8_t _pwm, bool eject)
	{
		noInterrupts();
		if (eject)
		{
			pin_motor_net = LOW;
			pin_left_motor = LOW;
			pin_right_motor = HIGH;
			pin_com = LOW;
		}
		else
		{
			pin_motor_net = HIGH;
			pin_left_motor = HIGH;
			pin_right_motor = LOW;
			pin_com = HIGH;
		}
		pwm = _pwm;
		interrupts();
	}

private:
	uint8_t pin_motor_net;
	uint8_t pin_right_motor;
	uint8_t pin_left_motor;
	uint8_t pin_com;
	uint8_t pwm;
	volatile uint8_t counter;
};

#endif

