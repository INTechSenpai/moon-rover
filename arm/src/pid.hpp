/**
 * pid.hpp
 *
 * Classe PID : implémente un régulateur PID (proportionnel intégral dérivé)
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

//		setOutputLimits(-2147483647, 2147483647);
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
		derivative = error - pre_error;
		integral += error;
		pre_error = error;

		float result = kp * error + ki * integral + kd * derivative;
//		sendCoquillage((uint8_t)(error));
//		sendCoquillage((uint8_t)(derivative*256));
		//Saturation
/*		if (result > outMax) {
			result = outMax;
		} else if (result < outMin) {
			result = outMin;
		}
*/


		//Seuillage de la commande
		if (ABS(result) < epsilon)
			result = 0;

		(*output) = result;
	}

	void setTunings(float kp, float ki, float kd) {
		if (kp < 0 || ki < 0 || kd < 0)
			return;

		this->kp = kp;
		this->ki = ki;
		this->kd = kd;
	}
/*
	void setOutputLimits(int32_t min, int32_t max) {
		if (min >= max)
			return;

		outMin = min;
		outMax = max;
	}*/
/*
	void setEpsilon(float seuil) {
		if(seuil < 0)
			return;
		epsilon = seuil;
	}*/

	void resetErrors() {
		pre_error = 0;
		integral = 0;
	}
private:

	float kp;
	float ki;
	float kd;

	volatile float* error; //erreur, c'est-à-dire différence entre la consigne et la valeur actuelle
	volatile float* output; //Output : commande

	float epsilon;
//	int32_t outMin, outMax;

	float pre_error;
	float derivative;
	float integral;
};

#endif
