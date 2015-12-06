#include "Executable.h"
#include "FreeRTOS.h"
#include "task.h"

Executable::~Executable()
{}


void Executable::setSerie(Uart<2> serie_rb)
{
	m_serie_rb = serie_rb;
}

/**
 * Exécutable update table
 */

void Exec_Update_Table::execute()
{
	m_serie_rb.printfln("tbl %d", m_nbElem);
}

Exec_Update_Table::~Exec_Update_Table()
{}

Exec_Update_Table::Exec_Update_Table(uint8_t nbElem):m_nbElem(nbElem)
{}

/**
 * Exécutable script
 */

Exec_Script::~Exec_Script()
{}

Exec_Script::Exec_Script(uint32_t nbScript):m_nbScript(nbScript)
{}

void Exec_Script::execute()
{
	m_serie_rb.printfln("scr %d", m_nbScript);
}

/**
 * Exécutable actionneur
 */

Exec_Act::~Exec_Act()
{}

Exec_Act::Exec_Act(uint32_t nbAct):m_nbAct(nbAct)
{}

void Exec_Act::execute()
{
	m_serie_rb.printfln("ordre SSC32");
	// TODO envoie série SSC 32
}
