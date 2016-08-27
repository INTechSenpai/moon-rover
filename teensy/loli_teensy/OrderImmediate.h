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
		M�thode ex�cutant l'ordre imm�diat.
		L'argument correspond � la fois � l'input et � l'output de l'odre, il sera modifi� par la m�thode.

		Si 'asciiMode' est pass� � True, alors l'argument n'est plus qu'un input. La m�thode doit envoyer sa r�ponse sur
		le canal de d�bug (USBserial). L'input est une chaine de caract�res qui devra donc �tre convertie en nombres avant usage.
	*/
	virtual void execute(std::vector<uint8_t> &, bool asciiMode = false) = 0;
};


// ### D�finition des ordres � r�ponse imm�diate ###

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

