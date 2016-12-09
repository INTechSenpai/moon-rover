#ifndef _INTERFACEAX12_h
#define _INTERFACEAX12_h

#include "Singleton.h"
#include "DynamixelInterface.h"
#include "communication_setup.h"

class InterfaceAX12 : public Singleton<InterfaceAX12>
{
public:
	InterfaceAX12():
		serial(SERIAL_AX)
	{
		serial.begin(SERIAL_AX_BAUDRATE);
	}
	DynamixelInterface serial;
};

#endif

