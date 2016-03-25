//
// This file is part of the GNU ARM Eclipse distribution.
// Copyright (c) 2014 Liviu Ionescu.
//

// ----------------------------------------------------------------------------

#include <stdio.h>
#include <stdlib.h>
#include <vector>
#include "diag/Trace.h"

#include "math.h"
#include "Timer.h"
#include "FreeRTOS.h"
#include "task.h"
#include "_initialize_hardware.c"
#include "Hook.h"
#include "Uart.hpp"
#include "global.h"
#include "ax12.hpp"
#include "serialProtocol.h"
#include "asserSimple.hpp"
#include "serie.h"
#include "threadEcouteSerie.hpp"
#include "threadHook.hpp"
#include "threadCapteurs.hpp"
#include "threadOdoAsser.hpp"

using namespace std;

// ----- main() ---------------------------------------------------------------

// Sample pragmas to cope with warnings. Please note the related line at
// the end of this function, used to pop the compiler diagnostics status.
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma GCC diagnostic ignored "-Wmissing-declarations"
#pragma GCC diagnostic ignored "-Wreturn-type"




// TODO : les volatile
// TODO : les mutex
// TODO : tester #include "arm_math.h"


int main(int argc, char* argv[])
{
	HAL_Init();
	SystemClock_Config();

	HAL_NVIC_SetPriorityGrouping(NVIC_PRIORITYGROUP_4);
	HAL_NVIC_SetPriority(SysTick_IRQn, 0, 1);
	HookTemps::setDateDebutMatch(); // TODO
	listeHooks.reserve(100);
	__TIM8_CLK_ENABLE();

	__GPIOC_CLK_ENABLE();

	GPIO_InitTypeDef GPIO_InitStruct;
	GPIO_InitStruct.Pin = GPIO_PIN_6 | GPIO_PIN_7;
	GPIO_InitStruct.Mode = GPIO_MODE_AF_PP;
	GPIO_InitStruct.Speed = GPIO_SPEED_FAST;
	GPIO_InitStruct.Alternate = GPIO_AF3_TIM8;
	GPIO_InitStruct.Pull = GPIO_NOPULL;

	HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);

    // Configure TIM4 for PWM
	timer3.Instance = TIM8;
	// Calcul du prescaler qui vient directement d'INTech
	timer3.Init.Prescaler= (uint16_t)((SystemCoreClock / 2) / 256000) - 1; //le deuxième /2 est dû au changement pour un timer de clock doublée
	timer3.Init.CounterMode = TIM_COUNTERMODE_UP;
	timer3.Init.Period = 8000;
	timer3.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
    HAL_TIM_PWM_Init(&timer3);

    // Configure channels 1-4 for TIM4,
    // each channel is mapped to a GPIO pin
    TIM_OC_InitTypeDef oc_config;
    oc_config.OCMode = TIM_OCMODE_PWM1;
    oc_config.Pulse = 6000;
    oc_config.OCPolarity = TIM_OCPOLARITY_LOW;
//    oc_config.OCNIdleState = TIM_OCNIDLESTATE_RESET;
//    oc_config.OCIdleState = TIM_OCIDLESTATE_RESET;
    oc_config.OCFastMode = TIM_OCFAST_DISABLE;

    HAL_TIM_PWM_ConfigChannel(&timer3, &oc_config, TIM_CHANNEL_1);

    // Flip the OC polarity for channels 2 and 4
    oc_config.OCMode = TIM_OCMODE_PWM1;
    HAL_TIM_PWM_ConfigChannel(&timer3, &oc_config, TIM_CHANNEL_2);

    // I want to shift channel 1-4 90 degrees apart...
    HAL_TIM_PWM_Start(&timer3, TIM_CHANNEL_1);
    HAL_TIM_PWM_Start(&timer3, TIM_CHANNEL_2);
	HAL_NVIC_SetPriority(TIM8_CC_IRQn, 0, 1);
	__GPIOD_CLK_ENABLE();

	/**
	* Initialisation des pins moteurs
	*/

	TIM8->CCR1 = 4000;
	TIM8->CCR2 = 4000;



	xTaskCreate(thread_capteurs, (char*)"TH_CPT", 2048, 0, 1, 0);
	vTaskStartScheduler();


	/*
	xTaskCreate(thread_hook, (char*)"TH_HOOK", 2048, 0, 1, 0);
	xTaskCreate(thread_ecoute_serie, (char*)"TH_LISTEN", 2048, 0, 1, 0);
	xTaskCreate(thread_odometrie_asser, (char*)"TH_ODO_ASR", 2048, 0, 1, 0);
	xTaskCreate(thread_capteurs, (char*)"TH_CPT", 2048, 0, 1, 0);
	vTaskStartScheduler();*/
	while(1)
	{
		vTaskDelay(1000);
	}

	return 0;
}


/*void TIM5_IRQHandler(void)
{
	HAL_TIM_IRQHandler(&timer);
}*/
#pragma GCC diagnostic pop

// ----------------------------------------------------------------------------
