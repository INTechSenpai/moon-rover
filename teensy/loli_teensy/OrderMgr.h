#ifndef _ORDERMGR_h
#define _ORDERMGR_h

#include "OrderImmediate.h"
#include "OrderLong.h"
#include "Frame.h"


#define BAUDRATE				115200	// bit/s
#define TIMEOUT					10		// ms
#define STACK_SIZE				(UINT8_MAX + 1)
#define RECEPTION_BUFFER_SIZE	(UINT8_MAX + 1)
#define NB_ORDER				(UINT8_MAX + 1)

class OrderMgr
{
public:
	OrderMgr(Stream & serial) : HLserial(serial)
	{
		for (int i = 0; i < NB_ORDER; i++)
		{
			longOrderList[i] = (OrderLong*)NULL;
			immediateOrderList[i] = (OrderImmediate*)NULL;
		}

		/*	################################################## *
		 *	# Ici est définie la correspondance ID <-> Ordre # *
		 *	################################################## */

		// Ordres à réponse immédiate
		immediateOrderList[0x5A] = &Ping::Instance();

		// Ordres longs
		longOrderList[0] = &PingOfDeath::Instance();
	}

	void communicate()
	{
		static uint8_t receptionBuffer[RECEPTION_BUFFER_SIZE];
		static size_t rBufferIndice = 0;
		static uint8_t frameLength = 255;
		static uint8_t rByte;

		if (HLserial.available())
		{
			if (rBufferIndice >= RECEPTION_BUFFER_SIZE)
			{
				rBufferIndice = 0;
				HLserial.println("erreur 1");
				//TODO : throw strong error
				return;
			}

			rByte = HLserial.read();

			if (rBufferIndice == 0) // Type de trame
			{
				if (rByte < 0xF9)
				{
					HLserial.println("erreur 2");
					//TODO : throw error
					return;
				}
			}
			else if (rBufferIndice == 1) // Taille de la trame
			{
				if (rByte < 4)
				{
					rBufferIndice = 0;
					HLserial.println("erreur 3");
					//TODO : throw error
					return;
				}
				else
				{
					frameLength = rByte;
				}
			}

			receptionBuffer[rBufferIndice] = rByte;
			rBufferIndice++;

			if (rBufferIndice == frameLength) // Fin de la réception de la trame
			{
				std::vector<uint8_t> receivedData;
				for (unsigned int i = 0; i < rBufferIndice; i++)
				{
					receivedData.push_back(receptionBuffer[i]);
				}
				Frame receivedFrame(receivedData);
				if (receivedFrame.isFrameValid())
				{
					handleNewFrame(receivedFrame);
				}
				else
				{
					// TODO : drop 'receivedData' to log output
					HLserial.println("erreur 4");
				}
				rBufferIndice = 0;
				frameLength = 255;
			}
		}
	}

	void execute()
	{
		static std::vector<uint8_t> output;
		for (size_t i = 0; i < STACK_SIZE; i++)
		{
			if (orderStack[i].isUsed())
			{
				if (orderStack[i].frame.getOrderType() == LONG_ORDER)
				{
					if (orderStack[i].runState == RUNNING)
					{
						if (longOrderList[orderStack[i].orderID] == NULL)
						{
							HLserial.println("erreur 5");
							return; // TODO : throw error
						}
						output.clear();
						longOrderList[orderStack[i].orderID]->onExecute(output);
						if (output.size() > 0)
						{
							Frame statusUpdate(STATUS_UPDATE, i, orderStack[i].orderID, output);
							sendFrame(statusUpdate);
						}
						if (longOrderList[orderStack[i].orderID]->isFinished())
						{
							output.clear();
							longOrderList[orderStack[i].orderID]->terminate(output);
							Frame execEnd(EXECUTION_END, i, orderStack[i].orderID, output);
							sendFrame(execEnd);
							orderStack[i].runState = ENDING;
							orderStack[i].frame = output;
							orderStack[i].timestamp = millis();
						}
					}
					else if (orderStack[i].runState == ENDING)
					{
						if (millis() - orderStack[i].timestamp > TIMEOUT)
						{
							sendFrame(orderStack[i].frame);
							orderStack[i].timestamp = millis();
						}
					}
				}
			}
		}
	}

