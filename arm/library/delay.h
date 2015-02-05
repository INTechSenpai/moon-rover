#ifndef __DELAY_H
#define __DELAY_H

#include "stm32f4xx.h"

#ifdef __cplusplus
extern "C" {
#endif

static __IO uint32_t timestamp;

void Delay(__IO uint32_t time);
void Delay_Init();
void SysTick_Handler(void);
uint32_t Millis();

#ifdef __cplusplus
}
#endif

#endif
