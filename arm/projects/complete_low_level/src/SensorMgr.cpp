#include "SensorMgr.h"

/*		PINS DES CAPTEURS
 *
 * 	ULTRASONS:
 * 		Avant Droit   :	PA6
 * 		Avant Gauche  :	PA4
 * 		Arrière Droit :	PA7
 * 		Arrière Gauche:	PB1
 *
 * 	CONTACTEURS:
 * 		Monte-plot		: PC15
 * 		Gobelet Droit	: PD9
 * 		Gobelet Gauche	: PD11
 */


SensorMgr::SensorMgr():
	leftFrontUS(),
	rightFrontUS(),
	leftBackUS(),
	rightBackUS()
{
	lastRefreshTime = 0;
	refreshDelay = 13;//(ms)

	/* Set variables used */
	GPIO_InitTypeDef GPIO_InitStruct;
	EXTI_InitTypeDef EXTI_InitStruct;
	NVIC_InitTypeDef NVIC_InitStruct;

	/*
	 * Initialisation des pins des capteurs de contact
	 */

	GPIO_StructInit(&GPIO_InitStruct); //Remplit avec les valeurs par défaut

	//Capteurs de contact des gobelets (droit : PD9 | gauche : PD11)
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD, ENABLE);//Active l'horloge du port D

	GPIO_InitStruct.GPIO_Pin = GPIO_Pin_9;
	GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN;
	GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_NOPULL;
	GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_Init(GPIOD, &GPIO_InitStruct);

	GPIO_InitStruct.GPIO_Pin = GPIO_Pin_11;
	GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN;
	GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_NOPULL;
	GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_Init(GPIOD, &GPIO_InitStruct);


	//Capteur intérieur du monte-plot (PC15)
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);//Active l'horloge du port C

	GPIO_InitStruct.GPIO_Pin = GPIO_Pin_15;
	GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN;
	GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_NOPULL;
	GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_Init(GPIOC, &GPIO_InitStruct);


	//Jumper (PC9)
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);//Active l'horloge du port C

	GPIO_InitStruct.GPIO_Pin = GPIO_Pin_9;
	GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN;
	GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_NOPULL;
	GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_Init(GPIOC, &GPIO_InitStruct);





