#ifndef _LOG_h
#define _LOG_h

#if defined(ARDUINO) && ARDUINO >= 100
#include "arduino.h"
#else
#include "WProgram.h"
#endif

#include "utils.h"
#include "communication_setup.h"

#define LOG_PREFIX_DATA		"_data_"
#define LOG_PREFIX_WARNING	"_warning_"
#define LOG_PREFIX_CRITICAL	"_error_"


class Log
{
public:
	Log()
	{}

	/* 
	On peut définir jusqu'à 32 cannaux de Log "data". Les cannaux "warning" et "critical" étant indépendants de tout cela.
	Un canal est donc identifié par un entier entre 0 et 31, ou bien grâce à l'enum ci-dessous qui permet d'avoir des noms
	plus parlants à la place des chiffres.
	Chaque canal peut être activé ou non, les cannaux activés sont indiqués par un uint32_t dans lequel chaque bit à 1
	correspond à un canal activé. Le bit de poids faible correspond au canal 0, le bit de poids fort au canal 31.
	*/

	enum LogChannel
	{
		POSITION,
		TRAJECTORY,
		PID_V_G,
		PID_V_D,
		PID_TRANS,
		BLOCKING_M_G,
		BLOCKING_M_D,
		STOPPING_MGR,
		DIRECTION,
		SENSORS
	};

private:
	static uint32_t enabledChannels;

public:
	static void enableChannel(LogChannel channel, bool enable)
	{
		uint32_t mask = 1 << channel;
		if (enable)
		{
			enabledChannels |= mask;
		}
		else
		{
			mask = ~mask;
			enabledChannels &= mask;
		}
	}

	static inline void data(LogChannel channel, const Printable & obj)
	{
		if ((enabledChannels & (1 << channel)) != 0 && debug_serial_free)
		{
			Serial.print(LOG_PREFIX_DATA);
			Serial.print(channel);
			Serial.print("_");
			Serial.println(obj);
			Serial.flush();
		}
	}

	static void warning(const char* s)
	{
		if (debug_serial_free)
		{
			Serial.print(LOG_PREFIX_WARNING);
			Serial.println(s);
		}
	}

	static void critical(int errorCode, const char* s = "")
	{
		if (debug_serial_free)
		{
			Serial.print(LOG_PREFIX_CRITICAL);
			Serial.print(errorCode);
			Serial.print("_");
			Serial.println(s);
		}
	}

	static void critical(int errorCode, const Printable & obj, const char* s = "")
	{
		if (debug_serial_free)
		{
			Serial.print(LOG_PREFIX_CRITICAL);
			Serial.print(errorCode);
			Serial.print("_");
			Serial.print(s);
			Serial.print("_");
			Serial.println(obj);
		}
	}
};


#endif

