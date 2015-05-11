/**
 * Moteur.cpp
 *
 * Classe de gestion d'un moteur (PWM, direction...)
 *
 * Récapitulatif pins utilisées pour contrôler les deux moteurs :
 *
 * Gauche :
 * 	-pins de sens : PD10
 * 	-pin de pwm : PC6
 * Droit :
 * 	-pins de sens : PD12
 * 	-pin de pwm : PC7
 *
 */

#include "Motor.h"

Motor::Motor(Side s) :
		side(s){

	/**
	 * Configuration des pins pour le sens des moteurs
	 * Gauche : PD10 (IN2)
	 * Droite : PD12 (IN3)
	 */

	GPIO_InitTypeDef GPIO_InitStruct;
	GPIO_StructInit(&GPIO_InitStruct); //Remplit avec les valeurs par défaut
	// Active l'horloge du port D
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD, ENABLE);

	if (s == Side::LEFT) {
		GPIO_InitStruct.GPIO_Pin = GPIO_Pin_10;
		GPIO_InitStruct.GPIO_Mode = GPIO_Mode_OUT;
		GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
		GPIO_Init(GPIOD, &GPIO_InitStruct);
	} else {
		GPIO_InitStruct.GPIO_Pin = GPIO_Pin_12;
		GPIO_InitStruct.GPIO_Mode = GPIO_Mode_OUT;
		GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
		GPIO_Init(GPIOD, &GPIO_InitStruct);
	}

	setDirection(Direction::FORWARD);
}

void Motor::initPWM(){
	//Structures
	GPIO_InitTypeDef GPIO_InitStruct;
	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	/**
	 * Configuration des PWM générés sur les canaux 1 et 2 du TIMER2
	 */
	//Active l'horloge du TIMER 2
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM2, ENABLE);
	//Active l'horloge du port C
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOB, ENABLE);

	/**
	 * Configuration pins PWM :
	 * TIM2 CH1 (PC6 = moteur gauche) et TIM2 CH2 (PC7 = moteur droit)
	 *
	 */

	GPIO_StructInit(&GPIO_InitStruct); //Remplit avec les valeurs par défaut
	GPIO_InitStruct.GPIO_Pin = GPIO_Pin_15;
	GPIO_InitStruct.GPIO_Mode = GPIO_Mode_AF;
	GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_InitStruct.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_UP;
	GPIO_Init(GPIOA, &GPIO_InitStruct);
	//Connexion des 2 pins PC6 et PC7 à la fonction alternative liée au TIMER 8
	GPIO_PinAFConfig(GPIOA, GPIO_PinSource15, GPIO_AF_TIM2);

	GPIO_InitStruct.GPIO_Pin = GPIO_Pin_3;
	GPIO_Init(GPIOB, &GPIO_InitStruct);
	GPIO_PinAFConfig(GPIOB, GPIO_PinSource3, GPIO_AF_TIM2);

	/* -----------------------------------------------------------------------
	 TIM3 Configuration: génère 2 PWM à deux rapports cycliques différents (un timer a 4 canaux,
	 il est donc possible de générer jusqu'à 4 PWM avec un même timer)
	 J'ai laissé et modifié légèrement ci-dessous l'explication de ST sur la configuration du TIMER pour comprendre
	 comment éventuellement modifier la fréquence du PWM (que j'ai fixé ici à 1kHz, une bonne valeur moyenne)


	 In this example TIM3 input clock (TIM3CLK) is set to 2 * APB1 clock (PCLK1),
	 since APB1 prescaler is different from 1.
	 TIM3CLK = 2 * PCLK1
	 PCLK1 = HCLK / 4
	 => TIM3CLK = HCLK / 2 = SystemCoreClock /2

	 To get TIM3 counter clock at 256 kHz, the prescaler is computed as follows:
	 Prescaler = (TIM3CLK / TIM3 counter clock) - 1
	 Prescaler = ((SystemCoreClock /2) /28 MHz) - 1

	 To get TIM3 output clock at 1 KHz, the period (ARR)) is computed as follows:
	 ARR = (TIM3 counter clock / TIM3 output clock) - 1
	 = 255

	 TIM3 Channel1 duty cycle = (TIM3_CCR1/ TIM3_ARR)* 100 = X%
	 TIM3 Channel2 duty cycle = (TIM3_CCR2/ TIM3_ARR)* 100 = Y%

	 Note:
	 SystemCoreClock variable holds HCLK frequency and is defined in system_stm32f4xx.c file.
	 Each time the core clock (HCLK) changes, user had to call SystemCoreClockUpdate()
	 function to update SystemCoreClock variable value. Otherwise, any configuration
	 based on this variable will be incorrect.
	 ----------------------------------------------------------------------- */

	//Le prescaler peut être n'importe quel entier entre 1 et 65535 (uint16_t)
	uint16_t prescaler = (uint16_t)((SystemCoreClock / 2) / 256000) - 1; //le deuxième /2 est dû au changement pour un timer de clock doublée

	//Configuration du TIMER 8
	TIM_TimeBaseStructure.TIM_Period = 10;//ancienne valeur = 255
	TIM_TimeBaseStructure.TIM_Prescaler = prescaler;
	TIM_TimeBaseStructure.TIM_ClockDivision = 0;
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;

	TIM_TimeBaseInit(TIM2, &TIM_TimeBaseStructure);

	//Configuration du canal 1
	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_PWM1;
	TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_High;

	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
	TIM_OCInitStructure.TIM_Pulse = 0; //Valeur du cycle initial

	TIM_OC1Init(TIM2, &TIM_OCInitStructure);
	TIM_OC1PreloadConfig(TIM2, TIM_OCPreload_Enable);

	//Configuration du canal 2
	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
	TIM_OCInitStructure.TIM_Pulse = 0; //Valeur du cycle initial

	TIM_OC2Init(TIM2, &TIM_OCInitStructure);
	TIM_OC2PreloadConfig(TIM2, TIM_OCPreload_Enable);

	TIM_ARRPreloadConfig(TIM2, ENABLE);

	//Active le TIMER 8
	TIM_Cmd(TIM2, ENABLE);
}

void Motor::run(int16_t pwm){
	if (pwm >= 0) {
		setDirection(Direction::FORWARD);
		if (side == Side::LEFT) {
			TIM2->CCR1 = MIN(pwm,255);
		} else {
			TIM2->CCR2 = MIN(pwm,255);
		}

	} else {
		setDirection(Direction::BACKWARD);
		if (side == Side::LEFT) {
			TIM2->CCR1 = MIN(-pwm,255);
		} else {
			TIM2->CCR2 = MIN(-pwm,255);
		}
	}
}

void Motor::setDirection(Direction dir) {
	if (side == Side::LEFT) {
		if (dir == Direction::FORWARD) {
			GPIO_SetBits(GPIOD, GPIO_Pin_10);
		} else {
			GPIO_ResetBits(GPIOD, GPIO_Pin_10);
		}
	} else {
		if (dir == Direction::FORWARD) {
			GPIO_SetBits(GPIOD, GPIO_Pin_12);
		} else {
			GPIO_ResetBits(GPIOD, GPIO_Pin_12);
		}
	}
}
