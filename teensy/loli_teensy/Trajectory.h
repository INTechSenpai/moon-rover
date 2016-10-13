#ifndef _TRAJECTORY_h
#define _TRAJECTORY_h

#include <Printable.h>
#include "Position.h"

class TrajectoryPoint : public Printable
{
public:
	TrajectoryPoint()
	{
		upToDate = false;
		stopPoint = false;
		curvature = 0;
	}

	TrajectoryPoint(const Position & pos, uint8_t hw_sp_curv, uint8_t lw_sp_curv)
	{
		position = pos;
		stopPoint = hw_sp_curv & B10000000;
		upToDate = true;
		int16_t curv = lw_sp_curv;
		curv += (hw_sp_curv & B00111111) << 8;
		if (hw_sp_curv & B01000000)
		{
			curv = -curv;
		}
		curvature = ((float)curv) / 100;
	}

	Position getPosition() const
	{
		return position;
	}

	bool isStopPoint() const
	{
		return stopPoint;
	}

	float getCurvature() const
	{
		return curvature;
	}

	bool isUpToDate() const
	{
		return upToDate;
	}

	void makeObsolete()
	{
		upToDate = false;
	}


	size_t printTo(Print& p) const
	{
		size_t count = 0;
		count += p.print(position);
		count += p.printf("_%g_%d_%d", curvature, stopPoint, upToDate);
		return count;
	}

private:
	Position position;
	bool stopPoint;
	float curvature; // m^-1
	bool upToDate;
};


#endif

