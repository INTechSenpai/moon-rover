#include "Hook.h"

#include "FreeRTOS.h"
#include "task.h"

#include "Executable.h"


Hook::Hook(bool isUnique, uint8_t nbCallback) : m_isUnique(isUnique), m_isDone(false), m_nbCallback(nbCallback)
{
	m_callbacks = (Executable**) pvPortMalloc(sizeof(Executable*)*m_nbCallback);
}

void Hook::insert(Executable* f, uint8_t indice)
{
	m_callbacks[indice] = f;
}

void Hook::execute()
{
	m_isDone = true;
	for(int i = 0; i < m_nbCallback; i++)
		(*m_callbacks[i]).execute();
}

Hook::~Hook()
{
	vPortFree(m_callbacks);
}

/**
 * HOOK DE TEMPS
 */

uint32_t HookTemps::m_dateDebutMatch;

bool HookTemps::evalue()
{
	return (!m_isUnique || !m_isDone) && (xTaskGetTickCount() - m_dateDebutMatch) >= m_dateExecution;
}

void HookTemps::setDateDebutMatch()
{
	m_dateDebutMatch = xTaskGetTickCount();
}

HookTemps::HookTemps(uint8_t nbCallback, uint32_t dateExecution):Hook(true, nbCallback), m_dateExecution(dateExecution)
{}

/**
 * HOOK DE CONTACT
 */

bool HookContact::evalue()
{
	// TODO
	return false;
}

HookContact::HookContact(bool isUnique, uint8_t nbCallback, uint8_t nbCapteur):Hook(isUnique, nbCallback), m_nbCapteur(nbCapteur)
{}

/**
 * HOOK DE POSITION
 */

bool HookPosition::evalue()
{
	// TODO
	return false;
}

HookPosition::HookPosition(bool isUnique, uint8_t nbCallback, uint32_t x, uint32_t y, uint32_t tolerance):Hook(isUnique,nbCallback), m_x(x), m_y(y), m_tolerance(tolerance)
{}

/**
 * HOOK DE DEMI-PLAN
 */

bool HookDemiPlan::evalue()
{
	// TODO
	return false;
}

HookDemiPlan::HookDemiPlan(uint8_t nbCallback, uint32_t x, uint32_t y, uint32_t direction_x, uint32_t direction_y):Hook(true, nbCallback), m_x(x), m_y(y), m_direction_x(direction_x), m_direction_y(direction_y)
{}
