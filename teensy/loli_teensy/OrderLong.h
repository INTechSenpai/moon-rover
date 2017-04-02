// OrderLong.h

#ifndef _ORDERLONG_h
#define _ORDERLONG_h

#include <vector>
#include "Singleton.h"
#include "MotionControlSystem.h"
#include "SensorMgr.h"
#include "ActuatorMgr.h"
#include "pin_mapping.h"
#include "Vutils.h"
#include "StreamMgr.h"
#include "StartupMgr.h"
#include "BatterySensor.h"

class OrderLong
{
public:
	OrderLong():
		finished(true), 
		motionControlSystem(MotionControlSystem::Instance()),
		sensorMgr(SensorMgr::Instance()),
		actuatorMgr(ActuatorMgr::Instance()),
		directionControler(DirectionController::Instance()),
		startupMgr(StartupMgr::Instance()),
		batterySensor(BatterySensor::Instance())
	{}

	void launch(const std::vector<uint8_t> & arg)
	{
		finished = false;
		_launch(arg);
	}

	/* Lancement de l'ordre long. L'argument correspond � un input (NEW_ORDER). */
	virtual void _launch(const std::vector<uint8_t> &) = 0;

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
	MotionControlSystem & motionControlSystem;
	SensorMgr & sensorMgr;
	ActuatorMgr & actuatorMgr;
	DirectionController & directionControler;
	StartupMgr & startupMgr;
	BatterySensor & batterySensor;
};


// ### D�finition des ordres longs ###

class RienL : public OrderLong, public Singleton<RienL>
{
public:
	RienL(){}
	void _launch(const std::vector<uint8_t> & input)
	{}
	void onExecute(std::vector<uint8_t> & output)
	{}
	void terminate(std::vector<uint8_t> & output)
	{}
};


/*
	Suit la trajectoire courante. Se termine
	une fois arriv� au prochain point d�arr�t
	de la trajectoire, lorsque le robot est �
	l�arr�t. Ou bien si � Stop � est appel�
*/
class FollowTrajectory : public OrderLong, public Singleton<FollowTrajectory>
{
public:
	FollowTrajectory() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		if (input.size() != 2)
		{// Nombre d'octets re�us incorrect
			Log::critical(40, "FollowTrajectory: argument incorrect");
		}
		else
		{
			int16_t maxSpeed = (input.at(0) << 8) + input.at(1);
			Serial.printf("FollowTraj: %d\n", maxSpeed);
			motionControlSystem.setMaxMovingSpeed(maxSpeed);
			motionControlSystem.gotoNextStopPoint();
		}
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		MotionControlSystem::MovingState movingState = motionControlSystem.getMovingState();
		switch (movingState)
		{
		case MotionControlSystem::STOPPED:
			endMoveStatus = ARRIVED;
			finished = true;
			break;
		case MotionControlSystem::MOVE_INIT:
			break;
		case MotionControlSystem::MOVING:
			break;
		case MotionControlSystem::EXT_BLOCKED:
			endMoveStatus = EXT_BLOCKED;
			finished = true;
			break;
		case MotionControlSystem::INT_BLOCKED:
			endMoveStatus = INT_BLOCKED;
			finished = true;
			break;
		case MotionControlSystem::EMPTY_TRAJ:
			endMoveStatus = NO_MORE_POINTS;
			finished = true;
			break;
		default:
			break;
		}
	}
	void terminate(std::vector<uint8_t> & output)
	{
		output.push_back(endMoveStatus);
	}

private:
	enum EndMoveStatus
	{
		ARRIVED = 0x00,
		EXT_BLOCKED = 0x01,
		INT_BLOCKED = 0x02,
		NO_MORE_POINTS = 0x03
	};
	uint8_t endMoveStatus;
};


/*
	Interromps la trajectoire courante au
	plus vite. La trajectoire est oubli�e. Se
	termine quand le robot est � l�arr�t.
*/
class Stop : public OrderLong, public Singleton<Stop>
{
public:
	Stop() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		motionControlSystem.highLevelStop();
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		finished = motionControlSystem.isStopped();
	}
	void terminate(std::vector<uint8_t> & output)
	{}
};


