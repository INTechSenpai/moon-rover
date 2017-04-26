#ifndef _ORDERMGR_h
#define _ORDERMGR_h

#include "OrderImmediate.h"
#include "OrderLong.h"
#include "Frame.h"
#include "Log.h"
#include "serial1.c"


#define TIMEOUT					30		// ms
#define STACK_SIZE				(UINT8_MAX + 1)
#define RECEPTION_BUFFER_SIZE	(UINT8_MAX + 1)
#define NB_ORDER				(UINT8_MAX + 1)

class OrderMgr
{
public:
	/*
		Le constructeur prend en argument un objet 'Stream' afin de pouvoir utiliser indifférement
		'HardwareSerial' ou 'usb_serial_class'. En contrepartie, la classe 'Stream' n'impémentant
		pas de méthode 'begin', il est nécéssaire d'ouvrir la liaison avant de la donner à ce
		constructeur (ou du moins de l'ouvrir avant de faire appel à 'communicate').
	*/
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
		immediateOrderList[0x59] = &GetColor::Instance();
		immediateOrderList[0x5A] = &Ping::Instance();
		immediateOrderList[0x5B] = &AddTrajectoryPoints::Instance();
		immediateOrderList[0x5C] = &SetMaxSpeed::Instance();
		immediateOrderList[0x5D] = &EditPosition::Instance();
		immediateOrderList[0x5E] = &StopStream::Instance();

		// Ordres longs
		longOrderList[0x38] = &FollowTrajectory::Instance();
		longOrderList[0x39] = &Stop::Instance();
		longOrderList[0x3A] = &WaitForJumper::Instance();
		longOrderList[0x3B] = &StartMatchChrono::Instance();
		longOrderList[0x3C] = &StreamAll::Instance();
		longOrderList[0x3D] = &PullDownNet::Instance();
		longOrderList[0x3E] = &PutNetHalfway::Instance();
		longOrderList[0x3F] = &PullUpNet::Instance();
		longOrderList[0x40] = &OpenNet::Instance();
		longOrderList[0x41] = &CloseNet::Instance();
		longOrderList[0x42] = &CrossFlipFlop::Instance();
		longOrderList[0x43] = &EjectLeftSide::Instance();
		longOrderList[0x44] = &RearmLeftSide::Instance();
		longOrderList[0x45] = &EjectRightSide::Instance();
		longOrderList[0x46] = &RearmRightSide::Instance();
		longOrderList[0x47] = &FunnyAction::Instance();
		longOrderList[0x48] = &LockNet::Instance();

		// Ordres ASCII
		immediateOrderList[0x80] = &Logon::Instance();
		immediateOrderList[0x81] = &Logoff::Instance();
		immediateOrderList[0x82] = &Batt::Instance();
		immediateOrderList[0x83] = &Stop_ascii::Instance();
		
		immediateOrderList[0x85] = &Save::Instance();
		immediateOrderList[0x86] = &Display::Instance();
		immediateOrderList[0x87] = &Default::Instance();
		
		immediateOrderList[0x8A] = &Pos::Instance();
		immediateOrderList[0x8B] = &PosX::Instance();
		immediateOrderList[0x8C] = &PosY::Instance();
		immediateOrderList[0x8D] = &PosO::Instance();
		immediateOrderList[0x8E] = &Rp::Instance();
		immediateOrderList[0x8F] = &Dir::Instance();
		immediateOrderList[0x90] = &Axg::Instance();
		immediateOrderList[0x91] = &Axd::Instance();
		immediateOrderList[0x92] = &Cod::Instance();
		immediateOrderList[0x93] = &Setaxid::Instance();
		immediateOrderList[0x94] = &Pid_c::Instance();
		immediateOrderList[0x95] = &Pid_kp::Instance();
		immediateOrderList[0x96] = &Pid_ki::Instance();
		immediateOrderList[0x97] = &Pid_kd::Instance();
		immediateOrderList[0x98] = &Smgre::Instance();
		immediateOrderList[0x99] = &Smgrt::Instance();
		immediateOrderList[0x9A] = &Bmgrs::Instance();
		immediateOrderList[0x9B] = &Bmgrt::Instance();
		immediateOrderList[0x9C] = &Mms::Instance();
		immediateOrderList[0x9D] = &Macc::Instance();
		immediateOrderList[0x9E] = &Control_p::Instance();
		immediateOrderList[0x9F] = &Control_vg::Instance();
		immediateOrderList[0xA0] = &Control_vd::Instance();
		immediateOrderList[0xA1] = &Control_pwm::Instance();
		
