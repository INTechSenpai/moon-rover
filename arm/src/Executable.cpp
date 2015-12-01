#include "Executable.h"
#include "FreeRTOS.h"
#include "task.h"

Executable::~Executable()
{}

void Exec_Update_Table::execute()
{
	m_serie_rb.printfln("OK");
//	m_serie_rb.printfln("%d", xTaskGetTickCount());
}

Exec_Update_Table::~Exec_Update_Table()
{}

void Exec_Update_Table::setSerie(Uart<2> serie_rb)
{
	m_serie_rb = serie_rb;
	m_serie_rb.printfln("INITIALISATION");
}
