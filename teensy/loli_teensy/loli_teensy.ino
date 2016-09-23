/*
 Name:		loli_teensy.ino
 Created:	02/08/2016 21:50:12
 Author:	Sylvain
*/


#include "Vutils.h"
#include "pin_mapping.h"
#include "BlockingMgr.h"
#include "DirectionController.h"
#include "Trajectory.h"
#include "Position.h"
#include "Log.h"
#include "AsciiOrderListener.h"
#include "Frame.h"
#include "OrderLong.h"
#include "OrderImmediate.h"
#include "OrderMgr.h"
#include "MotionControlSystem.h"
#include <vector>


void setup()
{
}


void loop()
{
	OrderMgr orderMgr(Serial1);
	AsciiOrderListener asciiOrder;
	uint8_t longOrder = 0x00;
	std::vector<uint8_t> longOrderData;
	bool longOrderRunning = false;

	while (true)
	{
		orderMgr.communicate();
		orderMgr.execute();
		//*
		asciiOrder.listen();
		if (asciiOrder.newImmediateOrderReceived())
		{
			uint8_t order;
			std::vector<uint8_t> data;
			asciiOrder.getLastOrder(order, data);
			orderMgr.executeImmediateOrder(order, data);
		}
		else if (asciiOrder.newLongOrderReceived() && !longOrderRunning)
		{
			asciiOrder.getLastOrder(longOrder, longOrderData);
			if (orderMgr.launchLongOrder(longOrder, longOrderData))
			{
				longOrderRunning = true;
			}
		}

		if (longOrderRunning)
		{
			orderMgr.executeLongOrder(longOrder);
			if (orderMgr.isLongOrderFinished(longOrder))
			{
				orderMgr.terminateLongOrder(longOrder);
				longOrderRunning = false;
			}
		}
		//*/
	}
}


/* Interruption d'asservissement */
void motionControlInterrupt()
{
	static MotionControlSystem & motionControlSystem = MotionControlSystem::Instance();
	motionControlSystem.control();
}




/* Ce bout de code permet de compiler avec std::vector */
namespace std {
	void __throw_bad_alloc()
	{
		while (true)
		{
			Log::critical(999, "Unable to allocate memory");
			delay(500);
		}
	}

	void __throw_length_error(char const*e)
	{
		while (true)
		{
			Log::critical(998, e);
			delay(500);
		}
	}

	void __throw_out_of_range(char const*e)
	{
		while (true)
		{
			Log::critical(997, e);
			delay(500);
		}
	}
}