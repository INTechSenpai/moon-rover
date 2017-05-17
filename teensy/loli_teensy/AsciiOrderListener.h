#ifndef _ASCIIORDERLISTENER_h
#define _ASCIIORDERLISTENER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "Arduino.h"
#else
	#include "WProgram.h"
#endif

#define MAX_ORDER_SIZE	16
#define ARG_SIZE	MAX_ORDER_SIZE

#include <string.h>
#include <vector>
#include "Log.h"

class AsciiOrderListener
{
public:
	AsciiOrderListener()
	{
		receivingState = RECEIVING_ORDER;
		rIndex = 0;
	}

	void listen()
	{
		if (Serial.available())
		{
			if (receivingState == ORDER_RECEIVED)
			{
				return;
			}
			if (rIndex >= MAX_ORDER_SIZE)
			{
				rIndex = 0;
				Log::critical(20, "Ordre ASCII trop long");
				return;
			}

			char rByte = Serial.read();

			if (receivingState == RECEIVING_ORDER)
			{
				if ((rByte == '\n' || rByte == '\r') || rByte == ' ')
				{
					rBuffer[rIndex] = '\0';
					OrderStatus orderStatus = getID(rBuffer, lastOrder, isImmediate);

					if (orderStatus == KNOWN_ORDER)
					{
						if (rByte == ' ')
						{
							receivingState = RECEIVING_DATA;
						}
						else
						{
							lastOrderData.clear();
							receivingState = ORDER_RECEIVED;
						}
					}
					else
					{
						Log::critical(21, "Ordre ASCII inconnu");
					}
					rIndex = 0;
				}
				else
				{
					rBuffer[rIndex] = rByte;
					rIndex++;
				}
			}
			else if (receivingState == RECEIVING_DATA)
			{
				if (rByte == '\n' || rByte == '\r')
				{
					lastOrderData.clear();
					for (size_t i = 0; i < rIndex; i++)
					{
						lastOrderData.push_back(rBuffer[i]);
					}
					receivingState = ORDER_RECEIVED;
					rIndex = 0;
				}
				else
				{
					rBuffer[rIndex] = rByte;
					rIndex++;
				}
			}
		}
	}

	bool newLongOrderReceived()
	{
		return receivingState == ORDER_RECEIVED && !isImmediate;
	}

	bool newImmediateOrderReceived()
	{
		return receivingState == ORDER_RECEIVED && isImmediate;
	}

	void getLastOrder(uint8_t & order, std::vector<uint8_t> & data)
	{
		order = lastOrder;
		data = lastOrderData;
		receivingState = RECEIVING_ORDER;
	}

	void trashLastOrder()
	{
		receivingState = RECEIVING_ORDER;
	}

private:

	enum ReceivingState
	{
		RECEIVING_ORDER,
		RECEIVING_DATA,
		ORDER_RECEIVED
	};

	ReceivingState receivingState;
	bool isImmediate;
	uint8_t lastOrder;
	std::vector<uint8_t> lastOrderData;
	char rBuffer[MAX_ORDER_SIZE];
	size_t rIndex;

	enum OrderStatus
	{
		KNOWN_ORDER,
		UNKNOWN_ORDER
	};


	/*	################################################## *
	*	# Définition de la correcpondance string <-> id  # *
	*	################################################## */

