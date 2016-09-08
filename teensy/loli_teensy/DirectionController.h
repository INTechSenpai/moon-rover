#ifndef _DIRECTIONCONTROLLER_h
#define _DIRECTIONCONTROLLER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Singleton.h"
#include <Printable.h>

class DirectionController : public Singleton<DirectionController>, public Printable
{
public:
	DirectionController()
	{

	}

	void control();
	void setAimCurvature(float);
	float getRealCurvature() const;

	size_t printTo(Print& p) const
	{

	}

private:


};


#endif

