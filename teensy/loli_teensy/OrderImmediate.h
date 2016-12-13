// OrderImmediate.h

#ifndef _ORDERIMMEDIATE_h
#define _ORDERIMMEDIATE_h

#include <vector>
#include "Singleton.h"
#include "Vutils.h"
#include "AsciiOrderListener.h"
#include "Log.h"
#include "MotionControlSystem.h"
#include "DirectionController.h"
#include "SensorMgr.h"
#include "Position.h"

class OrderImmediate
{
public:
	OrderImmediate() :
		motionControlSystem(MotionControlSystem::Instance()),
		directionController(DirectionController::Instance()),
		sensorMgr(SensorMgr::Instance())
	{}

	/*
		Méthode exécutant l'ordre immédiat.
		L'argument correspond à la fois à l'input et à l'output de l'odre, il sera modifié par la méthode.
	*/
	virtual void execute(std::vector<uint8_t> &) = 0;

protected:
	MotionControlSystem & motionControlSystem;
	DirectionController & directionController;
	SensorMgr & sensorMgr;
};


// ### Définition des ordres à réponse immédiate ###

class Rien : public OrderImmediate, public Singleton<Rien>
{
public:
	Rien(){}
	virtual void execute(std::vector<uint8_t> & io){}
};


/*
	Ne fait rien
*/
class Ping : public OrderImmediate, public Singleton<Ping>
{
public:
	Ping(){}

	virtual void execute(std::vector<uint8_t> & io)
	{
		Serial.println("Ping !");
		io.clear();
	}
};


/*
	Le bas niveau renvoie la couleur s’il la
	connait.
*/
class GetColor : public OrderImmediate, public Singleton<GetColor>
{
public:
	GetColor() {}
	virtual void execute(std::vector<uint8_t> & io) 
	{
		enum Side
		{
			INTECH = 0x00,
			WINDOW = 0x01,
			UNKNOWN = 0x02
		};

		io.clear();
		
		// DEBUG
		static bool called = false;
		static uint32_t t;
		if (!called)
		{
			t = millis();
			called = true;
		}
		if (millis() - t > 2000)
			io.push_back(INTECH);
		else
			io.push_back(UNKNOWN);
	}
};


/*
	Ajoute à la trajectoire courante un
	certain nombre de points (entre 0 et
	31 points)
*/
class AddTrajectoryPoints : public OrderImmediate, public Singleton<AddTrajectoryPoints>
{
public:
	AddTrajectoryPoints() {}
	virtual void execute(std::vector<uint8_t> & io) 
	{
		if (io.size() <= 1 || (io.size() - 1) % 7 != 0)
		{ // Nombre d'octets reçus incorrect
			Log::critical(40, "AddTrajectoryPoint: argument incorrect");
		}
		else
		{
			uint8_t index = io.at(0);
			for (size_t i = 1; i < io.size(); i += 7)
			{
				std::vector<uint8_t> pos_vect(io.begin() + i, io.begin() + i + 5);
				Position pos(pos_vect);
				TrajectoryPoint newTrajPoint(pos, io.at(5), io.at(6));
				motionControlSystem.addTrajectoryPoint(newTrajPoint, index);
				index++;
			}
		}
		io.clear();
	}
};


/*
	Règle la vitesse maximale courante.
*/
class SetMaxSpeed : public OrderImmediate, public Singleton<SetMaxSpeed>
{
public:
	SetMaxSpeed() {}
	virtual void execute(std::vector<uint8_t> & io) 
	{
		if (io.size() != 2)
		{// Nombre d'octets reçus incorrect
			Log::critical(40, "SetMaxSpeed: argument incorrect");
		}
		else
		{
			int16_t maxSpeed = (io.at(0) << 8) + io.at(1);
			motionControlSystem.setMaxMovingSpeed(maxSpeed);
		}
		io.clear();
	}
};


