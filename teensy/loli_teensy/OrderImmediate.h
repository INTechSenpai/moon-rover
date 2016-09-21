// OrderImmediate.h

#ifndef _ORDERIMMEDIATE_h
#define _ORDERIMMEDIATE_h

#include <vector>
#include "Singleton.h"

class OrderImmediate
{
public:
	OrderImmediate(){}

	/*
		Méthode exécutant l'ordre immédiat.
		L'argument correspond à la fois à l'input et à l'output de l'odre, il sera modifié par la méthode.
	*/
	virtual void execute(std::vector<uint8_t> &) = 0;
};


// ### Définition des ordres à réponse immédiate ###

class Rien : public OrderImmediate, public Singleton<Rien>
{
public:
	Rien(){}
	virtual void execute(std::vector<uint8_t> & io){}
};


class Ping : public OrderImmediate, public Singleton<Ping>
{
public:
	Ping(){}

	virtual void execute(std::vector<uint8_t> & io)
	{
		Serial.print("Ping !");
		io.clear();
	}
};


class GetColor : public OrderImmediate, public Singleton<GetColor>
{
public:
	GetColor() {}
	virtual void execute(std::vector<uint8_t> & io) 
	{
		enum Side
		{
			INTECH = 0x00,
			WINDOW = 0x01,
			UNKNOWN = 0x02
		};

		io.clear();
		
		// DEBUG
		static bool called = false;
		static uint32_t t;
		if (!called)
		{
			t = millis();
			called = true;
		}
		if (millis() - t > 2000)
			io.push_back(INTECH);
		else
			io.push_back(UNKNOWN);
	}
};


#endif