	void executeImmediateOrder(uint8_t id, std::vector<uint8_t> data)
	{
		immediateOrderList[id]->execute(data, true);
	}

private:

	enum RunningState
	{
		NOT_RUNNING,
		RUNNING,
		ENDING
	};

	class FrameHistory
	{
	public:
		FrameHistory()
		{
			runState = NOT_RUNNING;
		}
		RunningState runState;
		Frame frame;
		uint8_t orderID;
		uint32_t timestamp; // ms
		
		bool isUsed()
		{
			if (!frame.isFrameValid())
			{
				return false;
			}
			else if (frame.getOrderType() == IMMEDIATE_ORDER)
			{
				return millis() - timestamp < 2 * TIMEOUT;
			}
			else
			{
				return runState != NOT_RUNNING;
			}
		}
	};

	void handleNewFrame(const Frame & frame)
	{
		if (frame.getOrderType() == IMMEDIATE_ORDER)
		{
			handleImmediateOrder(frame);
		}
		else if (frame.getOrderType() == LONG_ORDER)
		{
			handleLongOrder(frame);
		}
	}

	void handleImmediateOrder(const Frame & frame)
	{
		if (frame.getFrameType() == VALUE_ANSWER)
		{
			HLserial.println("erreur 6");
			// TODO : drop frame to log output
		}
		else
		{
			if (orderStack[frame.getID()].isUsed())
			{// On envoie de nouveau l'acquittement
				sendFrame(orderStack[frame.getID()].frame);
				orderStack[frame.getID()].timestamp = millis();
			}
			else
			{
				if (immediateOrderList[frame.getOrder()] == NULL)
				{
					HLserial.println("erreur 7");
					// TODO : throw error
					return;
				}

				std::vector<uint8_t> data = frame.getData();
				immediateOrderList[frame.getOrder()]->execute(data);
				Frame answerFrame(VALUE_ANSWER, frame.getID(), frame.getOrder(), data);
				sendFrame(answerFrame);
				orderStack[frame.getID()].frame = answerFrame;
				orderStack[frame.getID()].orderID = frame.getOrder();
				orderStack[frame.getID()].timestamp = millis();
			}
		}
	}

	void handleLongOrder(const Frame & frame)
	{
		if (frame.getFrameType() == NEW_ORDER)
		{
			if (orderStack[frame.getID()].isUsed())
			{// On envoie de nouveau l'acquittement
				if (orderStack[frame.getID()].runState == RUNNING)
				{
					sendFrame(orderStack[frame.getID()].frame);
				}
			}
			else
			{
				if (longOrderList[frame.getOrder()] == NULL)
				{
					HLserial.println("erreur 9");
					return; // TODO : throw error
				}

				longOrderList[frame.getOrder()]->launch(frame.getData());
				Frame answerFrame(EXECUTION_BEGIN, frame.getID());
				sendFrame(answerFrame);
				orderStack[frame.getID()].frame = answerFrame;
				orderStack[frame.getID()].orderID = frame.getOrder();
				orderStack[frame.getID()].runState = RUNNING;
			}
		}
		else if (frame.getFrameType() == END_ORDER)
		{
			if (orderStack[frame.getID()].isUsed())
			{
				if (orderStack[frame.getID()].runState == ENDING)
				{
					orderStack[frame.getID()].runState = NOT_RUNNING;
				}
				else
				{
					HLserial.println("erreur 10");
					return; // TODO : throw error
				}
			}
			else
			{
				HLserial.println("erreur 11");
				return; // TODO : throw error
			}
		}
		else
		{
			HLserial.println("erreur 12");
			// TODO : throw error
		}
	}

	void sendFrame(const Frame & frame)
	{
		std::vector<uint8_t> frameVect = frame.getFrameVect();
		for (size_t i = 0; i < frameVect.size(); i++)
		{
			HLserial.write(frameVect.at(i));
		}
	}
	
	FrameHistory orderStack[STACK_SIZE];
	OrderLong* longOrderList[NB_ORDER];
	OrderImmediate* immediateOrderList[NB_ORDER];
	Stream & HLserial;
};

#endif