/*     ________________________________
	 *|								   |*
	 *|Initialisation des interruptions|*
	 *|________________________________|*
*/


	/*
	 * Capteur Avant Droit : PA6
	 */

	/* Activation de l'horloge du port GPIOA */
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);

	/* Activation de l'horloge du SYSCFG */
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_SYSCFG, ENABLE);

	/*Réglages de la pin*/
	GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN;
	GPIO_InitStruct.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStruct.GPIO_Pin = GPIO_Pin_6;
	GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_UP;
	GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_Init(GPIOA, &GPIO_InitStruct);

	/* Tell system that you will use PA6 for EXTI_Line6 */
	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOA, EXTI_PinSource6);

	/* PA6 is connected to EXTI_Line6 */
	EXTI_InitStruct.EXTI_Line = EXTI_Line6;
	/* Enable interrupt */
	EXTI_InitStruct.EXTI_LineCmd = DISABLE;
	/* Interrupt mode */
	EXTI_InitStruct.EXTI_Mode = EXTI_Mode_Interrupt;
	/* Triggers on rising and falling edge */
	EXTI_InitStruct.EXTI_Trigger = EXTI_Trigger_Rising;
	/* Add to EXTI */
	EXTI_Init(&EXTI_InitStruct);

	/* Add IRQ vector to NVIC */
	/* PA6 is connected to EXTI_Line6, which has EXTI9_5_IRQn vector */
	NVIC_InitStruct.NVIC_IRQChannel = EXTI9_5_IRQn;
	/* Set priority */
	NVIC_InitStruct.NVIC_IRQChannelPreemptionPriority = 0x00;
	/* Set sub priority */
	NVIC_InitStruct.NVIC_IRQChannelSubPriority = 0x00;
	/* Enable interrupt */
	NVIC_InitStruct.NVIC_IRQChannelCmd = ENABLE;
	/* Add to NVIC */
	NVIC_Init(&NVIC_InitStruct);

	rightFrontUS.init(GPIOA, GPIO_InitStruct, EXTI_InitStruct);//On donne les paramètres de la pin et de l'interruption au capteur pour qu'il puisse les modifier sans faire d'erreur


	/*
	 * Capteur Avant Gauche : PA4
	 */

	/* Activation de l'horloge du port GPIOA */
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);

	/* Activation de l'horloge du SYSCFG */
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_SYSCFG, ENABLE);

	/*Réglages de la pin*/
	GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN;
	GPIO_InitStruct.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStruct.GPIO_Pin = GPIO_Pin_4;
	GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_UP;
	GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_Init(GPIOA, &GPIO_InitStruct);

	/* Tell system that you will use PA4 for EXTI_Line4 */
	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOA, EXTI_PinSource4);

	/* PA4 is connected to EXTI_Line4 */
	EXTI_InitStruct.EXTI_Line = EXTI_Line4;
	/* Enable interrupt */
	EXTI_InitStruct.EXTI_LineCmd = DISABLE;
	/* Interrupt mode */
	EXTI_InitStruct.EXTI_Mode = EXTI_Mode_Interrupt;
	/* Triggers on rising and falling edge */
	EXTI_InitStruct.EXTI_Trigger = EXTI_Trigger_Rising;
	/* Add to EXTI */
	EXTI_Init(&EXTI_InitStruct);

	/* Add IRQ vector to NVIC */
	/* PA4 is connected to EXTI_Line4, which has EXTI4_IRQn vector */
	NVIC_InitStruct.NVIC_IRQChannel = EXTI4_IRQn;
	/* Set priority */
	NVIC_InitStruct.NVIC_IRQChannelPreemptionPriority = 0x00;
	/* Set sub priority */
	NVIC_InitStruct.NVIC_IRQChannelSubPriority = 0x01;
	/* Enable interrupt */
	NVIC_InitStruct.NVIC_IRQChannelCmd = ENABLE;
	/* Add to NVIC */
	NVIC_Init(&NVIC_InitStruct);

	leftFrontUS.init(GPIOA, GPIO_InitStruct, EXTI_InitStruct);//On donne les paramètres de la pin et de l'interruption au capteur pour qu'il puisse les modifier sans faire d'erreur



	/*
	 * Capteur Arrière Droit : PA7
	 */

	/* Activation de l'horloge du port GPIOA */
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);

	/* Activation de l'horloge du SYSCFG */
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_SYSCFG, ENABLE);

	/*Réglages de la pin*/
	GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN;
	GPIO_InitStruct.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStruct.GPIO_Pin = GPIO_Pin_7;
	GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_UP;
	GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_Init(GPIOA, &GPIO_InitStruct);

	/* Tell system that you will use PA7 for EXTI_Line7 */
	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOA, EXTI_PinSource7);

	/* PA7 is connected to EXTI_Line7 */
	EXTI_InitStruct.EXTI_Line = EXTI_Line7;
	/* Enable interrupt */
	EXTI_InitStruct.EXTI_LineCmd = DISABLE;
	/* Interrupt mode */
	EXTI_InitStruct.EXTI_Mode = EXTI_Mode_Interrupt;
	/* Triggers on rising and falling edge */
	EXTI_InitStruct.EXTI_Trigger = EXTI_Trigger_Rising;
	/* Add to EXTI */
	EXTI_Init(&EXTI_InitStruct);

	/* Add IRQ vector to NVIC */
	/* PA7 is connected to EXTI_Line7, which has EXTI9_5_IRQn vector */
	NVIC_InitStruct.NVIC_IRQChannel = EXTI9_5_IRQn;
	/* Set priority */
	NVIC_InitStruct.NVIC_IRQChannelPreemptionPriority = 0x00;
	/* Set sub priority */
	NVIC_InitStruct.NVIC_IRQChannelSubPriority = 0x02;
	/* Enable interrupt */
	NVIC_InitStruct.NVIC_IRQChannelCmd = ENABLE;
	/* Add to NVIC */
	NVIC_Init(&NVIC_InitStruct);

	rightBackUS.init(GPIOA, GPIO_InitStruct, EXTI_InitStruct);//On donne les paramètres de la pin et de l'interruption au capteur pour qu'il puisse les modifier sans faire d'erreur



	/*
	 * Capteur Arrière Gauche : PB1
	 */

	/* Activation de l'horloge du port GPIOB */
	RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOB, ENABLE);

	/* Activation de l'horloge du SYSCFG */
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_SYSCFG, ENABLE);

	/*Réglages de la pin*/
	GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN;
	GPIO_InitStruct.GPIO_OType = GPIO_OType_PP;
	GPIO_InitStruct.GPIO_Pin = GPIO_Pin_1;
	GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_UP;
	GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
	GPIO_Init(GPIOB, &GPIO_InitStruct);

	/* Tell system that you will use PB1 for EXTI_Line1 */
	SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOB, EXTI_PinSource1);

	/* PB1 is connected to EXTI_Line1 */
	EXTI_InitStruct.EXTI_Line = EXTI_Line1;
	/* Enable interrupt */
	EXTI_InitStruct.EXTI_LineCmd = DISABLE;
	/* Interrupt mode */
	EXTI_InitStruct.EXTI_Mode = EXTI_Mode_Interrupt;
	/* Triggers on rising and falling edge */
	EXTI_InitStruct.EXTI_Trigger = EXTI_Trigger_Rising;
	/* Add to EXTI */
	EXTI_Init(&EXTI_InitStruct);

	/* Add IRQ vector to NVIC */
	/* PB1 is connected to EXTI_Line1, which has EXTI1_IRQn vector */
	NVIC_InitStruct.NVIC_IRQChannel = EXTI1_IRQn;
	/* Set priority */
	NVIC_InitStruct.NVIC_IRQChannelPreemptionPriority = 0x00;
	/* Set sub priority */
	NVIC_InitStruct.NVIC_IRQChannelSubPriority = 0x03;
	/* Enable interrupt */
	NVIC_InitStruct.NVIC_IRQChannelCmd = ENABLE;
	/* Add to NVIC */
	NVIC_Init(&NVIC_InitStruct);

	leftBackUS.init(GPIOB, GPIO_InitStruct, EXTI_InitStruct);//On donne les paramètres de la pin et de l'interruption au capteur pour qu'il puisse les modifier sans faire d'erreur


}

