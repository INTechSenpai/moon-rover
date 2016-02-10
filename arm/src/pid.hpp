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


class PID
{
public:


	PID(volatile int32_t* input, volatile int32_t* output, volatile int32_t* setPoint)
	{
		this->output = output;
		this->input = input;
		this->setPoint = setPoint;

		setOutputLimits(-2147483647, 2147483647);
		setTunings(0, 0, 0);
		epsilon = 0;
		pre_error = 0;
		derivative = 0;
		integral = 0;
	}

	void compute() {

		int32_t error = (*setPoint) - (*input);
		derivative = error - pre_error;
		integral += error;
		pre_error = error;

		int32_t result = (int32_t)(
				kp * error + ki * integral + kd * derivative);

		//Saturation
		if (result > outMax) {
			result = outMax;
		} else if (result < outMin) {
			result = outMin;
		}



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

	void setOutputLimits(int32_t min, int32_t max) {
		if (min >= max)
			return;

		outMin = min;
		outMax = max;
	}

	void setEpsilon(int32_t seuil) {
		if(seuil < 0)
			return;
		epsilon = seuil;
	}

	void resetErrors() {
		pre_error = 0;
		integral = 0;
	}
private:

	float kp;
	float ki;
	float kd;

	volatile int32_t* input; //valeur actuelle, réelle
	volatile int32_t* output; //Output : commande
	volatile int32_t* setPoint; //Valeur à atteindre, consigne

	int32_t epsilon;
	int32_t outMin, outMax;

	int32_t pre_error;
	int32_t derivative;
	int32_t integral;
};

#endif
