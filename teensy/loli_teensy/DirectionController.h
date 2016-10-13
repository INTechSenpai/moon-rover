#ifndef _DIRECTIONCONTROLLER_h
#define _DIRECTIONCONTROLLER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Singleton.h"
#include "physical_dimensions.h"
#include <Printable.h>

#define LEFT_ANGLE_ORIGIN	150
#define RIGHT_ANGLE_ORIGIN	150

class DirectionController : public Singleton<DirectionController>, public Printable
{
public:
	DirectionController()
	{
		aimCurvature = 0;
		updateAimAngles();
		realLeftAngle = 0;
		realRightAngle = 0;
		updateRealCurvature();
	}

	void control()
	{
		updateAimAngles();
		sendAimAngles();
		readRealAngles();
		updateRealCurvature();
	}
	
	void setAimCurvature(float curvature)
	{
		aimCurvature = curvature;
	}
	float getRealCurvature() const
	{
		return realCurvature;
	}
	uint16_t getLeftAngle() const
	{
		return realLeftAngle;
	}
	uint16_t getRightAngle() const
	{
		return realRightAngle;
	}
	void setLeftAngle(uint16_t angle)
	{
		aimLeftAngle = angle;
	}
	void setRightAngle(uint16_t angle)
	{
		aimRightAngle = angle;
	}

	size_t printTo(Print& p) const
	{
		return p.printf("%g_%g_%d_%d", aimCurvature, realCurvature, realLeftAngle, realRightAngle);
	}

private:
	void readRealAngles()
	{
		//todo
	}

	void sendAimAngles()
	{
		//todo
	}

	void updateRealCurvature()
	{
		float leftCurvature, rightCurvature;
		float e = LEFT_RIGHT_WHEELS_DISTANCE / 2;
		if (realLeftAngle == 0)
		{
			leftCurvature = 0;
		}
		else
		{
			float leftAngle_rad = ((float)realLeftAngle - LEFT_ANGLE_ORIGIN) * PI / 180;
			leftCurvature = 1 / (FRONT_BACK_WHEELS_DISTANCE / tanf(leftAngle_rad) + e);
		}

		if (realRightAngle == 0)
		{
			rightCurvature = 0;
		}
		else
		{
			float rightAngle_rad = ((float)realRightAngle - RIGHT_ANGLE_ORIGIN) * PI / 180;
			rightCurvature = 1 / (FRONT_BACK_WHEELS_DISTANCE / tanf(rightAngle_rad) - e);
		}
		noInterrupts();
		realCurvature = (leftCurvature + rightCurvature) / 2;
		interrupts();
	}

	void updateAimAngles()
	{
		noInterrupts();
		float aimCurvature_cpy = aimCurvature;
		interrupts();
		float leftAngle_rad, rightAngle_rad;
		float e = LEFT_RIGHT_WHEELS_DISTANCE / 2;
		if (aimCurvature_cpy == 0)
		{
			leftAngle_rad = 0;
			rightAngle_rad = 0;
		}
		else
		{
			float bendRadius;
			bendRadius = 1 / aimCurvature_cpy;
			leftAngle_rad = atan2f(FRONT_BACK_WHEELS_DISTANCE, bendRadius - e);
			rightAngle_rad = atan2f(FRONT_BACK_WHEELS_DISTANCE, bendRadius + e);
		}
		aimLeftAngle = (uint16_t)(LEFT_ANGLE_ORIGIN + leftAngle_rad * 180 / PI);
		aimRightAngle = (uint16_t)(RIGHT_ANGLE_ORIGIN + rightAngle_rad * 180 / PI);
	}
	
	volatile float aimCurvature;
	volatile float realCurvature;
	uint16_t aimLeftAngle;
	uint16_t aimRightAngle;
	uint16_t realLeftAngle;
	uint16_t realRightAngle;
};


#endif

