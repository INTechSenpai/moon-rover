/**
 * delay.c
 *
 * Configuration du SysTick sur un base de temps de la ms afin d'avoir une fonction delay(ms)
 *
 * Auteur : Paul BERNIER - bernier.pja@gmail.com
 */

#include "delay.h"

void Delay(__IO uint32_t time) {
	__IO uint32_t time_setpoint=timestamp+time*1000;
	while (timestamp < time_setpoint)
		;
}

void Delay_us(__IO uint32_t time) {
	__IO uint32_t time_setpoint=timestamp+time;
	while (timestamp < time_setpoint)
		;
}

void Delay_Init(){
	//Initialisation SysTick à la ms
	if (SysTick_Config(SystemCoreClock / 1000000)) {
		/* Capture error */
		while (1)
			;
	}
}

void SysTick_Handler(void) {
	timestamp++;
}

uint32_t Millis(){
	return timestamp/1000;
}

uint32_t Micros(){
	return timestamp;
}
