/**
 * Moteur.cpp
 *
 * Classe de gestion d'un moteur (PWM, direction...)
 *
 * Récapitulatif pins utilisées pour contrôler les deux moteurs :
 *
 * Gauche :
 * 	-pins de sens : PD13
 * 	-pin de pwm : PC6
 * Droit :
 * 	-pins de sens : PD12
 * 	-pin de pwm : PC7
 *
 */

#include "Motor.h"

Motor::Motor(Side s) :
		side(s), maxPWM(170) {

	/**
	 * Configuration des pins pour le sens des moteurs
	 * Gauche : PD13 (IN2)
	 * Droite : PD12 (IN3)
	 */

	GPIO_InitTypeDef GPIO_InitStructure;
	GPIO_StructInit(&GPIO_InitStructure); //Remplit avec les valeurs par défaut
	// Active l'horloge du port D
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD, ENABLE);

	if (s == Side::LEFT) {
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_14;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
		GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;
		GPIO_Init(GPIOD, &GPIO_InitStructure);
	} else {
		GPIO_InitStructure.GPIO_Pin = GPIO_Pin_12;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
		GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;
		GPIO_Init(GPIOD, &GPIO_InitStructure);
	}

	setDirection(Direction::FORWARD);
}

void Motor::initPWM(){
	//Structures
	GPIO_InitTypeDef GPIO_InitStructure;
	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_OCInitTypeDef TIM_OCInitStructure;

	/**
	 * Configuration des PWM générés sur les canaux 1 et 2 du TIMER3
	 */

	//Active l'horloge du TIMER 3
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM3, ENABLE);
	//Active l'horloge du port C
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);

	/**
	 * Configuration pins PWM :
	 * TIM3 CH1 (PC6 = moteur gauche) et TIM3 CH2 (PC7 = moteur droit)
	 */

	GPIO_StructInit(&GPIO_InitStructure); //Remplit avec les valeurs par défaut
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_6 | GPIO_Pin_7;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;
	GPIO_Init(GPIOC, &GPIO_InitStructure);
	//Connexion des 2 pins PC6 et PC7 à la fonction alternative liée au TIMER 3
	GPIO_PinAFConfig(GPIOC, GPIO_PinSource6, GPIO_AF_TIM3);
	GPIO_PinAFConfig(GPIOC, GPIO_PinSource7, GPIO_AF_TIM3);

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
	uint16_t prescaler = 1;//(uint16_t)((SystemCoreClock / 2) / 256000) - 1;

	//Configuration du TIMER 3
	TIM_TimeBaseStructure.TIM_Period = 255;
	TIM_TimeBaseStructure.TIM_Prescaler = prescaler;
	TIM_TimeBaseStructure.TIM_ClockDivision = 0;
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;

	TIM_TimeBaseInit(TIM3, &TIM_TimeBaseStructure);

	//Configuration du canal 1
	TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_PWM1;
	TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_High;

	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
	TIM_OCInitStructure.TIM_Pulse = 0; //Valeur du cycle initial

	TIM_OC1Init(TIM3, &TIM_OCInitStructure);
	TIM_OC1PreloadConfig(TIM3, TIM_OCPreload_Enable);

	//Configuration du canal 2
	TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
	TIM_OCInitStructure.TIM_Pulse = 0; //Valeur du cycle initial

	TIM_OC2Init(TIM3, &TIM_OCInitStructure);
	TIM_OC2PreloadConfig(TIM3, TIM_OCPreload_Enable);

	TIM_ARRPreloadConfig(TIM3, ENABLE);

	//Active le TIMER 3
	TIM_Cmd(TIM3, ENABLE);
}

void Motor::run(int16_t pwm){

	if (pwm >= 0) {
		setDirection(Direction::FORWARD);

		if (side == Side::LEFT) {
			TIM3->CCR1 = MIN(pwm, maxPWM);
		} else {
			TIM3->CCR2 = MIN(pwm, maxPWM);
		}

	} else {
		setDirection(Direction::BACKWARD);
		if (side == Side::LEFT) {
			TIM3->CCR1 = MIN(-pwm, maxPWM);
		} else {
			TIM3->CCR2 = MIN(-pwm, maxPWM);
		}
	}
}

void Motor::setDirection(Direction dir) {
	if (side == Side::LEFT) {
		if (dir == Direction::FORWARD) {
			GPIO_SetBits(GPIOD, GPIO_Pin_12);
		} else {
			GPIO_ResetBits(GPIOD, GPIO_Pin_12);
		}
	} else {
		if (dir == Direction::FORWARD) {
			GPIO_SetBits(GPIOD, GPIO_Pin_14);
		} else {
			GPIO_ResetBits(GPIOD, GPIO_Pin_14);
		}
	}
}

void Motor::setMaxPWM(uint8_t max) {
	maxPWM = max;
}
