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

// TODO : les volatile
// TODO : les mutex
// TODO : tester #include "arm_math.h"

TIM_HandleTypeDef timer, timer2, timer3;

int main(int argc, char* argv[])
{
	TIM_Encoder_InitTypeDef encoder, encoder2;

	HAL_Init();
	SystemClock_Config();

	HAL_NVIC_SetPriorityGrouping(NVIC_PRIORITYGROUP_4);
	HAL_NVIC_SetPriority(SysTick_IRQn, 0, 1);
	listeHooks.reserve(100);

	/**
	 * Initialisation du codeur 1
	 */

	timer.Instance = TIM3;
	timer.Init.Period = 0xFFFF;
	timer.Init.CounterMode = TIM_COUNTERMODE_UP;
	timer.Init.Prescaler = 0;
	timer.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;

	encoder.EncoderMode = TIM_ENCODERMODE_TI12;
	encoder.IC1Filter = 0x0F;
	encoder.IC1Polarity = TIM_INPUTCHANNELPOLARITY_RISING;
	encoder.IC1Prescaler = TIM_ICPSC_DIV4;
	encoder.IC1Selection = TIM_ICSELECTION_DIRECTTI;

	encoder.IC2Filter = 0x0F;
	encoder.IC2Polarity = TIM_INPUTCHANNELPOLARITY_FALLING;
	encoder.IC2Prescaler = TIM_ICPSC_DIV4;
	encoder.IC2Selection = TIM_ICSELECTION_DIRECTTI;

	HAL_TIM_Encoder_Init(&timer, &encoder);
	HAL_TIM_Encoder_Start_IT(&timer, TIM_CHANNEL_1);

	/**
	 * Initialisation du codeur 2
	 */

	timer2.Instance = TIM2;
	timer2.Init.Period = 0xFFFF;
	timer2.Init.CounterMode = TIM_COUNTERMODE_UP;
	timer2.Init.Prescaler = 0;
	timer2.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;

	encoder2.EncoderMode = TIM_ENCODERMODE_TI12;

	encoder2.IC1Filter = 0x0F;
	encoder2.IC1Polarity = TIM_INPUTCHANNELPOLARITY_RISING;
	encoder2.IC1Prescaler = TIM_ICPSC_DIV4;
	encoder2.IC1Selection = TIM_ICSELECTION_DIRECTTI;

	encoder2.IC2Filter = 0x0F;
	encoder2.IC2Polarity = TIM_INPUTCHANNELPOLARITY_FALLING;
	encoder2.IC2Prescaler = TIM_ICPSC_DIV4;
	encoder2.IC2Selection = TIM_ICSELECTION_DIRECTTI;

	HAL_TIM_Encoder_Init(&timer2, &encoder2);
	HAL_TIM_Encoder_Start_IT(&timer2, TIM_CHANNEL_1);

	/**
	 * Activation des timers des codeurs
	 */

	GPIO_InitTypeDef GPIO_InitStructA, GPIO_InitStructB2, GPIO_InitStructB;

	__TIM3_CLK_ENABLE();
	__TIM2_CLK_ENABLE();

	__GPIOA_CLK_ENABLE();
	__GPIOB_CLK_ENABLE();

	// Pin B4 et B5 : codeur droit, timer 3

	GPIO_InitStructB2.Pin = GPIO_PIN_4 | GPIO_PIN_5;
	GPIO_InitStructB2.Mode = GPIO_MODE_AF_PP;
	GPIO_InitStructB2.Pull = GPIO_PULLUP;
	GPIO_InitStructB2.Speed = GPIO_SPEED_HIGH;
	GPIO_InitStructB2.Alternate = GPIO_AF2_TIM3;
	HAL_GPIO_Init(GPIOB, &GPIO_InitStructB2);

	HAL_NVIC_SetPriority(TIM3_IRQn, 0, 1);

	// Pin A15 et B3 : codeur gauche, timer 2

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

	HAL_NVIC_SetPriority(TIM2_IRQn, 0, 1);

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
	// Calcul du prescaler qui vient directement d'INTech
	timer3.Init.Prescaler= (uint16_t)((SystemCoreClock / 2) / 256000) - 1; //le deuxième /2 est dû au changement pour un timer de clock doublée
	timer3.Init.CounterMode = TIM_COUNTERMODE_UP;
	timer3.Init.Period = 8000;
	timer3.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
    HAL_TIM_PWM_Init(&timer3);

    TIM_OC_InitTypeDef oc_config;
    oc_config.OCMode = TIM_OCMODE_PWM1;
    oc_config.Pulse = 6000;
    oc_config.OCPolarity = TIM_OCPOLARITY_LOW;
    oc_config.OCFastMode = TIM_OCFAST_DISABLE;

    HAL_TIM_PWM_ConfigChannel(&timer3, &oc_config, TIM_CHANNEL_1);
    HAL_TIM_PWM_ConfigChannel(&timer3, &oc_config, TIM_CHANNEL_2);

    HAL_TIM_PWM_Start(&timer3, TIM_CHANNEL_1);
    HAL_TIM_PWM_Start(&timer3, TIM_CHANNEL_2);
	HAL_NVIC_SetPriority(TIM8_CC_IRQn, 0, 1);
	__GPIOD_CLK_ENABLE();
	__GPIOE_CLK_ENABLE();

	// TODO
	TIM8->CCR1 = 4000;
	TIM8->CCR2 = 4000;

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

	// Le thread d'odométrie et d'asservissement à une haute priorité car c'est le seul qui est critique
	xTaskCreate(thread_odometrie_asser, (char*)"TH_ODO_ASR", 2048, 0, 2, 0);

	vTaskStartScheduler();
	while(1); // on ne devrait jamais arriver ici
}

void TIM3_IRQHandler(void){
	HAL_TIM_IRQHandler(&timer);
}

void TIM2_IRQHandler(void){
	HAL_TIM_IRQHandler(&timer2);
}

#pragma GCC diagnostic pop