/*
	Se termine lorsque le jumper est retir�
	du robot
*/
class WaitForJumper : public OrderLong, public Singleton<WaitForJumper>
{
public:
	WaitForJumper() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		jumperInPlace = false;
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		if (analogRead(PIN_GET_JUMPER) > 500 && startupMgr.isReady())
		{// Jumper en place
			jumperInPlace = true;
		}
		else
		{// Jumper sorti
			if (jumperInPlace)
			{
				finished = true;
			}
		}
	}
	void terminate(std::vector<uint8_t> & output)
	{}
private:
	bool jumperInPlace;
};


/*
	Se termine au bout de 90 secondes si
	tout se passe bien. Ou bien avant en cas
	de probl�me grave
*/
class StartMatchChrono : public OrderLong, public Singleton<StartMatchChrono>
{
public:
	StartMatchChrono() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		beginTime = millis();
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		if (millis() - beginTime > 90000)
		{
			returnStatement = 0x00; // MATCH_FINISHED
			finished = true;
		}
	}
	void terminate(std::vector<uint8_t> & output)
	{
		output.push_back(returnStatement);
	}
private:
	uint32_t beginTime;
	uint8_t returnStatement;
};


/*
	Envoie la position du robot et/ou l��tat
	des capteurs avec les fr�quences
	demand�es
*/
class StreamAll : public OrderLong, public Singleton<StreamAll>
{
public:
	StreamAll():
		streamMgr(StreamMgr::Instance()) {}
	void _launch(const std::vector<uint8_t> & input)
	{
		if (input.size() != 3)
		{// Nombre d'octets re�us incorrect
			Log::critical(40, "StreamAll: argument incorrect (launched anyway)");
			// On utilise des valeurs par d�faut
			sendPeriod = 1000;
			sensorsPrescaler = 0;
		}
		else
		{
			sendPeriod = (input.at(0) << 8) + input.at(1);
			sensorsPrescaler = input.at(2);
		}
		lastUpdateTime = millis();
		prescalerCounter = 1;
		streamMgr.running = true;
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		if (streamMgr.running)
		{
			if (millis() - lastUpdateTime >= sendPeriod)
			{
				motionControlSystem.getPosition(currentPosition);
				output = currentPosition.getVector();
				output.push_back(motionControlSystem.getTrajectoryIndex());
				if (sensorsPrescaler != 0)
				{
					if (prescalerCounter == sensorsPrescaler)
					{
						output.push_back((uint8_t)directionControler.getLeftAngle()); // uint16_t tronqu� en uint8_t car la valeur ne d�passe jamais 206
						output.push_back((uint8_t)directionControler.getRightAngle()); // idem
						std::vector<uint8_t> sensorValues = sensorMgr.getValues();
						output.insert(output.end(), sensorValues.begin(), sensorValues.end());
						prescalerCounter = 1;
					}
					else
					{
						prescalerCounter++;
					}
				}
				lastUpdateTime = millis();
			}
		}
		else
		{
			finished = true;
		}
	}
	void terminate(std::vector<uint8_t> & output)
	{}
private:
	Position currentPosition;
	uint32_t lastUpdateTime;
	uint16_t sendPeriod;
	uint8_t sensorsPrescaler;
	uint8_t prescalerCounter;
	StreamMgr & streamMgr;
};


/*
	Abaisse le filet � l�horizontale
*/
class PullDownNet : public OrderLong, public Singleton<PullDownNet>
{
public:
	PullDownNet() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		actuatorMgr.pullDownNet(true);
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		finished = actuatorMgr.pullDownNet(false);
	}
	void terminate(std::vector<uint8_t> & output)
	{}
};


/*
	Abaisse le filet � mi-chemin. Afin de
	pouvoir remplir un filet d�j� en partie
	rempli sans perdre de balles
*/
class PutNetHalfway : public OrderLong, public Singleton<PutNetHalfway>
{
public:
	PutNetHalfway() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		actuatorMgr.putNetHalfway(true);
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		finished = actuatorMgr.putNetHalfway(false);
	}
	void terminate(std::vector<uint8_t> & output)
	{}
};


/*
	Remonte le filet � la verticale.
*/
class PullUpNet : public OrderLong, public Singleton<PullUpNet>
{
public:
	PullUpNet() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		actuatorMgr.pullUpNet(true);
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		finished = actuatorMgr.pullUpNet(false);
	}
	void terminate(std::vector<uint8_t> & output)
	{}
};


