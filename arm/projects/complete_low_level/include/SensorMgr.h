#ifndef SENSOR_MGR_H
#define SENSOR_MGR_H

#include "stm32f4xx.h"
#include "stm32f4xx_tim.h"
#include "stm32f4xx_gpio.h"
#include "stm32f4xx_rcc.h"
#include "stm32f4xx_exti.h"
#include "stm32f4xx_syscfg.h"
#include "misc.h"
#include "capteur_srf05.hpp"
#include "Singleton.hpp"
#include <Uart.hpp>

extern Uart<1> serial;

class SensorMgr : public Singleton<SensorMgr>
{
public:
	SensorMgr();

	int getLeftFrontValue() const;
	int getRightFrontValue() const;
	int getLeftBackValue() const;
	int getRightBackValue() const;
	bool isPlotInside() const;
	bool isLeftGlassInside() const;
	bool isRightGlassInside() const;
	bool isJumperOut() const;

	void refresh();

	void leftFrontUSInterrupt();
	void rightFrontUSInterrupt();
	void leftBackUSInterrupt();
	void rightBackUSInterrupt();

private:
	CapteurSRF leftFrontUS;
	CapteurSRF rightFrontUS;
	CapteurSRF leftBackUS;
	CapteurSRF rightBackUS;

	unsigned int refreshDelay;
	unsigned int currentTime;
	unsigned int lastRefreshTime;
};


#endif
