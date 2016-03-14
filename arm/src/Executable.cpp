#include "Executable.h"
#include "FreeRTOS.h"
#include "task.h"

#include "serie.h"

Executable::~Executable()
{}

/**
 * Exécutable update table
 */

void Exec_Update_Table::execute()
{
	sendElementShoot(m_nbElem);
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
}

/**
 * Exécutable actionneur
 */

Exec_Act::~Exec_Act()
{}

Exec_Act::Exec_Act(AX<Uart<6>>* ax, uint16_t angle):m_ax(ax), m_angle(angle)
{}

void Exec_Act::execute()
{
	m_ax->goTo(m_angle);
}
