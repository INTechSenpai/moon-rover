#include "Hook.h"

#include "FreeRTOS.h"
#include "task.h"

#include "Executable.h"


Hook::Hook(bool isUnique, uint8_t nbCallback) : m_isUnique(isUnique), m_nbCallback(nbCallback)
{
	m_callbacks = (Executable*) pvPortMalloc(sizeof(Executable)*m_nbCallback);
}

void Hook::insert(Executable f, uint8_t indice)
{
	m_callbacks[indice] = f;
}

void Hook::execute()
{
	for(int i = 0; i < m_nbCallback; i++)
		m_callbacks[i].execute();
}

Hook::~Hook()
{
	vPortFree(m_callbacks);
}

uint32_t HookTemps::m_dateDebutMatch;

bool HookTemps::evalue()
{
	return (xTaskGetTickCount() - m_dateDebutMatch) > m_dateExecution;
}

void HookTemps::setDateDebutMatch()
{
	m_dateDebutMatch = xTaskGetTickCount();
}

HookTemps::HookTemps(uint8_t nbCallback, uint32_t dateExecution):Hook(true, nbCallback), m_dateExecution(dateExecution)
{}
