/*
 Name:		loli_teensy.ino
 Created:	02/08/2016 21:50:12
 Author:	Sylvain
*/

#include "ax12config.h"
#include "SynchronousPWM.h"
#include "ActuatorMgr.h"
#include "ControlerNet.h"

#include "Dynamixel.h"
#include "DynamixelInterface.h"
#include "DynamixelMotor.h"

#include "AsciiOrderListener.h"
#include "Frame.h"
#include "InterfaceAX12.h"
#include "Log.h"
#include "OrderImmediate.h"
#include "OrderLong.h"
#include "OrderMgr.h"

#include "communication_setup.h"
#include "physical_dimensions.h"
#include "pin_mapping.h"

#include "BlockingMgr.h"
#include "DirectionController.h"
#include "Encoder.h"
#include "MotionControlSystem.h"
#include "Motor.h"
#include "PID.h"
#include "Position.h"
#include "Trajectory.h"

#include "InfraredSensor.h"
#include "SensorMgr.h"
#include "ToF_longRange.h"
#include "ToF_shortRange.h"
#include "VL53L0X.h"
#include "VL6180X.h"

#include "Average.h"
#include "Singleton.h"
#include "utils.h"
#include "Vutils.h"

#include <Arduino.h>
#include <Wire\Wire.h>
#include <EEPROM.h>
#include <vector>


void setup()
{
	delay(500);
}


void loop()
{
	if (debug_serial_free)
	{
		Serial.println("Loli-Teensy");
	}

	OrderMgr orderMgr(SERIAL_HL);
	AsciiOrderListener asciiOrder;
	uint8_t longOrder = 0x00;
	std::vector<uint8_t> longOrderData;
	bool longOrderRunning = false;

	SERIAL_HL.begin(SERIAL_HL_BAUDRATE);
	
	Wire.begin();
	Wire.setClock(1500000);
	SensorMgr & sensorMgr = SensorMgr::Instance();
	//sensorMgr.powerOn();
	uint32_t updatePattern[NB_SENSORS] = {};
	sensorMgr.setUpdatePattern(updatePattern);

	DirectionController & directionController = DirectionController::Instance();

	IntervalTimer motionControlTimer;
	motionControlTimer.priority(253);
	motionControlTimer.begin(motionControlInterrupt, 1000); // 1kHz

	IntervalTimer synchronousPWM_timer;
	synchronousPWM_timer.priority(250);
	synchronousPWM_timer.begin(synchronousPWM_interrupt, 36); // 440Hz avec un pwm codé sur 6bits

	if (debug_serial_free)
	{
		Serial.println("Initialised");
	}

	while (true)
	{
		/* Gestion des ordres binaires */
		orderMgr.communicate();
		orderMgr.execute();

		/* Gestion des ordres ASCII */
		if (debug_serial_free)
		{// Les ordres ASCII son désactivés si la série de débug est déjà utilisée
			asciiOrder.listen();
			if (asciiOrder.newImmediateOrderReceived())
			{
				uint8_t order;
				std::vector<uint8_t> data;
				asciiOrder.getLastOrder(order, data);
				if (order == 0xBE)
				{// "abort" termine l'ordre long en cours.
					if (longOrderRunning)
					{
						orderMgr.terminateLongOrder(longOrder);
						longOrderRunning = false;
						Serial.printf("Interruption de l'ordre long (id=0x%x)\n", longOrder);
					}
				}
				else
				{
					orderMgr.executeImmediateOrder(order, data);
				}
			}
			else if (asciiOrder.newLongOrderReceived())
			{
				if (!longOrderRunning)
				{
					asciiOrder.getLastOrder(longOrder, longOrderData);
					if (orderMgr.launchLongOrder(longOrder, longOrderData))
					{
						longOrderRunning = true;
					}
				}
				else
				{
					Serial.print("Ordre long deja en cours d'execution; id=0x");
					Serial.print(longOrder, HEX);
					if (longOrderData.size() > 0)
					{
						Serial.print(" arg=");
						for (size_t i = 0; i < longOrderData.size(); i++)
						{
							char orderChar = longOrderData.at(i);
							if (orderChar != '\r' && orderChar != '\n')
							{
								Serial.print(orderChar);
							}
						}
					}
					Serial.println();
					asciiOrder.trashLastOrder();
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
		}

		/* Gestion des AX12 de direction */
		directionController.control();

		/* Mise à jour des capteurs */
		sensorMgr.update();

		/* Vérification de la rapidité d'exécution */
		checkSpeed();
	}
}


/* Interruption d'asservissement */
void motionControlInterrupt()
{
	static MotionControlSystem & motionControlSystem = MotionControlSystem::Instance();
	motionControlSystem.control();
}


/* Interruption contrôlant les PMW synchrones des ponts en H du filet */
void synchronousPWM_interrupt()
{
	static SynchronousPWM & synchronousPWM = SynchronousPWM::Instance();
	synchronousPWM.update();
}


/* Calcul des 'FPS' de la boucle principale */
void checkSpeed()
{
	static const size_t bufferSize = 100;
	static Average<float, bufferSize> bufferFPS;
	static uint32_t lastCallTime = 0, lastPrintTime = 0;

	bufferFPS.add(1000000 / (float)(micros() - lastCallTime));
	lastCallTime = micros();

	float averageFPS = bufferFPS.value();
	if (averageFPS < 4000)
	{
		Serial.printf("FPS= %g\n", averageFPS);
		lastPrintTime = millis();
	}
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