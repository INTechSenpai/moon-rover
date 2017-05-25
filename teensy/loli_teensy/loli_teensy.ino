/*
 Name:		loli_teensy.ino
 Created:	02/08/2016 21:50:12
 Author:	Sylvain
*/


#include "Median.h"
#include "RAM_check.h"
#include "ActuatorStatus.h"
#include "start_potitions.h"
#include "LedMgr.h"
#include "StartupMgr.h"
#include "BatterySensor.h"
#include "ActuatorMgr.h"
#include "ControlerNet.h"
#include "SynchronousPWM.h"

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

#include "ax12config.h"
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
#include <Wire.h>
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
	

	SensorMgr & sensorMgr = SensorMgr::Instance();
	Wire.begin();
	Wire.setClock(400000);
	sensorMgr.powerOn();
	sensorMgr.setUpdatePattern(SensorMgr::FRONT_AND_BACK);

	DirectionController & directionController = DirectionController::Instance();
	MotionControlSystem & motionControlSystem = MotionControlSystem::Instance();

	IntervalTimer motionControlTimer;
	motionControlTimer.priority(253);
	motionControlTimer.begin(motionControlInterrupt, PERIOD_ASSERV); // 1kHz

	IntervalTimer synchronousPWM_timer;
	synchronousPWM_timer.priority(250);
	synchronousPWM_timer.begin(synchronousPWM_interrupt, 36); // 440Hz avec un pwm codé sur 6bits

	BatterySensor & batterySensor = BatterySensor::Instance();
	StartupMgr & startupMgr = StartupMgr::Instance();
	LedMgr & ledMgr = LedMgr::Instance();

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
					Serial.printf("Fin ordre long 0x%x\n", longOrder);
				}
			}
		}

		/* Gestion des AX12 de direction */
		directionController.control();

		/* Mise à jour des capteurs */
		sensorMgr.update();

		/* Mise à jour du niveau de batterie */
		batterySensor.update();

		/* Vérification de la rapidité d'exécution */
		//checkSpeed(10000, 100);

		/* Print des logs */
		motionControlSystem.logAllData();

		/* Gestion de la phase de choix de la couleur (côté de la table) */
		startupMgr.update();

		/* Gestion de l'allumage des DELs du robot */
		ledMgr.update();

		/* Alerte visuelle en cas de niveau bas de batterie */
		if (batterySensor.getLevel() < 20)
		{
			ledMgr.statusLed_lowBattery();
		}
		else
		{
			ledMgr.statusLed_batteryOk();
		}

		//static uint32_t loli = 0;
		//if (millis() - loli > 100)
		//{
		//	loli = millis();
		//	Serial.println(FreeRam());
		//	uint8_t mState = (uint8_t)motionControlSystem.getMovingState();
		//	Serial.printf("%u - s=%u\n", millis(), mState);
		//}

		static uint32_t lol = 0;
		static uint8_t lastIndex = 255, index = 0;
		static uint8_t lastMovingState = 0, movingState = 0;
		static Position p;
		if (millis() - lol > 100)
		{
			lol = millis();
			index = motionControlSystem.getTrajectoryIndex();
			movingState = (uint8_t)motionControlSystem.getMovingState();
			if (index != lastIndex || movingState != lastMovingState)
			{
				motionControlSystem.getPosition(p);

				Serial.printf("%u - i=%u s=%d p=", millis(), index, movingState);
				Serial.println(p);

				lastMovingState = movingState;
				lastIndex = index;
			}

			static bool ordersRunning = false;
			uint32_t nbOrdersRunning = orderMgr.getNbOrdersRunning();
			if (nbOrdersRunning > 0 && !ordersRunning)
			{
				ordersRunning = true;
				//Serial.println("HL orders started");
			}
			else if (nbOrdersRunning == 0 && ordersRunning)
			{
				ordersRunning = false;
				//Serial.println("All HL orders terminated");
			}
		}

		static uint32_t chibre = 0;
		if (millis() - chibre > 1000)
		{
			chibre = millis();
			//orderMgr.printOrdersRunning();
		}
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


/* Calcul des 'FPS' de la boucle principale et de l'interruption d'asservissement */
void checkSpeed(uint32_t daemonPeriod, float threshold)
{
	static MotionControlSystem & motionControlSystem = MotionControlSystem::Instance();
	static const size_t bufferSize = 1;
	static Average<float, bufferSize> bufferFPS;
	static uint32_t lastCallTime = 0, lastPrintTime = 0;

	bufferFPS.add(1000000 / (float)(micros() - lastCallTime));
	lastCallTime = micros();

	float averageFPS = bufferFPS.value();
	if ((daemonPeriod != 0 && millis() - lastPrintTime > daemonPeriod) ||
		(threshold != 0 && averageFPS < threshold))
	{
		if (threshold != 0 && averageFPS < threshold)
		{
			Serial.print("!!! ");
		}
		Serial.printf("FPS= %g  Asserv= %u (max=%u)\n", 
			averageFPS,
			motionControlSystem.getLastInterruptDuration(),
			motionControlSystem.getMaxInterruptDuration());
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

	void __throw_out_of_range_fmt(char const*e, ...)
	{
		while (true)
		{
			Log::critical(996, e);
			delay(500);
		}
	}
}