/*
	Modifie la position du robot dans le
	bas niveau, les coordonnées fournies
	sont ajoutées aux coordonnées
	courantes du bas niveau.
*/
class EditPosition : public OrderImmediate, public Singleton<EditPosition>
{
public:
	EditPosition() {}
	virtual void execute(std::vector<uint8_t> & io) 
	{
		Position correctifPosition(io);
		Position currentPosition;
		motionControlSystem.getPosition(currentPosition);
		currentPosition.x += correctifPosition.x;
		currentPosition.y += correctifPosition.y;
		currentPosition.setOrientation(currentPosition.orientation + correctifPosition.orientation);
		motionControlSystem.setPosition(currentPosition);
		io.clear();
	}
};


/*
	######################
	##   Ordres ASCII   ##
	######################
*/

class Logon : public OrderImmediate, public Singleton<Logon>
{
public:
	Logon() {}
	virtual void execute(std::vector<uint8_t> & io) {
		Log::enableChannel((Log::LogChannel)Vutils<ARG_SIZE>::vtoi(io), true);
	}
};

class Logoff : public OrderImmediate, public Singleton<Logoff>
{
public:
	Logoff() {}
	virtual void execute(std::vector<uint8_t> & io) {
		Log::enableChannel((Log::LogChannel)Vutils<ARG_SIZE>::vtoi(io), false);
	}
};