/*
	Ouvre les mailles du filet pour pouvoir
	accueillir les balles.
*/
class OpenNet : public OrderLong, public Singleton<OpenNet>
{
public:
	OpenNet() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		actuatorMgr.openNet(true);
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		finished = actuatorMgr.openNet(false);
	}
	void terminate(std::vector<uint8_t> & output)
	{}
};


/*
	Ferme les mailles du filet.
*/
class CloseNet : public OrderLong, public Singleton<CloseNet>
{
public:
	CloseNet() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		actuatorMgr.closeNet(true);
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		finished = actuatorMgr.closeNet(false);
	}
	void terminate(std::vector<uint8_t> & output)
	{}
};


/*
	Actionne le filet de mani�re � pouvoir
	traverser la bascule de la zone de d�part
	en marche arri�re comme si de rien �tait.
	Entre en CONFLIT avec tous les autres
	ordres actionnant le filet
*/
class CrossFlipFlop : public OrderLong, public Singleton<CrossFlipFlop>
{
public:
	CrossFlipFlop() {}
	void _launch(const std::vector<uint8_t> & input)
	{}
	void onExecute(std::vector<uint8_t> & output)
	{
		//todo
	}
	void terminate(std::vector<uint8_t> & output)
	{
		//todo
	}
};


/*
	Vide le filet par le c�t� gauche (du point
	de vue du robot)
*/
class EjectLeftSide : public OrderLong, public Singleton<EjectLeftSide>
{
public:
	EjectLeftSide() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		actuatorMgr.ejectLeftSide(true);
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		retStatus = actuatorMgr.ejectLeftSide(false);
		finished = retStatus != 0xFF;
	}
	void terminate(std::vector<uint8_t> & output)
	{
		output.push_back(retStatus);
	}
private:
	uint8_t retStatus;
};


/*
	Range le bras gauche permettant de
	vider le filet.
*/
class RearmLeftSide : public OrderLong, public Singleton<RearmLeftSide>
{
public:
	RearmLeftSide() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		actuatorMgr.rearmLeftSide(true);
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		retStatus = actuatorMgr.rearmLeftSide(false);
		finished = retStatus != 0xFF;
	}
	void terminate(std::vector<uint8_t> & output)
	{
		output.push_back(retStatus);
	}
private:
	uint8_t retStatus;
};


/*
	Vide le filet par le c�t� droit (du point
	de vue du robot).
*/
class EjectRightSide : public OrderLong, public Singleton<EjectRightSide>
{
public:
	EjectRightSide() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		actuatorMgr.ejectRightSide(true);
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		retStatus = actuatorMgr.ejectRightSide(false);
		finished = retStatus != 0xFF;
	}
	void terminate(std::vector<uint8_t> & output)
	{
		output.push_back(retStatus);
	}
private:
	uint8_t retStatus;
};


/*
	Range le bras droit permettant de vider
	le filet.
*/
class RearmRightSide : public OrderLong, public Singleton<RearmRightSide>
{
public:
	RearmRightSide() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		actuatorMgr.rearmRightSide(true);
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		retStatus = actuatorMgr.rearmRightSide(false);
		finished = retStatus != 0xFF;
	}
	void terminate(std::vector<uint8_t> & output)
	{
		output.push_back(retStatus);
	}
private:
	uint8_t retStatus;
};


/*
	Fait tourner le ventilateur et lib�re le projectile.
*/
class FunnyAction : public OrderLong, public Singleton<FunnyAction>
{
public:
	FunnyAction() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		Serial.println("Start funny action");
		actuatorMgr.funnyAction(true);
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		finished = actuatorMgr.funnyAction(false);
	}
	void terminate(std::vector<uint8_t> & output)
	{
		Serial.println("End funny action");
	}
};



/*
	######################
	##   Ordres ASCII   ##
	######################
*/

class Test_pwm : public OrderLong, public Singleton<Test_pwm>
{
public:
	Test_pwm() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		motionControlSystem.enablePositionControl(false);
		motionControlSystem.enableLeftSpeedControl(false);
		motionControlSystem.enableRightSpeedControl(false);
		motionControlSystem.enablePwmControl(true);
		int arg = Vutils<ARG_SIZE>::vtof(input);
		motionControlSystem.setPWM(arg);
		beginTime = millis();
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		finished = millis() - beginTime > 2000;
	}
	void terminate(std::vector<uint8_t> & output)
	{
		motionControlSystem.setPWM(0);
	}
