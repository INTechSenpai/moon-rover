
#ifndef __COUNTER_H__
#define __COUNTER_H__

#include "stm32f4xx.h"
#include "stm32f4xx_tim.h"
#include "stm32f4xx_gpio.h"
#include "stm32f4xx_rcc.h"

class Counter {

public:
	Counter();
	static int32_t getLeftValue();
	static int32_t getRightValue();

};

#endif
