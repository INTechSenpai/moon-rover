#ifndef _PID_h
#define _PID_h

#if defined(ARDUINO) && ARDUINO >= 100
#include "arduino.h"
#else
#include "WProgram.h"
#endif

#include <Printable.h>

class PID : public Printable
{
public:

	PID(volatile int32_t* input, volatile int32_t* output, volatile int32_t* setPoint)
	{
		this->output = output;
		this->input = input;
		this->setPoint = setPoint;

		setOutputLimits(INT32_MIN, INT32_MAX);
		setTunings(0, 0, 0);
		pre_error = 0;
		derivative = 0;
		integral = 0;
		resetErrors();
	}

	void compute() {

		int32_t error = (*setPoint) - (*input);
		derivative = error - pre_error;
		integral += error;
		pre_error = error;

		int32_t result = (int32_t)(
			kp * error + ki * integral + kd * derivative);

		if (result > outMax)
			result = outMax;
		else if (result < outMin)
			result = outMin;

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

		if ((*output) > outMax)
			(*output) = outMax;
		else if ((*output) < outMin)
			(*output) = outMin;
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

	int32_t getError() const {
		return pre_error;
	}

	int32_t getDerivativeError() const {
		return derivative;
	}

	int32_t getIntegralErrol() const {
		return integral;
	}

	size_t printTo(Print& p) const
	{
		return p.printf("%d_%d_%d", *setPoint, *input, *output);
	}

private:

	float kp;
	float ki;
	float kd;

	volatile int32_t* input; //Valeur du codeur
	volatile int32_t* output; //Output : pwm
	volatile int32_t* setPoint; //Valeur à atteindre

	int32_t outMin, outMax;

	int32_t pre_error;
	int32_t derivative;
	int32_t integral;
};


#endif