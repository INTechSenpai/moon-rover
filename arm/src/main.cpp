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

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma GCC diagnostic ignored "-Wmissing-declarations"
#pragma GCC diagnostic ignored "-Wreturn-type"

//�TODO�: les volatile
// TODO�: les mutex
// TODO : tester #include "arm_math.h"

int main(int argc, char* argv[])
{
	TIM_HandleTypeDef timer3;
	HAL_Init();
	SystemClock_Config();

	HAL_NVIC_SetPriorityGrouping(NVIC_PRIORITYGROUP_4);
	HAL_NVIC_SetPriority(SysTick_IRQn, 0, 1);
	listeHooks.reserve(100);

	/**
	 * Configuration du PWM des moteurs
	 */

	__TIM8_CLK_ENABLE();

	__GPIOC_CLK_ENABLE();

	GPIO_InitTypeDef GPIO_InitStruct; // pins C6 et C7
	GPIO_InitStruct.Pin = GPIO_PIN_6 | GPIO_PIN_7;
	GPIO_InitStruct.Mode = GPIO_MODE_AF_PP;
	GPIO_InitStruct.Speed = GPIO_SPEED_FAST;
	GPIO_InitStruct.Alternate = GPIO_AF3_TIM8;
	GPIO_InitStruct.Pull = GPIO_NOPULL;

	HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);

	timer3.Instance = TIM8;

	timer3.Init.Prescaler =  (uint16_t)((SystemCoreClock / 2) / (FREQUENCE_PWM * PWM_MAX)) - 1; //le deuxi�me /2 est d� au changement pour un timer de clock doubl�e
	timer3.Init.CounterMode = TIM_COUNTERMODE_UP;
	timer3.Init.Period = PWM_MAX;
	timer3.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
    HAL_TIM_PWM_Init(&timer3);

    TIM_OC_InitTypeDef oc_config;
    oc_config.OCMode = TIM_OCMODE_PWM1;
    oc_config.Pulse = 0; // valeur initiale
    oc_config.OCPolarity = TIM_OCPOLARITY_HIGH;
    oc_config.OCIdleState = TIM_OCIDLESTATE_RESET;
    oc_config.OCFastMode = TIM_OCFAST_DISABLE;
    oc_config.OCNPolarity = TIM_OCNPOLARITY_HIGH;
    oc_config.OCNIdleState = TIM_OCNIDLESTATE_RESET;

    HAL_TIM_PWM_ConfigChannel(&timer3, &oc_config, TIM_CHANNEL_1);
    HAL_TIM_PWM_ConfigChannel(&timer3, &oc_config, TIM_CHANNEL_2);

    HAL_TIM_PWM_Start(&timer3, TIM_CHANNEL_1);
    HAL_TIM_PWM_Start(&timer3, TIM_CHANNEL_2);
	HAL_NVIC_SetPriority(TIM8_CC_IRQn, 0, 1);
	__GPIOD_CLK_ENABLE();
	__GPIOE_CLK_ENABLE();

	/**
	 * Pins de direction
	 */

	GPIO_InitTypeDef GPIO_InitStruct2;

	GPIO_InitStruct2.Pin = GPIO_PIN_10 | GPIO_PIN_12;
	GPIO_InitStruct2.Mode = GPIO_MODE_OUTPUT_PP;
	GPIO_InitStruct2.Speed = GPIO_SPEED_FAST;
	GPIO_InitStruct2.Pull = GPIO_NOPULL;
	HAL_GPIO_Init(GPIOD, &GPIO_InitStruct2);

	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_10, GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12, GPIO_PIN_SET);

	xTaskCreate(thread_hook, (char*)"TH_HOOK", 2048, 0, 1, 0);
	xTaskCreate(thread_ecoute_serie, (char*)"TH_LISTEN", 2048, 0, 1, 0);
	xTaskCreate(thread_capteurs, (char*)"TH_CPT", 2048, 0, 1, 0);

	// Le thread d'odom�trie et d'asservissement � une haute priorit� car c'est le seul qui est critique
	xTaskCreate(thread_odometrie_asser, (char*)"TH_ODO_ASR", 2048, 0, 2, 0);

	vTaskStartScheduler();
	while(1); // on ne devrait jamais arriver ici
}

/**
 * Fonction appel�e automatiquement lors de l'initialisation des encodeurs
 */
void HAL_TIM_Encoder_MspInit(TIM_HandleTypeDef *htim)
{
	GPIO_InitTypeDef GPIO_InitStructA, GPIO_InitStructB2, GPIO_InitStructB;

	/**
	 * Activation des timers des codeurs
	 */
	if(htim->Instance == TIM3)
	{
		__TIM3_CLK_ENABLE();

		__GPIOB_CLK_ENABLE();

		// Pin B4 et B5 : codeur droit, timer 3

		GPIO_InitStructB2.Pin = GPIO_PIN_4 | GPIO_PIN_5;
		GPIO_InitStructB2.Mode = GPIO_MODE_AF_PP;
		GPIO_InitStructB2.Pull = GPIO_PULLUP;
		GPIO_InitStructB2.Speed = GPIO_SPEED_HIGH;
		GPIO_InitStructB2.Alternate = GPIO_AF2_TIM3;
		HAL_GPIO_Init(GPIOB, &GPIO_InitStructB2);
	}
	else if(htim->Instance == TIM2)
	{
		// Pin A15 et B3 : codeur gauche, timer 2
		__TIM2_CLK_ENABLE();
		__GPIOA_CLK_ENABLE();
		__GPIOB_CLK_ENABLE();

		GPIO_InitStructA.Pin = GPIO_PIN_15;
		GPIO_InitStructA.Mode = GPIO_MODE_AF_PP;
		GPIO_InitStructA.Pull = GPIO_PULLUP;
		GPIO_InitStructA.Speed = GPIO_SPEED_HIGH;
		GPIO_InitStructA.Alternate = GPIO_AF1_TIM2;
		HAL_GPIO_Init(GPIOA, &GPIO_InitStructA);

		GPIO_InitStructB.Pin = GPIO_PIN_3;
		GPIO_InitStructB.Mode = GPIO_MODE_AF_PP;
		GPIO_InitStructB.Pull = GPIO_PULLUP;
		GPIO_InitStructB.Speed = GPIO_SPEED_HIGH;
		GPIO_InitStructB.Alternate = GPIO_AF1_TIM2;
		HAL_GPIO_Init(GPIOB, &GPIO_InitStructB);
	}
}

#pragma GCC diagnostic pop
