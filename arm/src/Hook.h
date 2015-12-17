#ifndef DEF_HOOK
#define DEF_HOOK

#include <stm32f4xx_hal.h>
#include "Executable.h"

/**
 * Classe m�re abstraite des Hooks.
 */


class Hook
{

public:
	virtual bool evalue() = 0;
	Hook(bool isUnique, uint8_t numero, uint8_t nbCallback);
	virtual ~Hook();
	void insert(Executable* f, uint8_t indice);
	bool execute();
	bool shouldBeDeleted(uint8_t numero);

protected:
	bool m_isUnique;

private:
	uint8_t m_numero;
	uint8_t m_nbCallback;
	Executable** m_callbacks;
};

class HookTemps : public Hook
{
private:
	uint32_t m_dateExecution;
	static uint32_t m_dateDebutMatch;

public:
	static void setDateDebutMatch();
	HookTemps(uint8_t numero, uint8_t nbCallback, uint32_t dateExecution);
	bool evalue();
};

class HookContact : public Hook
{
private:
	uint8_t m_nbCapteur;

public:
	HookContact(bool isUnique, uint8_t numero, uint8_t nbCallback, uint8_t nbCapteur);
	bool evalue();
};

class HookPosition : public Hook
{
private:
	uint32_t m_x;
	uint32_t m_y;
	uint32_t m_tolerance;

public:
	HookPosition(uint8_t numero, uint8_t nbCallback, uint32_t x, uint32_t y, uint32_t tolerance);
	bool evalue();
};

class HookDemiPlan : public Hook
{
private:
	uint32_t m_x;
	uint32_t m_y;
	uint32_t m_direction_x;
	uint32_t m_direction_y;

public:
	HookDemiPlan(uint8_t numero, uint8_t nbCallback, uint32_t x, uint32_t y, uint32_t direction_x, uint32_t direction_y);
	bool evalue();
};

#endif