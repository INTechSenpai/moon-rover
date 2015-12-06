#ifndef EXECUTABLE
#define EXECUTABLE

#include "FreeRTOS.h"
#include "task.h"
#include "Uart.hpp"

class Executable
{
protected:
	static Uart<2> m_serie_rb;
public:
	virtual void execute()=0;
	virtual ~Executable()=0;
	static void setSerie(Uart<2> serie_rb);
};

class Exec_Update_Table : public Executable
{
private:
	uint8_t m_nbElem;
public:
	void execute();
	~Exec_Update_Table();
	Exec_Update_Table(uint8_t nbElem);
};

class Exec_Script : public Executable
{
private:
	uint32_t m_nbScript;
public:
	void execute();
	~Exec_Script();
	Exec_Script(uint32_t nbScript);
};

class Exec_Act : public Executable
{
private:
	uint32_t m_nbAct;
public:
	void execute();
	~Exec_Act();
	Exec_Act(uint32_t nbAct);
};

#endif
