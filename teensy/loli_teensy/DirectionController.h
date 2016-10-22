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

/* Angles des AX12 correspondant à des roues alignées vers l'avant */
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
		if (realLeftAngle == LEFT_ANGLE_ORIGIN)
		{
			leftCurvature = 0;
		}
		else
		{
			float leftAngle_rad = ((float)realLeftAngle - LEFT_ANGLE_ORIGIN) * PI / 180;
			leftCurvature = 1 / (FRONT_BACK_WHEELS_DISTANCE / tanf(leftAngle_rad) + DIRECTION_ROTATION_POINT_Y);
		}

		if (realRightAngle == RIGHT_ANGLE_ORIGIN)
		{
			rightCurvature = 0;
		}
		else
		{
			float rightAngle_rad = ((float)realRightAngle - RIGHT_ANGLE_ORIGIN) * PI / 180;
			rightCurvature = 1 / (FRONT_BACK_WHEELS_DISTANCE / tanf(rightAngle_rad) - DIRECTION_ROTATION_POINT_Y);
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
		if (aimCurvature_cpy == 0)
		{
			leftAngle_rad = 0;
			rightAngle_rad = 0;
		}
		else
		{
			float bendRadius;
			bendRadius = 1 / aimCurvature_cpy;
			leftAngle_rad = atan2f(FRONT_BACK_WHEELS_DISTANCE, bendRadius - DIRECTION_ROTATION_POINT_Y);
			rightAngle_rad = atan2f(FRONT_BACK_WHEELS_DISTANCE, bendRadius + DIRECTION_ROTATION_POINT_Y);
		}
		aimLeftAngle = (uint16_t)(LEFT_ANGLE_ORIGIN + leftAngle_rad * 180 / PI);
		aimRightAngle = (uint16_t)(RIGHT_ANGLE_ORIGIN + rightAngle_rad * 180 / PI);
	}
	
	/* Courburen, en m^-1 */
	volatile float aimCurvature;
	volatile float realCurvature;

	/* Angles des AX12, en degrés */
	uint16_t aimLeftAngle;
	uint16_t aimRightAngle;
	uint16_t realLeftAngle;
	uint16_t realRightAngle;
};


#endif

