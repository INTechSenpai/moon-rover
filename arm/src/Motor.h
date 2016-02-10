/**
 * Motor.h
 *
 */

#ifndef __MOTOR_h__
#define __MOTOR_h__

#include "stm32f4xx.h"
#include "utils.h"
#include "stm32f4xx_hal_gpio.h"
#include "stm32f4xx_hal_tim.h"
#include "stm32f4xx_hal_rcc.h"

enum Direction {
	MOTOR_BACKWARD, MOTOR_FORWARD
};

enum Side {
	MOTOR_LEFT, MOTOR_RIGHT
};

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
