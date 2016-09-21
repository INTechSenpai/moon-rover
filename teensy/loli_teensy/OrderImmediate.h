// OrderImmediate.h

#ifndef _ORDERIMMEDIATE_h
#define _ORDERIMMEDIATE_h

#include <vector>
#include "Singleton.h"

class OrderImmediate
{
public:
	OrderImmediate(){}

	/*
		Méthode exécutant l'ordre immédiat.
		L'argument correspond à la fois à l'input et à l'output de l'odre, il sera modifié par la méthode.
	*/
	virtual void execute(std::vector<uint8_t> &) = 0;
};


// ### Définition des ordres à réponse immédiate ###

class Rien : public OrderImmediate, public Singleton<Rien>
{
public:
	Rien(){}
	virtual void execute(std::vector<uint8_t> & io){}
};


class Ping : public OrderImmediate, public Singleton<Ping>
{
public:
	Ping(){}

	virtual void execute(std::vector<uint8_t> & io)
	{
		Serial.print("Ping !");
		io.clear();
	}
};


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
	######################
	##   Ordres ASCII   ##
	######################
*/

class Logon : public OrderImmediate, public Singleton<Logon>
{
public:
	Logon() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Logoff : public OrderImmediate, public Singleton<Logoff>
{
public:
	Logoff() {}
	virtual void execute(std::vector<uint8_t> & io) {}
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
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Save : public OrderImmediate, public Singleton<Save>
{
public:
	Save() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Display : public OrderImmediate, public Singleton<Display>
{
public:
	Display() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Pos : public OrderImmediate, public Singleton<Pos>
{
public:
	Pos() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class PosX : public OrderImmediate, public Singleton<PosX>
{
public:
	PosX() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class PosY : public OrderImmediate, public Singleton<PosY>
{
public:
	PosY() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class PosO : public OrderImmediate, public Singleton<PosO>
{
public:
	PosO() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Rp : public OrderImmediate, public Singleton<Rp>
{
public:
	Rp() {}
	virtual void execute(std::vector<uint8_t> & io) {}
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
	virtual void execute(std::vector<uint8_t> & io) {}
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
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Pid_kp : public OrderImmediate, public Singleton<Pid_kp>
{
public:
	Pid_kp() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Pid_ki : public OrderImmediate, public Singleton<Pid_ki>
{
public:
	Pid_ki() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Pid_kd : public OrderImmediate, public Singleton<Pid_kd>
{
public:
	Pid_kd() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Smgre : public OrderImmediate, public Singleton<Smgre>
{
public:
	Smgre() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Smgrt : public OrderImmediate, public Singleton<Smgrt>
{
public:
	Smgrt() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Bmgrs : public OrderImmediate, public Singleton<Bmgrs>
{
public:
	Bmgrs() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Bmgrt : public OrderImmediate, public Singleton<Bmgrt>
{
public:
	Bmgrt() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Mms : public OrderImmediate, public Singleton<Mms>
{
public:
	Mms() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Macc : public OrderImmediate, public Singleton<Macc>
{
public:
	Macc() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Control_p : public OrderImmediate, public Singleton<Control_p>
{
public:
	Control_p() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Control_vg : public OrderImmediate, public Singleton<Control_vg>
{
public:
	Control_vg() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Control_vd : public OrderImmediate, public Singleton<Control_vd>
{
public:
	Control_vd() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Control_pwm : public OrderImmediate, public Singleton<Control_pwm>
{
public:
	Control_pwm() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

class Capt : public OrderImmediate, public Singleton<Capt>
{
public:
	Capt() {}
	virtual void execute(std::vector<uint8_t> & io) {}
};

#endif

