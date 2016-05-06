/**
 * pid.hpp
 *
 * Classe PID : impl�mente un r�gulateur PID (proportionnel int�gral d�riv�)
 *
 * Auteur : Paul BERNIER - bernier.pja@gmail.com
 */

#ifndef PID_HPP
#define PID_HPP

#include <stdint.h>
#include "utils.h"
#include "serie.h"

class PID
{
public:


	PID(volatile float* error, volatile float* output, float epsilon)
	{
		this->output = output;
		this->error = error;

		setTunings(0, 0, 0);
		this->epsilon = epsilon;
		pre_error = 0;
		derivative = 0;
		integral = 0;
	}

	float getDerivativeError()
	{
		return derivative;
	}

	void compute() {

		float error = *(this->error);

		// Seuillage de l'erreur. Particuli�rement important si Ki n'est pas nul
/*		if(ABS(error) < epsilon)
		{
			pre_error = error;
			(*output) = 0;
		}
		else*/
		{
			derivative = error - pre_error;
			integral += error;
			pre_error = error;

			float result = kp * error + ki * integral + kd * derivative;
			(*output) = result;
		}
	}

	void setTunings(float kp, float ki, float kd)
	{
		if (kp < 0 || ki < 0 || kd < 0)
			return;

		this->kp = kp;
		this->ki = ki;
		this->kd = kd;
	}

	void resetErrors()
	{
		pre_error = 0;
		integral = 0;
	}

private:

	volatile float kp;
	volatile float ki;
	volatile float kd;

	volatile float* error; //erreur, c'est-�-dire diff�rence entre la consigne et la valeur actuelle
	volatile float* output; //Output : commande

	float epsilon;

	float pre_error;
	float derivative;
	float integral;
};

#endif