		longOrderList[0xA2] = &Test_pwm::Instance();
		longOrderList[0xA3] = &Test_speed::Instance();
		longOrderList[0xA4] = &Test_pos::Instance();

		immediateOrderList[0xA5] = &Curv_k1::Instance();
		immediateOrderList[0xA6] = &Curv_k2::Instance();

		immediateOrderList[0xB0] = &Capt::Instance();
		immediateOrderList[0xB1] = &AddTraj_test::Instance();

		longOrderList[0xB2] = &FollowTrajectory_ascii::Instance();
		longOrderList[0xB3] = &TestAX12::Instance();

		immediateOrderList[0xB5] = &AxNet::Instance();

		immediateOrderList[0xBF] = &Help::Instance();
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

		int nbReadingPerformed = 0;
		while (HLserial.available() && nbReadingPerformed < 32) // On s'autorise à lire 32 octets d'affilé (sans rendre la main à la boucle principale)
		{
			if (HLserial.available() == RX_BUFFER_SIZE - 1)
			{
				Log::warning("Buffer reception bas niveau plein : possible perte de donnees");
			}
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
					Log::critical(2, "Debut de trame invalide. Lecture abandonnee.");
					return;
				}
			}
			else if (rBuffer.indice == 1) // Taille de la trame
			{
				if (rByte < 4)
				{
					rBuffer.indice = 0;
					Log::critical(3, "Taille de trame invalide. Lecture abandonnee.");
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
				break; // On rend la main à la boucle principale en fin de lecture de trame.
			}
			nbReadingPerformed++; // On compte le nombre d'octets lus d'affilé
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
							Log::critical(5, "Ordre oublie");
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
		if (immediateOrderList[id] == NULL)
		{
			Log::critical(id, "Ordre inconnu");
		}
		else
		{
			immediateOrderList[id]->execute(data);
		}
	}

	bool launchLongOrder(uint8_t id, std::vector<uint8_t> data)
	{
		if (longOrderList[id] == NULL)
		{
			Log::critical(id, "Ordre inconnu");
			return false;
		}
		else
		{
			longOrderList[id]->launch(data);
			return true;
		}
	}

	void executeLongOrder(uint8_t id)
	{
		if (longOrderList[id] == NULL)
		{
			Log::critical(id, "Ordre inconnu");
		}
		else
		{
			std::vector<uint8_t> out;
			longOrderList[id]->onExecute(out);
		}
	}

	bool isLongOrderFinished(uint8_t id)
	{
		if (longOrderList[id] == NULL)
		{
			Log::critical(id, "Ordre inconnu");
			return true;
		}
		else
		{
			return longOrderList[id]->isFinished();
		}
	}

	void terminateLongOrder(uint8_t id)
	{
		if (longOrderList[id] == NULL)
		{
			Log::critical(id, "Ordre inconnu");
		}
		else
		{
			std::vector<uint8_t> out;
			longOrderList[id]->terminate(out);
		}
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
				return millis() - timestamp < 20 * TIMEOUT;
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
				data.clear();
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
				Log::warning("END_ORDER reçu pour une conversation deja terminee.");
			}
		}
		else
		{
			Log::critical(10, frame, "Type incorrect");
		}
	}

	void sendFrame(const Frame & frame)
	{
		//Serial.printf("[%u]__SendFrame__", millis());
		//Serial.println(frame);
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

