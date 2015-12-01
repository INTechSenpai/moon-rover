#ifndef DEF_HOOK
#define DEF_HOOK

#include <stm32f4xx_hal.h>
#include "Executable.h"

/**
 * Classe mère abstraite des Hooks.
 */


class Hook
{

public:
	bool evalue();
	Hook(bool isUnique, uint8_t nbCallback);
	~Hook();
	void insert(Executable f, uint8_t indice);
	void execute();

private:
	bool m_isUnique;
	uint8_t m_nbCallback;
	Executable* m_callbacks;
};

class HookTemps : public Hook
{
private:
	uint32_t m_dateExecution;
	static uint32_t m_dateDebutMatch;

public:
	static void setDateDebutMatch();
	HookTemps(uint8_t nbCallback, uint32_t dateExecution);
	bool evalue();
};

#endif
