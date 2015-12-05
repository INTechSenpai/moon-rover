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
public:
	void execute();
	~Exec_Update_Table();
};

#endif
