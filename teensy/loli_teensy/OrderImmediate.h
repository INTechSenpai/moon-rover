// OrderImmediate.h

#ifndef _ORDERIMMEDIATE_h
#define _ORDERIMMEDIATE_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include <vector>
#include "Singleton.h"

class OrderImmediate
{
public:
	OrderImmediate(){}

	/*
		Méthode exécutant l'ordre immédiat.
		L'argument correspond à la fois à l'input et à l'output de l'odre, il sera modifié par la méthode.

		Si 'asciiMode' est passé à True, alors l'argument n'est plus qu'un input. La méthode doit envoyer sa réponse sur
		le canal de débug (USBserial). L'input est une chaine de caractères qui devra donc être convertie en nombres avant usage.
	*/
	virtual void execute(std::vector<uint8_t> &, bool asciiMode = false) = 0;
};


// ### Définition des ordres à réponse immédiate ###

class Ping : public OrderImmediate, public Singleton<Ping>
{
public:
	Ping(){}

	virtual void execute(std::vector<uint8_t> & io, bool asciiMode = false)
	{
		if (asciiMode)
		{
			Serial.print("Ping !");
		}
		else
		{
			io.clear();
			//Serial.print("LOLI");
		}
	}
};


#endif

