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
#include "safe_enum.hpp"
#include "utils.h"

struct pid_direction_def {
	enum type {
		DIRECT, REVERSE
	};
};

typedef safe_enum<pid_direction_def> PidDirection;

class PID {
public:

	PID(volatile int32_t* input, volatile int16_t* output, volatile int32_t* setPoint) :
			controllerDirection(PidDirection::DIRECT), epsilon(0), pre_error(
					0), integral(0) {

		this->output = output;
		this->input = input;
		this->setPoint = setPoint;

		setOutputLimits(-32678, 32767);
		setTunings(0, 0, 0);
		resetErrors();
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

		if (controllerDirection == PidDirection::DIRECT) {
			this->kp = kp;
			this->ki = ki;
			this->kd = kd;
		} else {
			this->kp = (0 - kp);
			this->ki = (0 - ki);
			this->kd = (0 - kd);
		}
	}

	void setOutputLimits(int16_t min, int16_t max) {
		if (min >= max)
			return;

		outMin = min;
		outMax = max;

		if ((*output) > outMax)
			(*output) = outMax;
		else if ((*output) < outMin)
			(*output) = outMin;
	}

	int16_t getOutputLimit()
	{
		return outMax;
	}

	void setControllerDirection(PidDirection dir) {
		if (dir == PidDirection::REVERSE) {
			kp = (0 - kp);
			ki = (0 - ki);
			kd = (0 - kd);
		}
		controllerDirection = dir;
	}

	void resetErrors() {
		pre_error = 0;
		integral = 0;
	}
	float getKp() const {
		return kp;
	}
	float getKi() const {
		return ki;
	}
	float getKd() const {
		return kd;
	}
	PidDirection getDirection() const {
		return controllerDirection;
	}

	int32_t getError() const {
		return pre_error;
	}

	int32_t getDerivativeError() const {
		return derivative;
	}

	int32_t getIntegralErrol() const {
		return integral;
	}

private:

	float kp;
	float ki;
	float kd;

	PidDirection controllerDirection;

	volatile int32_t* input; //Valeur du codeur
	volatile int16_t* output; //Output : pwm
	volatile int32_t* setPoint; //Valeur à atteindre

	uint8_t epsilon;
	int16_t outMin, outMax;

	int32_t pre_error;
	int32_t derivative;
	int32_t integral;
};

#endif
