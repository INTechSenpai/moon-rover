#ifndef _COMMUNICATION_SETUP_h
#define _COMMUNICATION_SETUP_h

#if defined(ARDUINO) && ARDUINO >= 100
#include "arduino.h"
#else
#include "WProgram.h"
#endif

/* Liaison s�rie avec la Raspberry pi */
#define SERIAL_HL			Serial1
#define SERIAL_HL_BAUDRATE	115200

/* Liaison s�rie avec les AX12 */
#define SERIAL_AX			Serial2
#define SERIAL_AX_BAUDRATE	9600


#endif

