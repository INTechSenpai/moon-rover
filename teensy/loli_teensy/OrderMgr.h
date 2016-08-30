#ifndef _ORDERMGR_h
#define _ORDERMGR_h

#include "OrderImmediate.h"
#include "OrderLong.h"
#include "Frame.h"
#include "Log.h"


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
		immediateOrderList[0x13] = &GetColor::Instance();

		// Ordres longs
		longOrderList[0x00] = &PingOfDeath::Instance();
		longOrderList[0x0C] = &Move::Instance();
	}

private:
	class ReceptionBuffer : public Printable
	{
	public:
		ReceptionBuffer() { indice = 0; }
		size_t indice;
		uint8_t buffer[RECEPTION_BUFFER_SIZE];

		size_t printTo(Print& p) const
		{
			size_t nbBytesWritten = 0;
			for (size_t i = 0; i < indice; i++)
			{
				nbBytesWritten += p.print(buffer[i], HEX);
				nbBytesWritten += p.print(" ");
			}
			return nbBytesWritten;
		}
	};

public:
	void communicate()
	{
		static ReceptionBuffer rBuffer;
		static uint8_t frameLength = 255;
		static uint8_t rByte;

		if (HLserial.available())
		{
			if (rBuffer.indice >= RECEPTION_BUFFER_SIZE)
			{
				rBuffer.indice = 0;
				Log::critical(1, "Reception buffer overflow");
				return;
			}

			rByte = HLserial.read();

			if (rBuffer.indice == 0) // Type de trame
			{
				if (rByte < 0xF9)
				{
					Log::critical(2, "Début de trame invalide. Lecture abandonnée.");
					return;
				}
			}
			else if (rBuffer.indice == 1) // Taille de la trame
			{
				if (rByte < 4)
				{
					rBuffer.indice = 0;
					Log::critical(3, "Taille de trame invalide. Lecture abandonnée.");
					return;
				}
				else
				{
					frameLength = rByte;
				}
			}

			rBuffer.buffer[rBuffer.indice] = rByte;
			rBuffer.indice++;

			if (rBuffer.indice == frameLength) // Fin de la réception de la trame
			{
				std::vector<uint8_t> receivedData;
				for (size_t i = 0; i < rBuffer.indice; i++)
				{
					receivedData.push_back(rBuffer.buffer[i]);
				}
				Frame receivedFrame(receivedData);
				if (receivedFrame.isFrameValid())
				{
					handleNewFrame(receivedFrame);
				}
				else
				{
					Log::critical(4, rBuffer, "Trame invalide");
				}
				rBuffer.indice = 0;
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
							Log::critical(5, "Ordre oublié");
						}
						output.clear();
						longOrderList[orderStack[i].orderID]->onExecute(output);
						if (output.size() > 0)
						{
							Frame statusUpdate(STATUS_UPDATE, i, output);
							sendFrame(statusUpdate);
						}
						if (longOrderList[orderStack[i].orderID]->isFinished())
						{
							output.clear();
							longOrderList[orderStack[i].orderID]->terminate(output);
							Frame execEnd(EXECUTION_END, i, output);
							sendFrame(execEnd);
							orderStack[i].runState = ENDING;
							orderStack[i].frame = execEnd;
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
			Log::critical(6, frame, "Type incorrect");
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
					Log::critical(7, frame, "Ordre inconnu");
					return;
				}

				std::vector<uint8_t> data = frame.getData();
				immediateOrderList[frame.getOrder()]->execute(data);
				Frame answerFrame(VALUE_ANSWER, frame.getID(), data);
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
					Log::critical(8, frame, "Ordre inconnu");
				}

				std::vector<uint8_t> data = frame.getData();
				longOrderList[frame.getOrder()]->launch(data);
				Frame answerFrame(EXECUTION_BEGIN, frame.getID(), data);
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
					Log::critical(9, frame, "Type END_ORDER incorrect");
				}
			}
			else
			{
				Log::warning("END_ORDER reçu pour une conversation déjà terminée.");
			}
		}
		else
		{
			Log::critical(10, frame, "Type incorrect");
		}
	}

	void sendFrame(const Frame & frame)
	{
		if (frame.isFrameValid())
		{
			std::vector<uint8_t> frameVect = frame.getFrameVect();
			for (size_t i = 0; i < frameVect.size(); i++)
			{
				HLserial.write(frameVect.at(i));
			}
		}
		else
		{
			Log::critical(11, "Tentative d'envoi d'une trame invalide");
		}
	}
	
	FrameHistory orderStack[STACK_SIZE];
	OrderLong* longOrderList[NB_ORDER];
	OrderImmediate* immediateOrderList[NB_ORDER];
	Stream & HLserial;
};

#endif

