#ifndef _ASCIIORDERLISTENER_h
#define _ASCIIORDERLISTENER_h

#if defined(ARDUINO) && ARDUINO >= 100
#include "arduino.h"
#else
#include "WProgram.h"
#endif

#define MAX_ORDER_SIZE	16

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
			if (rIndex >= MAX_ORDER_SIZE)
			{
				rIndex = 0;
				Log::critical(20, "Ordre ASCII trop long");
				return;
			}
			if (receivingState == ORDER_RECEIVED)
			{
				return;
			}

			char rByte = Serial.read();

			if (receivingState == RECEIVING_ORDER)
			{
				if ((rByte == '\n' || rByte == '\r') || rByte == ' ')
				{
					rBuffer[rIndex] = '\0';
					OrderStatus orderStatus = getID(rBuffer, lastOrder);
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
						lastOrderData.push_back(rBuffer[rIndex]);
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

	bool newOrderRecieved()
	{
		return receivingState == ORDER_RECEIVED;
	}

	void getLastOrder(uint8_t & order, std::vector<uint8_t> & data)
	{
		order = lastOrder;
		data = lastOrderData;
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

	OrderStatus getID(const char order[MAX_ORDER_SIZE], uint8_t & id)
	{
		OrderStatus orderStatus = KNOWN_ORDER;
		if (strcmp(order, "abwabwa") == 0)
		{
			id = 42;
		}
		else if (strcmp(order, "ronald") == 0)
		{
			id = 24;
		}
		else
		{
			orderStatus = UNKNOWN_ORDER;
		}
		return orderStatus;
	}
};


#endif