/*
 * Fonction de mise à jour des capteurs à ultrason
 */
void SensorMgr::refresh(MOVING_DIRECTION direction, bool moving)
{
	static uint8_t capteur = 0;
	currentTime = Millis();

	if(currentTime - lastRefreshTime >= refreshDelay)
	{
		if(capteur == 0 && (direction == FORWARD || !moving)) {
			leftFrontUS.refresh();
		} else if(capteur == 1 && (direction == BACKWARD || !moving)) {
			rightBackUS.refresh();
		}
		else if(capteur == 2 && (direction == FORWARD || !moving)) {
			rightFrontUS.refresh();
		} else if(capteur == 3 && (direction == BACKWARD  || !moving))
			leftBackUS.refresh();

		capteur = (capteur+1)%4;
		lastRefreshTime = currentTime;
	}
}


/*
 * Fonctions d'interruption des capteurs à ultrason
 */

void SensorMgr::rightFrontUSInterrupt(){
	rightFrontUS.interruption();

}

void SensorMgr::leftFrontUSInterrupt(){
	leftFrontUS.interruption();
}

void SensorMgr::rightBackUSInterrupt(){
	rightBackUS.interruption();
}

void SensorMgr::leftBackUSInterrupt(){
	leftBackUS.interruption();
}


/*
 * Fonctions de récupération de la distance mesurée
 */

int SensorMgr::getRightFrontValue() {
	return rightFrontUS.value();
}

int SensorMgr::getLeftFrontValue() {
	return leftFrontUS.value();
}

int SensorMgr::getRightBackValue() {
	return rightBackUS.value();
}

int SensorMgr::getLeftBackValue() {
	return leftBackUS.value();
}


/*
 * Fonctions de récupération de l'état des capteurs de contact et du jumper
 */

bool SensorMgr::isPlotInside() const{
	return GPIO_ReadInputDataBit(GPIOC, GPIO_Pin_15);
}

bool SensorMgr::isRightGlassInside() const{
	return GPIO_ReadInputDataBit(GPIOD, GPIO_Pin_9);
}

bool SensorMgr::isLeftGlassInside() const{
	return GPIO_ReadInputDataBit(GPIOD, GPIO_Pin_11);
}

bool SensorMgr::isJumperOut() const{
	return !GPIO_ReadInputDataBit(GPIOC, GPIO_Pin_9);
}
