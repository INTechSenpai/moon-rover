#include "Executable.h"
#include "FreeRTOS.h"
#include "task.h"

Executable::~Executable()
{}


/**
 * Ex�cutable update table
 */

void Exec_Update_Table::execute()
{
	serial_rb.printfln("tbl %d", m_nbElem);
}

Exec_Update_Table::~Exec_Update_Table()
{}

Exec_Update_Table::Exec_Update_Table(uint8_t nbElem):m_nbElem(nbElem)
{}

/**
 * Ex�cutable script
 */

Exec_Script::~Exec_Script()
{}

Exec_Script::Exec_Script(uint32_t nbScript):m_nbScript(nbScript)
{}

void Exec_Script::execute()
{
	serial_rb.printfln("scr %d", m_nbScript);
}

/**
 * Ex�cutable actionneur
 */

Exec_Act::~Exec_Act()
{}

Exec_Act::Exec_Act(uint32_t nbAct):m_nbAct(nbAct)
{}

void Exec_Act::execute()
{
	serial_rb.printfln("ordre SSC32");
	// TODO envoie s�rie SSC 32
}
