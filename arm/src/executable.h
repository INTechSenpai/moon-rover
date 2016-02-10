#ifndef EXECUTABLE
#define EXECUTABLE

#include "FreeRTOS.h"
#include "task.h"
#include "Uart.hpp"
#include "global.h"
#include "ax12.hpp"

class Executable
{
public:
	virtual void execute()=0;
	virtual ~Executable();
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
	AX<Uart<6>>* m_ax;
	uint16_t m_angle;
public:
	void execute();
	~Exec_Act();
	Exec_Act(AX<Uart<6>>* ax, uint16_t angle);
};

#endif
