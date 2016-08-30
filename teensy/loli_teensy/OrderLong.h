// OrderLong.h

#ifndef _ORDERLONG_h
#define _ORDERLONG_h

#include <vector>
#include "Singleton.h"

class OrderLong
{
public:
	OrderLong() : finished(false) {}

	/* Lancement de l'ordre long. L'argument correspond � un input (NEW_ORDER). */
	virtual void launch(const std::vector<uint8_t> &) = 0;

	/* M�thode ex�cut�e en boucle durant l'ex�cution de l'odre. L'argument est un output, si il est non vide cela correspond � un STATUS_UPDATE. */
	virtual void onExecute(std::vector<uint8_t> &) = 0;
	
	/* M�thode indiquant si l'odre long a fini son ex�cution ou non. */
	bool isFinished()
	{
		return finished;
	}

	/* M�thode � appeler une fois que l'odre est ternmin�. L'argument est un output, il correspond au contenu du EXECUTION_END. */
	virtual void terminate(std::vector<uint8_t> &) = 0;

protected:
	bool finished;
};


// ### D�finition des ordres longs ###

class RienL : public OrderLong, public Singleton<RienL>
{
public:
	RienL(){}
	void launch(const std::vector<uint8_t> & input)
	{}
	void onExecute(std::vector<uint8_t> & output)
	{}
	void terminate(std::vector<uint8_t> & output)
	{}
};

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


class Move : public OrderLong, public Singleton<Move>
{
public:
	Move()
	{
		called = false;
	}
	void launch(const std::vector<uint8_t> & input)
	{
		beginTime = millis();
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		uint32_t now = millis();
		if ((now - beginTime) % 500 == 0)
		{
			output.push_back(now & (0xFF << 24));
			output.push_back(now & (0xFF << 16));
			output.push_back(now & (0xFF << 8));
			output.push_back(now & (0xFF));
		}
	}
	void terminate(std::vector<uint8_t> & output)
	{
		if (called)
		{
			output.push_back(ARRIVED);
		}
		else
		{
			output.push_back(BLOCKED);
		}
		called = true;
	}
	enum RETURN_STATE
	{
		ARRIVED = 0x00,
		BLOCKED = 0x01
	};
private:
	uint32_t beginTime;
	bool called;
};


#endif