private:
	uint32_t beginTime;
};

class Test_speed : public OrderLong, public Singleton<Test_speed>
{
public:
	Test_speed() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		motionControlSystem.enablePositionControl(false);
		motionControlSystem.enableLeftSpeedControl(true);
		motionControlSystem.enableRightSpeedControl(true);
		motionControlSystem.enablePwmControl(true);
		int arg = Vutils<ARG_SIZE>::vtof(input);
		motionControlSystem.setSpeed(arg);
		beginTime = millis();
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		finished = millis() - beginTime > 2000;
	}
	void terminate(std::vector<uint8_t> & output)
	{
		motionControlSystem.setSpeed(0);
	}
private:
	uint32_t beginTime;
};

class Test_pos : public OrderLong, public Singleton<Test_pos>
{
public:
	void _launch(const std::vector<uint8_t> & input)
	{
		motionControlSystem.enablePositionControl(true);
		motionControlSystem.enableLeftSpeedControl(true);
		motionControlSystem.enableRightSpeedControl(true);
		motionControlSystem.enablePwmControl(true);
		int arg = Vutils<ARG_SIZE>::vtof(input);
		motionControlSystem.setTranslation(arg);
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		finished = motionControlSystem.isStopped();
	}
	void terminate(std::vector<uint8_t> & output)
	{
	}
};


class FollowTrajectory_ascii : public OrderLong, public Singleton<FollowTrajectory_ascii>
{
public:
	FollowTrajectory_ascii() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		Serial.println("FollowTrajectory");
		int arg = Vutils<ARG_SIZE>::vtoi(input);
		motionControlSystem.setMaxMovingSpeed(arg);
		Serial.println(motionControlSystem.getMaxMovingSpeed());
		motionControlSystem.gotoNextStopPoint();
		Serial.println("launched");
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		MotionControlSystem::MovingState movingState = motionControlSystem.getMovingState();
		switch (movingState)
		{
		case MotionControlSystem::STOPPED:
			Serial.println("STOPPED");
			endMoveStatus = ARRIVED;
			finished = true;
			break;
		case MotionControlSystem::MOVE_INIT:
			//Serial.println("MOVE_INIT");
			break;
		case MotionControlSystem::MOVING:
			//Serial.println("MOVING");
			break;
		case MotionControlSystem::EXT_BLOCKED:
			Serial.println("EXT_BLOCKED");
			endMoveStatus = EXT_BLOCKED;
			finished = true;
			break;
		case MotionControlSystem::INT_BLOCKED:
			Serial.println("INT_BLOCKED");
			endMoveStatus = INT_BLOCKED;
			finished = true;
			break;
		case MotionControlSystem::EMPTY_TRAJ:
			Serial.println("EMPTY_TRAJ");
			endMoveStatus = NO_MORE_POINTS;
			finished = true;
			break;
		default:
			break;
		}
	}
	void terminate(std::vector<uint8_t> & output)
	{
		output.push_back(endMoveStatus);
	}

private:
	enum EndMoveStatus
	{
		ARRIVED = 0x00,
		EXT_BLOCKED = 0x01,
		INT_BLOCKED = 0x02,
		NO_MORE_POINTS = 0x03
	};
	uint8_t endMoveStatus;
};


class TestAX12 : public OrderLong, public Singleton<TestAX12>
{
public:
	TestAX12() : directionController(DirectionController::Instance()) {}
	void _launch(const std::vector<uint8_t> & input)
	{
		c = 0;
		lastUpdate = millis();
		t = 10;
		delta = 10 * (float)t / 1000; // m^-1 / seconde
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		if (millis() - lastUpdate > t)
		{
			c += delta;
			directionController.setAimCurvature(c);
			lastUpdate = millis();
			if (c >= 3)
			{
				finished = true;
			}
		}
	}
	void terminate(std::vector<uint8_t> & output)
	{}

private:
	float c;
	uint32_t lastUpdate;
	uint32_t t;
	float delta;
	DirectionController & directionController;
};


#endif

