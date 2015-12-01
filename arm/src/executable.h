#ifndef EXECUTABLE
#define EXECUTABLE

#include "FreeRTOS.h"
#include "task.h"
#include "Uart.hpp"

class Executable
{
public:
	virtual void execute();
	virtual ~Executable();
};

class Exec_Update_Table : public Executable
{
private:
	static Uart<2> m_serie_rb;
public:
	void execute();
	~Exec_Update_Table();
	static void setSerie(Uart<2> serie_rb);
};

#endif