	OrderStatus getID(const char order[MAX_ORDER_SIZE], uint8_t & id, bool & immediate)
	{
		OrderStatus orderStatus = KNOWN_ORDER;
		if (strcmp(order, "logon") == 0) { id = 0x80; immediate = true; }
		else if (strcmp(order, "logoff") == 0) { id = 0x81; immediate = true; }
		else if (strcmp(order, "batt") == 0) { id = 0x82; immediate = true; }
		else if (strcmp(order, "stop") == 0) { id = 0x83; immediate = true; }
		else if (strcmp(order, "s") == 0) { id = 0x83; immediate = true; }
		else if (strcmp(order, "save") == 0) { id = 0x85; immediate = true; }
		else if (strcmp(order, "display") == 0) { id = 0x86; immediate = true; }
		else if (strcmp(order, "default") == 0) { id = 0x87; immediate = true; }
		else if (strcmp(order, "pos") == 0) { id = 0x8A; immediate = true; }
		else if (strcmp(order, "x") == 0) { id = 0x8B; immediate = true; }
		else if (strcmp(order, "y") == 0) { id = 0x8C; immediate = true; }
		else if (strcmp(order, "o") == 0) { id = 0x8D; immediate = true; }
		else if (strcmp(order, "rp") == 0) { id = 0x8E; immediate = true; }
		else if (strcmp(order, "dir") == 0) { id = 0x8F; immediate = true; }
		else if (strcmp(order, "axg") == 0) { id = 0x90; immediate = true; }
		else if (strcmp(order, "axd") == 0) { id = 0x91; immediate = true; }
		else if (strcmp(order, "cod") == 0) { id = 0x92; immediate = true; }
		else if (strcmp(order, "setaxid") == 0) { id = 0x93; immediate = true; }
		else if (strcmp(order, "pid") == 0) { id = 0x94; immediate = true; }
		else if (strcmp(order, "kp") == 0) { id = 0x95; immediate = true; }
		else if (strcmp(order, "ki") == 0) { id = 0x96; immediate = true; }
		else if (strcmp(order, "kd") == 0) { id = 0x97; immediate = true; }
		else if (strcmp(order, "smgre") == 0) { id = 0x98; immediate = true; }
		else if (strcmp(order, "smgrt") == 0) { id = 0x99; immediate = true; }
		else if (strcmp(order, "bmgrs") == 0) { id = 0x9A; immediate = true; }
		else if (strcmp(order, "bmgrt") == 0) { id = 0x9B; immediate = true; }
		else if (strcmp(order, "mms") == 0) { id = 0x9C; immediate = true; }
		else if (strcmp(order, "macc") == 0) { id = 0x9D; immediate = true; }
		else if (strcmp(order, "cp") == 0) { id = 0x9E; immediate = true; }
		else if (strcmp(order, "cvg") == 0) { id = 0x9F; immediate = true; }
		else if (strcmp(order, "cvd") == 0) { id = 0xA0; immediate = true; }
		else if (strcmp(order, "cpwm") == 0) { id = 0xA1; immediate = true; }
		else if (strcmp(order, "pwm") == 0) { id = 0xA2; immediate = false; }
		else if (strcmp(order, "a") == 0) { id = 0xA3; immediate = false; }
		else if (strcmp(order, "p") == 0) { id = 0xA4; immediate = false; }
		else if (strcmp(order, "k1") == 0) { id = 0xA5; immediate = true; }
		else if (strcmp(order, "k2") == 0) { id = 0xA6; immediate = true; }
		else if (strcmp(order, "mdec") == 0) { id = 0xA7; immediate = true; }
		else if (strcmp(order, "capt") == 0) { id = 0xB0; immediate = true; }
		else if (strcmp(order, "axn") == 0) { id = 0xB5; immediate = true; }
		else if (strcmp(order, "pdn") == 0) { id = 0x3D; immediate = false; }
		else if (strcmp(order, "pnh") == 0) { id = 0x3E; immediate = false; }
		else if (strcmp(order, "pun") == 0) { id = 0x3F; immediate = false; }
		else if (strcmp(order, "opn") == 0) { id = 0x40; immediate = false; }
		else if (strcmp(order, "cln") == 0) { id = 0x41; immediate = false; }
		else if (strcmp(order, "cff") == 0) { id = 0x42; immediate = false; }
		else if (strcmp(order, "els") == 0) { id = 0x43; immediate = false; }
		else if (strcmp(order, "rls") == 0) { id = 0x44; immediate = false; }
		else if (strcmp(order, "ers") == 0) { id = 0x45; immediate = false; }
		else if (strcmp(order, "rrs") == 0) { id = 0x46; immediate = false; }
		else if (strcmp(order, "fa") == 0) { id = 0x47; immediate = false; }
		else if (strcmp(order, "ln") == 0) { id = 0x48; immediate = false; }
		else if (strcmp(order, "scann") == 0) { id = 0x49; immediate = false; }
		else if (strcmp(order, "clnf") == 0) { id = 0x4A; immediate = false; }
		else if (strcmp(order, "abort") == 0) { id = 0xBE; immediate = true; }
		else if (strcmp(order, "help") == 0) { id = 0xBF; immediate = true; }
		else if (strcmp(order, "addt") == 0) { id = 0xB1; immediate = true; }
		else if (strcmp(order, "ft") == 0) { id = 0xB2; immediate = false; }
		else if (strcmp(order, "tax") == 0) { id = 0xB3; immediate = false; }
		else
		{
			orderStatus = UNKNOWN_ORDER;
		}
		return orderStatus;
	}
};


#endif

