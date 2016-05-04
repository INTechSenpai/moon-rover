#include "Hook.h"

#include "FreeRTOS.h"
#include "task.h"
#include "global.h"
#include "Executable.h"

Hook::Hook(uint8_t id, bool isUnique, uint8_t nbCallback) : m_id(id), m_isUnique(isUnique), m_nbCallback(nbCallback)
{
	m_callbacks = (Executable**) pvPortMalloc(sizeof(Executable*)*m_nbCallback);
}

void Hook::insert(Executable* f, uint8_t indice)
{
	m_callbacks[indice] = f;
}

bool Hook::execute()
{
	for(int i = 0; i < m_nbCallback; i++)
		(*m_callbacks[i]).execute();
	return m_isUnique;
}

Hook::~Hook()
{
	for(int i = 0; i < m_nbCallback; i++)
		vPortFree(m_callbacks[i]);
	vPortFree(m_callbacks);
}

uint16_t Hook::getId()
{
	return m_id;
}

/**
 * HOOK�DE�TEMPS
 */

uint32_t HookTemps::m_dateDebutMatch;

bool HookTemps::evalue()
{
	return (xTaskGetTickCount() - m_dateDebutMatch) >= m_dateExecution;
}

void HookTemps::setDateDebutMatch()
{
	m_dateDebutMatch = xTaskGetTickCount();
}

HookTemps::HookTemps(uint8_t id, uint8_t nbCallback, uint32_t dateExecution):Hook(id, true, nbCallback), m_dateExecution(dateExecution)
{}

/**
 * HOOK�DE�CONTACT
 */

bool HookContact::evalue()
{
	// TODO
	return false;
}

HookContact::HookContact(uint8_t id, bool isUnique, uint8_t nbCallback, uint8_t nbCapteur):Hook(id, isUnique, nbCallback), m_nbCapteur(nbCapteur)
{}

/**
 * HOOK�DE�POSITION
 */

bool HookPosition::evalue()
{
	return (x_odo - m_x) * (x_odo - m_x) + (y_odo - m_y) * (y_odo - m_y) < m_tolerance;
}

HookPosition::HookPosition(uint8_t id, uint8_t nbCallback, uint16_t x, uint16_t y, uint16_t tolerance):Hook(id,true,nbCallback), m_x(x), m_y(y), m_tolerance(tolerance)
{}

/**
 * HOOK�DE�DEMI-PLAN
 */

bool HookDemiPlan::evalue()
{
	return (x_odo - m_x) * m_direction_x + (y_odo - m_y) * m_direction_y > 0;
}

HookDemiPlan::HookDemiPlan(uint8_t id, uint8_t nbCallback, float x, float y, float direction_x, float direction_y):Hook(id, true, nbCallback), m_x(x), m_y(y), m_direction_x(direction_x), m_direction_y(direction_y)
{}
