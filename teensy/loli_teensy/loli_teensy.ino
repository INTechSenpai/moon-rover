/*
 Name:		loli_teensy.ino
 Created:	02/08/2016 21:50:12
 Author:	Sylvain
*/


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
	OrderMgr orderMgr(Serial);
	AsciiOrderListener asciiOrder;
	while (true)
	{
		orderMgr.communicate();
		orderMgr.execute();
		/*
		asciiOrder.listen();
		if (asciiOrder.newOrderRecieved())
		{
			uint8_t order;
			std::vector<uint8_t> data;
			asciiOrder.getLastOrder(order, data);
			orderMgr.executeImmediateOrder(order, data);
		}
		*/
	}
}

/* Interruption d'asservissement */
void motionControlInterrupt()
{
	static MotionControlSystem & motionControlSystem = MotionControlSystem::Instance();
	motionControlSystem.control();
	motionControlSystem.manageBlocking();
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