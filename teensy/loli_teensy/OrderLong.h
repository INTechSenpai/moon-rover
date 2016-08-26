// OrderLong.h

#ifndef _ORDERLONG_h
#define _ORDERLONG_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include <vector>
#include "Singleton.h"

class OrderLong
{
public:
	OrderLong() : finished(false) {}

	/* Lancement de l'ordre long. L'argument correspond à un input (NEW_ORDER). */
	virtual void launch(const std::vector<uint8_t> &) = 0;

	/* Méthode exécutée en boucle durant l'exécution de l'odre. L'argument est un output, si il est non vide cela correspond à un STATUS_UPDATE. */
	virtual void onExecute(std::vector<uint8_t> &) = 0;
	
	/* Méthode indiquant si l'odre long a fini son exécution ou non. */
	bool isFinished()
	{
		return finished;
	}

	/* Méthode à appeler une fois que l'odre est ternminé. L'argument est un output, il correspond au contenu du EXECUTION_END. */
	virtual void terminate(std::vector<uint8_t> &) = 0;

protected:
	bool finished;
};


// ### Définition des ordres longs ###

class PingOfDeath : public OrderLong, public Singleton<PingOfDeath>
{
public:
	PingOfDeath(){}

	void launch(const std::vector<uint8_t> &)
	{
		Serial.println("Launch PingOfDeath");
	}

	void onExecute(std::vector<uint8_t> &)
	{
		static uint32_t lastPrintTime = 0;
		static int printCount = 0;
		if (millis() - lastPrintTime > 1000)
		{
			Serial.print("Ping of death ! #");
			Serial.println(printCount);
			lastPrintTime = millis();
			printCount++;
		}
		if (printCount > 10)
		{
			finished = true;
		}
	}

	void terminate(std::vector<uint8_t> &)
	{
		Serial.println("End of Ping of death");
	}
};


#endif

