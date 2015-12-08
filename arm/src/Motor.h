/**
 * Motor.h
 *
 */

#ifndef __MOTOR_h__
#define __MOTOR_h__

#include "stm32f4xx.h"
#include "safe_enum.hpp"
#include "utils.h"
#include "stm32f4xx_hal_gpio.h"
#include "stm32f4xx_hal_tim.h"
#include "stm32f4xx_hal_rcc.h"

struct direction_def {
	enum type {
		BACKWARD, FORWARD
	};
};

struct side_def {
	enum type {
		LEFT, RIGHT
	};
};

typedef safe_enum<direction_def> Direction;
typedef safe_enum<side_def> Side;

class Motor {
private:
	Side side;
	void setDirection(Direction);

public:
	Motor(Side);
	static void initPWM();
	void run(int16_t);
};

#endif
