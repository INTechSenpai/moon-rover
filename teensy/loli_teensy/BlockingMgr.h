#ifndef _BLOCKINGMGR_h
#define _BLOCKINGMGR_h

/*
	Permet de détecter le blocage physique d'un moteur disposant d'un encodeur 
*/

#include <Printable.h>
#include "utils.h"
#include "Position.h"


class BlockingMgr : public Printable
{
public:
	BlockingMgr(volatile int32_t const & aimSpeed, volatile int32_t const & realSpeed) :
		aimSpeed(aimSpeed),
		realSpeed(realSpeed)
	{
		sensibility = 0;
		responseTime = 0;
		beginTime = 0;
		blocked = false;
	}

	void compute()
	{
		if (ABS(realSpeed) < ABS(aimSpeed)*sensibility)
		{
			if (!blocked)
			{
				blocked = true;
				beginTime = millis();
			}
		}
		else
		{
			blocked = false;
		}
	}

	void setTunings(float sensibility, uint32_t responseTime)
	{
		this->sensibility = sensibility;
		this->responseTime = responseTime;
	}

	void getTunings(float & sensibility, uint32_t & responseTime) const
	{
		sensibility = this->sensibility;
		responseTime = this->responseTime;
	}

	bool isBlocked() const
	{
		return blocked && (millis() - beginTime > responseTime);
	}

	size_t printTo(Print& p) const
	{
		return p.printf("%d_%d_%d", aimSpeed, realSpeed, isBlocked());
	}

private:
	volatile int32_t const & aimSpeed;
	volatile int32_t const & realSpeed;

	float sensibility; // Entre 0 et 1. Le seuil "vitesse insuffisante" vaut aimSpeed*sensibility
	uint32_t responseTime; // ms

	bool blocked;
	uint32_t beginTime;
};



class StoppingMgr : public Printable
{
public:
	StoppingMgr(volatile int32_t const & speed) :
		speed(speed)
	{
		epsilon = 0;
		responseTime = 0;
		stopped = false;
		moveBegin = false;
		beginTime = 0;
		breaking = false;
	}

	void compute()
	{
		abs_speed = ABS(speed);
		if ((uint32_t)abs_speed < epsilon)
		{
			if (!stopped)
			{
				stopped = true;
				beginTime = millis();
			}
		}
		else
		{
			stopped = false;
			moveBegin = false;
		}
		averageAcceleration.add(abs_speed - last_abs_speed);
		last_abs_speed = abs_speed;
	}

	void moveIsStarting()
	{
		stopped = false;
		moveBegin = true;
	}

	void setTunings(uint32_t epsilon, uint32_t responseTime)
	{
		this->epsilon = epsilon;
		this->responseTime = responseTime;
	}

	void getTunings(uint32_t & epsilon, uint32_t & responseTime) const
	{
		epsilon = this->epsilon;
		responseTime = this->responseTime;
	}

	bool isStopped() const
	{
		if (moveBegin)
		{
			return stopped && millis() - beginTime > responseTime * 5;
		}
		else
		{
			return stopped && millis() - beginTime > responseTime;
		}
	}

	bool isMoveBegin() const
	{
		return moveBegin;
	}

	/* Indique si on est en train de ralentir */
	bool isBreaking() const
	{
		return averageAcceleration.value() < 0;
	}

	size_t printTo(Print& p) const
	{
		return p.printf("%d_%d", speed, isStopped());
	}

private:
	volatile int32_t const & speed;
	
	uint32_t epsilon;
	uint32_t responseTime; // ms

	uint32_t beginTime;
	bool stopped;
	bool moveBegin;
	int32_t abs_speed;
	int32_t last_abs_speed;
	bool breaking;
	Average<int32_t, 25> averageAcceleration;
};

#endif