class Batt : public OrderImmediate, public Singleton<Batt>
{
public:
	Batt() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Stop_ascii : public OrderImmediate, public Singleton<Stop_ascii>
{
public:
	Stop_ascii() {}
	virtual void execute(std::vector<uint8_t> & io) {
		motionControlSystem.stop();
	}
};

class Save : public OrderImmediate, public Singleton<Save>
{
public:
	Save() {}
	virtual void execute(std::vector<uint8_t> & io) {
		motionControlSystem.saveParameters();
		Serial.println("Current parameters saved to EEPROM");
	}
};

class Display : public OrderImmediate, public Singleton<Display>
{
public:
	Display() {}
	virtual void execute(std::vector<uint8_t> & io) {
		float vg_kp, vg_ki, vg_kd;
		float vd_kp, vd_ki, vd_kd;
		float tr_kp, tr_ki, tr_kd;
		float k1, k2;
		uint32_t smgre, smgrt;
		float bmgrs;
		uint32_t bmgrt;
		int32_t mms, macc;
		bool cp, cvg, cvd, cpwm;
		char pidToSet_str[12];
		motionControlSystem.getPIDtoSet_str(pidToSet_str, 12);

		motionControlSystem.getLeftSpeedTunings(vg_kp, vg_ki, vg_kd);
		motionControlSystem.getRightSpeedTunings(vd_kp, vd_ki, vd_kd);
		motionControlSystem.getTranslationTunings(tr_kp, tr_ki, tr_kd);
		motionControlSystem.getTrajectoryTunings(k1, k2);
		motionControlSystem.getEndOfMoveMgrTunings(smgre, smgrt);
		motionControlSystem.getLeftMotorBmgrTunings(bmgrs, bmgrt);
		mms = motionControlSystem.getMaxMovingSpeed();
		macc = motionControlSystem.getMaxAcceleration();
		motionControlSystem.getEnableStates(cp, cvg, cvd, cpwm);

		Serial.print("PID to set : ");
		Serial.println(pidToSet_str);
		Serial.println("\tkp\tki\tkd");
		Serial.printf("V_g\t%g\t%g\t%g\n", vg_kp, vg_ki, vg_kd);
		Serial.printf("V_d\t%g\t%g\t%g\n", vd_kp, vd_ki, vd_kd);
		Serial.printf("Tr \t%g\t%g\t%g\n", tr_kp, tr_ki, tr_kd);
		Serial.println();
		Serial.printf("Curvature K1= %g\n", k1);
		Serial.printf("Curvature K2= %g\n", k2);
		Serial.println();
		Serial.printf("StopMgr epsilon= %d\tresponseTime= %d\n", smgre, smgrt);
		Serial.printf("BlockMgr sensibility= %g\tresponseTime= %d\n", bmgrs, bmgrt);
		Serial.println();
		Serial.printf("MaxMovingSpeed= %d\n", mms);
		Serial.printf("MaxAcceleration= %d\n", macc);
		Serial.println();
		Serial.printf("ControlPosition[%d]\n", cp);
		Serial.printf("ControlLeftSpeed[%d]\n", cvg);
		Serial.printf("ControlRightSpeed[%d]\n", cvd);
		Serial.printf("ControlPWM[%d]\n", cpwm);
		Serial.println();
	}
};

class Default : public OrderImmediate, public Singleton<Default>
{
public:
	Default() {}
	virtual void execute(std::vector<uint8_t> & io) {
		motionControlSystem.stop();
		motionControlSystem.loadDefaultParameters();
		Serial.println("Default parameters restored");
	}
};

class Pos : public OrderImmediate, public Singleton<Pos>
{
public:
	Pos() {}
	virtual void execute(std::vector<uint8_t> & io) {
		Position position;
		motionControlSystem.getPosition(position);
		Serial.printf("x= %g\ty= %g\t o= %g\n", position.x, position.y, position.orientation);
	}
};

class PosX : public OrderImmediate, public Singleton<PosX>
{
public:
	PosX() {}
	virtual void execute(std::vector<uint8_t> & io) {
		Position position;
		motionControlSystem.getPosition(position);
		if (io.size() > 0)
		{
			float arg = Vutils<ARG_SIZE>::vtof(io);
			position.x = arg;
			motionControlSystem.setPosition(position);
		}
		Serial.printf("x= %g\n", position.x);
	}
};

class PosY : public OrderImmediate, public Singleton<PosY>
{
public:
	PosY() {}
	virtual void execute(std::vector<uint8_t> & io) {
		Position position;
		motionControlSystem.getPosition(position);
		if (io.size() > 0)
		{
			float arg = Vutils<ARG_SIZE>::vtof(io);
			position.y = arg;
			motionControlSystem.setPosition(position);
		}
		Serial.printf("y= %g\n", position.y);
	}
};

class PosO : public OrderImmediate, public Singleton<PosO>
{
public:
	PosO() {}
	virtual void execute(std::vector<uint8_t> & io) {
		Position position;
		motionControlSystem.getPosition(position);
		if (io.size() > 0)
		{
			float arg = Vutils<ARG_SIZE>::vtof(io);
			position.orientation = arg;
			motionControlSystem.setPosition(position);
		}
		Serial.printf("o= %g\n", position.orientation);
	}
};

class Rp : public OrderImmediate, public Singleton<Rp>
{
public:
	Rp() {}
	virtual void execute(std::vector<uint8_t> & io) {
		char arg[ARG_SIZE];
		Vutils<ARG_SIZE>::vtostr(io, arg);
		if (strcmp(arg, "") == 0)
			motionControlSystem.resetPosition();
		else if (strcmp(arg, "i") == 0)
			motionControlSystem.setPosition(Position(0, 0, 0)); // TODO: position coté INTech
		else if (strcmp(arg, "w") == 0)
			motionControlSystem.setPosition(Position(0, 0, 0)); // TODO : position côté fenêtre
		else
			Log::warning("[rp] Argument incorrect");
		Position position;
		motionControlSystem.getPosition(position);
		Serial.printf("x= %g\ty= %g\t o= %g\n", position.x, position.y, position.orientation);
	}
};

class Dir : public OrderImmediate, public Singleton<Dir>
{
public:
	Dir() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Axg : public OrderImmediate, public Singleton<Axg>
{
public:
	Axg() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Axd : public OrderImmediate, public Singleton<Axd>
{
public:
	Axd() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Cod : public OrderImmediate, public Singleton<Cod>
{
public:
	Cod() {}
	virtual void execute(std::vector<uint8_t> & io) {
		int32_t avG, avD, arG, arD;
		motionControlSystem.getTicks(avG, avD, arG, arD);
		Serial.printf("avG= %d\tavD= %d\tarG= %d\tarD=%d\n", avG, avD, arG, arD);
	}
};

class Setaxid : public OrderImmediate, public Singleton<Setaxid>
{
public:
	Setaxid() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Pid_c : public OrderImmediate, public Singleton<Pid_c>
{
public:
	Pid_c() {}
	virtual void execute(std::vector<uint8_t> & io) {
		char arg[ARG_SIZE];
		Vutils<ARG_SIZE>::vtostr(io, arg);
		if (strcmp(arg, "g") == 0)
			motionControlSystem.setPIDtoSet(MotionControlSystem::LEFT_SPEED);
		else if (strcmp(arg, "d") == 0)
			motionControlSystem.setPIDtoSet(MotionControlSystem::RIGHT_SPEED);
		else if (strcmp(arg, "v") == 0)
			motionControlSystem.setPIDtoSet(MotionControlSystem::SPEED);
		else if (strcmp(arg, "t") == 0)
			motionControlSystem.setPIDtoSet(MotionControlSystem::TRANSLATION);
		else if (strcmp(arg, "") != 0)
			Log::warning("[pid] Argument incorrect");
		char str[12];
		motionControlSystem.getPIDtoSet_str(str, 12);
		Serial.print("Current PID to set : ");
		Serial.println(str);
	}
};

class Pid_kp : public OrderImmediate, public Singleton<Pid_kp>
{
public:
	Pid_kp() {}
	virtual void execute(std::vector<uint8_t> & io) {
		float kp, ki, kd;
		motionControlSystem.getCurrentPIDTunings(kp, ki, kd);
		if (io.size() > 0)
		{
			float arg = Vutils<ARG_SIZE>::vtof(io);
			motionControlSystem.setCurrentPIDTunings(arg, ki, kd);
		}
		motionControlSystem.getCurrentPIDTunings(kp, ki, kd);
		char pidName[12];
		motionControlSystem.getPIDtoSet_str(pidName, 12);
		Serial.print(pidName);
		Serial.printf(" : kp= %g\n", kp);
	}
};

class Pid_ki : public OrderImmediate, public Singleton<Pid_ki>
{
public:
	Pid_ki() {}
	virtual void execute(std::vector<uint8_t> & io) {
		float kp, ki, kd;
		motionControlSystem.getCurrentPIDTunings(kp, ki, kd);
		if (io.size() > 0)
		{
			float arg = Vutils<ARG_SIZE>::vtof(io);
			motionControlSystem.setCurrentPIDTunings(kp, arg, kd);
		}
		motionControlSystem.getCurrentPIDTunings(kp, ki, kd);
		char pidName[12];
		motionControlSystem.getPIDtoSet_str(pidName, 12);
		Serial.print(pidName);
		Serial.printf(" : ki= %g\n", ki);
	}
};

class Pid_kd : public OrderImmediate, public Singleton<Pid_kd>
{
public:
	Pid_kd() {}
	virtual void execute(std::vector<uint8_t> & io) {
		float kp, ki, kd;
		motionControlSystem.getCurrentPIDTunings(kp, ki, kd);
		if (io.size() > 0)
		{
			float arg = Vutils<ARG_SIZE>::vtof(io);
			motionControlSystem.setCurrentPIDTunings(kp, ki, arg);
		}
		motionControlSystem.getCurrentPIDTunings(kp, ki, kd);
		char pidName[12];
		motionControlSystem.getPIDtoSet_str(pidName, 12);
		Serial.print(pidName);
		Serial.printf(" : kd= %g\n", kd);
	}
};

class Smgre : public OrderImmediate, public Singleton<Smgre>
{
public:
	Smgre() {}
	virtual void execute(std::vector<uint8_t> & io) {
		uint32_t epsilon, responseTime;
		motionControlSystem.getEndOfMoveMgrTunings(epsilon, responseTime);
		if (io.size() > 0)
		{
			int32_t arg = Vutils<ARG_SIZE>::vtoi(io);
			motionControlSystem.setEndOfMoveMgrTunings(arg, responseTime);
		}
		motionControlSystem.getEndOfMoveMgrTunings(epsilon, responseTime);
		Serial.printf("EndOfMoveMgr : epsilon= %d\n", epsilon);
	}
};

class Smgrt : public OrderImmediate, public Singleton<Smgrt>
{
public:
	Smgrt() {}
	virtual void execute(std::vector<uint8_t> & io) {
		uint32_t epsilon, responseTime;
		motionControlSystem.getEndOfMoveMgrTunings(epsilon, responseTime);
		if (io.size() > 0)
		{
			int32_t arg = Vutils<ARG_SIZE>::vtoi(io);
			motionControlSystem.setEndOfMoveMgrTunings(epsilon, arg);
		}
		motionControlSystem.getEndOfMoveMgrTunings(epsilon, responseTime);
		Serial.printf("EndOfMoveMgr : responseTime= %d\n", responseTime);
	}
};

class Bmgrs : public OrderImmediate, public Singleton<Bmgrs>
{
public:
	Bmgrs() {}
	virtual void execute(std::vector<uint8_t> & io) {
		float sensibility;
		uint32_t responseTime;
		motionControlSystem.getLeftMotorBmgrTunings(sensibility, responseTime);
		if (io.size() > 0)
		{
			float arg = Vutils<ARG_SIZE>::vtof(io);
			motionControlSystem.setLeftMotorBmgrTunings(arg, responseTime);
			motionControlSystem.setRightMotorBmgrTunings(arg, responseTime);
		}
		motionControlSystem.getLeftMotorBmgrTunings(sensibility, responseTime);
		Serial.printf("BlockingMgr : sensibility= %g\n", sensibility);
	}
};

class Bmgrt : public OrderImmediate, public Singleton<Bmgrt>
{
public:
	Bmgrt() {}
	virtual void execute(std::vector<uint8_t> & io) {
		float sensibility;
		uint32_t responseTime;
		motionControlSystem.getLeftMotorBmgrTunings(sensibility, responseTime);
		if (io.size() > 0)
		{
			int32_t arg = Vutils<ARG_SIZE>::vtoi(io);
			motionControlSystem.setLeftMotorBmgrTunings(sensibility, arg);
			motionControlSystem.setRightMotorBmgrTunings(sensibility, arg);
		}
		motionControlSystem.getLeftMotorBmgrTunings(sensibility, responseTime);
		Serial.printf("BlockingMgr : responseTime= %g\n", responseTime);
	}
};

class Mms : public OrderImmediate, public Singleton<Mms>
{
public:
	Mms() {}
	virtual void execute(std::vector<uint8_t> & io) {
		if (io.size() > 0)
		{
			motionControlSystem.setMaxMovingSpeed(Vutils<ARG_SIZE>::vtoi(io));
		}
		Serial.printf("MaxMovingSpeed= %d\n", motionControlSystem.getMaxMovingSpeed());
	}
};

class Macc : public OrderImmediate, public Singleton<Macc>
{
public:
	Macc() {}
	virtual void execute(std::vector<uint8_t> & io) {
		if (io.size() > 0)
		{
			motionControlSystem.setMaxAcceleration(Vutils<ARG_SIZE>::vtoi(io));
		}
		Serial.printf("MaxAcceleration= %d\n", motionControlSystem.getMaxAcceleration());
	}
};

class Control_p : public OrderImmediate, public Singleton<Control_p>
{
public:
	Control_p() {}
	virtual void execute(std::vector<uint8_t> & io) {
		int32_t arg = Vutils<ARG_SIZE>::vtoi(io);
		motionControlSystem.enablePositionControl((bool)arg);
		if ((bool)arg)
			Serial.println("Position control ENABLED");
		else
			Serial.println("Position control DISABLED");
	}
};

class Control_vg : public OrderImmediate, public Singleton<Control_vg>
{
public:
	Control_vg() {}
	virtual void execute(std::vector<uint8_t> & io) {
		int32_t arg = Vutils<ARG_SIZE>::vtoi(io);
		motionControlSystem.enableLeftSpeedControl((bool)arg);
		if ((bool)arg)
			Serial.println("Left speed control ENABLED");
		else
			Serial.println("Left speed control DISABLED");
	}
};

class Control_vd : public OrderImmediate, public Singleton<Control_vd>
{
public:
	Control_vd() {}
	virtual void execute(std::vector<uint8_t> & io) {
		int32_t arg = Vutils<ARG_SIZE>::vtoi(io);
		motionControlSystem.enableRightSpeedControl((bool)arg);
		if ((bool)arg)
			Serial.println("Right speed control ENABLED");
		else
			Serial.println("Right speed control DISABLED");
	}
};

class Control_pwm : public OrderImmediate, public Singleton<Control_pwm>
{
public:
	Control_pwm() {}
	virtual void execute(std::vector<uint8_t> & io) {
		int32_t arg = Vutils<ARG_SIZE>::vtoi(io);
		motionControlSystem.enablePwmControl((bool)arg);
		if ((bool)arg)
			Serial.println("PWM control ENABLED");
		else
			Serial.println("PWM control DISABLED");
	}
};

class Curv_k1 : public OrderImmediate, public Singleton<Curv_k1>
{
public:
	Curv_k1() {}
	virtual void execute(std::vector<uint8_t> & io) {
		float k1, k2;
		motionControlSystem.getTrajectoryTunings(k1, k2);
		if (io.size() > 0)
		{
			float arg = Vutils<ARG_SIZE>::vtof(io);
			motionControlSystem.setTrajectoryTunings(arg, k2);
		}
		motionControlSystem.getTrajectoryTunings(k1, k2);
		Serial.printf("CurvatureCorrector k1= %g\n", k1);
	}
};

class Curv_k2 : public OrderImmediate, public Singleton<Curv_k2>
{
public:
	Curv_k2() {}
	virtual void execute(std::vector<uint8_t> & io) {
		float k1, k2;
		motionControlSystem.getTrajectoryTunings(k1, k2);
		if (io.size() > 0)
		{
			float arg = Vutils<ARG_SIZE>::vtof(io);
			motionControlSystem.setTrajectoryTunings(k1, arg);
		}
		motionControlSystem.getTrajectoryTunings(k1, k2);
		Serial.printf("CurvatureCorrector k2= %g\n", k2);
	}
};

class Capt : public OrderImmediate, public Singleton<Capt>
{
public:
	Capt() {}
	virtual void execute(std::vector<uint8_t> & io)
	{
		uint8_t values[NB_SENSORS];
		sensorMgr.getValues_noReset(values);
		Serial.printf("ToF_LP_AV:%4u  ToF_LP_AR:%4u  IR_AVG:%4u  IR_AVD:%4u\n",
			values[0]*10,
			values[2]*10,
			values[1]*10,
			values[3]*10);
		Serial.printf("AVg:%3u  flanAVg:%3u  flanARg:%3u  ARg:%3u  ARd:%3u  flanARd:%3u  flanAVd:%3u  AVd:%3u\n",
			values[4],
			values[5],
			values[6],
			values[7],
			values[8],
			values[9],
			values[10],
			values[11]);
		Serial.println();
	}
};

class Help : public OrderImmediate, public Singleton<Help>
{
public:
	Help() {}
	virtual void execute(std::vector<uint8_t> & io) 
	{
		Serial.println("---Liste des ordres ASCII (type de l'argument)---");
		Serial.println("logon (int)");
		Serial.println("logoff (int)");
		Serial.println("batt (void)");
		Serial.println("stop (void)");
		Serial.println("s (void)");
		Serial.println("save (void)");
		Serial.println("display (void)");
		Serial.println("default (void)");
		Serial.println("pos (void)");
		Serial.println("x (float)");
		Serial.println("y (float)");
		Serial.println("o (float)");
		Serial.println("rp {'';'i';'w'}");
		Serial.println("dir (float)");
		Serial.println("axg (int)");
		Serial.println("axd (int)");
		Serial.println("cod (void)");
		Serial.println("setaxid (int)");
		Serial.println("pid {'g';'d';'t'}");
		Serial.println("kp (float)");
		Serial.println("ki (float)");
		Serial.println("kd (float)");
		Serial.println("smgre (int)");
		Serial.println("smgrt (int)");
		Serial.println("bmgrs (float)");
		Serial.println("bmgrt (int)");
		Serial.println("mms (int)");
		Serial.println("macc (int)");
		Serial.println("cp (bool)");
		Serial.println("cvg (bool)");
		Serial.println("cvd (bool)");
		Serial.println("cpwm (bool)");
		Serial.println("pwm (int)");
		Serial.println("a (int)");
		Serial.println("p (int)");
		Serial.println("k1 (float)");
		Serial.println("k2 (float)");
		Serial.println("capt (void)");
		Serial.println("abort (void)");
		Serial.println("help (void)");
		Serial.println("---FIN---");
		Serial.println("");
	}
};

#endif

