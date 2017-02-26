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

class OrderLong
{
public:
	OrderLong():
		finished(true), 
		motionControlSystem(MotionControlSystem::Instance()),
		sensorMgr(SensorMgr::Instance()),
		actuatorMgr(ActuatorMgr::Instance())
	{}

	void launch(const std::vector<uint8_t> & arg)
	{
		finished = false;
		_launch(arg);
	}

	/* Lancement de l'ordre long. L'argument correspond à un input (NEW_ORDER). */
	virtual void _launch(const std::vector<uint8_t> &) = 0;

	/* Méthode exécutée en boucle durant l'exécution de l'odre. L'argument est un output, si il est non vide cela correspond à un STATUS_UPDATE. */
	virtual void onExecute(std::vector<uint8_t> &) = 0;
	
	/* Méthode indiquant si l'odre long a fini son exécution ou non. */
	bool isFinished()
	{
		return finished;
	}

	/* Méthode à appeler une fois que l'odre est ternminé. L'argument est un output, il correspond au contenu du EXECUTION_END. */
	virtual void terminate(std::vector<uint8_t> &) = 0;

protected:
	bool finished;
	MotionControlSystem & motionControlSystem;
	SensorMgr & sensorMgr;
	ActuatorMgr & actuatorMgr;
};


// ### Définition des ordres longs ###

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
	une fois arrivé au prochain point d’arrêt
	de la trajectoire, lorsque le robot est à
	l’arrêt. Ou bien si « Stop » est appelé
*/
class FollowTrajectory : public OrderLong, public Singleton<FollowTrajectory>
{
public:
	FollowTrajectory() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		if (input.size() != 2)
		{// Nombre d'octets reçus incorrect
			Log::critical(40, "FollowTrajectory: argument incorrect");
		}
		else
		{
			int16_t maxSpeed = (input.at(0) << 8) + input.at(1);
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
	plus vite. La trajectoire est oubliée. Se
	termine quand le robot est à l’arrêt.
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
	Se termine lorsque le jumper est retiré
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
		if (analogRead(PIN_GET_JUMPER) > 500)
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
	de problème grave
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
		else if (false) // TODO : check batterie
		{
			returnStatement = 0x01; // EMERGENCY_STOP
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
	Envoie la position du robot et/ou l’état
	des capteurs avec les fréquences
	demandées
*/
class StreamAll : public OrderLong, public Singleton<StreamAll>
{
public:
	StreamAll() {}
	void _launch(const std::vector<uint8_t> & input)
	{
		if (input.size() != 3)
		{// Nombre d'octets reçus incorrect
			Log::critical(40, "StreamAll: argument incorrect");
			// On utilise des valeurs par défaut
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
	}
	void onExecute(std::vector<uint8_t> & output)
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
	void terminate(std::vector<uint8_t> & output)
	{}
private:
	Position currentPosition;
	uint32_t lastUpdateTime;
	uint16_t sendPeriod;
	uint8_t sensorsPrescaler;
	uint8_t prescalerCounter;
};


/*
	Abaisse le filet à l’horizontale
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
	Abaisse le filet à mi-chemin. Afin de
	pouvoir remplir un filet déjà en partie
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
	Remonte le filet à la verticale.
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
		Serial.println("closing");
	}
	void onExecute(std::vector<uint8_t> & output)
	{
		finished = actuatorMgr.closeNet(false);
	}
	void terminate(std::vector<uint8_t> & output)
	{
		Serial.println("closed");
	}
};


/*
	Actionne le filet de manière à pouvoir
	traverser la bascule de la zone de départ
	en marche arrière comme si de rien était.
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
	Vide le filet par le côté gauche (du point
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
	Vide le filet par le côté droit (du point
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


#endif

