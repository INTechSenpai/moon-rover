#ifndef TH_CAPT
#define TH_CAPT

#include <stdio.h>
#include <stdlib.h>
#include <vector>
#include "diag/Trace.h"

#include "math.h"
#include "Timer.h"
#include "FreeRTOS.h"
#include "task.h"
#include "global.h"
#include "serie.h"

using namespace std;

/**
 * Thread des capteurs
 */
void thread_capteurs(void*)
{
/*
	while(1)
		for(uint32_t i = 0; i < 80; i++)
		{
			TIM8->CCR1 = 100*i;
			TIM8->CCR2 = 100*i;
			vTaskDelay(300);
		}
*/

	/**
	 * Configuration des sorties
	 */

	GPIO_InitTypeDef GPIO_InitStruct;
	GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
	GPIO_InitStruct.Speed = GPIO_SPEED_FAST;
	GPIO_InitStruct.Pull = GPIO_NOPULL;

	GPIO_InitStruct.Pin = GPIO_PIN_13 | GPIO_PIN_15; // C13 et C15
	HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);

	GPIO_InitStruct.Pin = GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4 | GPIO_PIN_5; // E0 E1 E2 E3 E4 E5
	HAL_GPIO_Init(GPIOE, &GPIO_InitStruct);

	GPIO_InitStruct.Pin = GPIO_PIN_8 | GPIO_PIN_9; // B8 et B9
	HAL_GPIO_Init(GPIOB, &GPIO_InitStruct);

	GPIO_InitStruct.Pin = GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4; // D0 D1 D2 D3 D4
	HAL_GPIO_Init(GPIOD, &GPIO_InitStruct);

	HAL_GPIO_WritePin(GPIOC, GPIO_PIN_13, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOC, GPIO_PIN_15, GPIO_PIN_RESET);

	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_1, GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_2, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_3, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_4, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_5, GPIO_PIN_RESET);

	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);

	HAL_GPIO_WritePin(GPIOB, GPIO_PIN_8, GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOB, GPIO_PIN_9, GPIO_PIN_RESET);

	/**
	 * Configuration des entrées
	 */

	GPIO_InitStruct.Mode = GPIO_MODE_INPUT;
	GPIO_InitStruct.Speed = GPIO_SPEED_FAST;
	GPIO_InitStruct.Pull = GPIO_PULLUP;

	GPIO_InitStruct.Pin = GPIO_PIN_13 | GPIO_PIN_15; // B13 et B15
	HAL_GPIO_Init(GPIOB, &GPIO_InitStruct);

	GPIO_InitStruct.Pin = GPIO_PIN_13 | GPIO_PIN_15; // D13 et D15
	HAL_GPIO_Init(GPIOD, &GPIO_InitStruct);

	uint8_t codeCoquillage = 0;
	GPIO_PinState coquillageBouton = GPIO_PIN_SET;
	bool balisePresente = false;
	GPIO_PinState balisePresenteBouton = GPIO_PIN_SET;
	bool symetrie = false; // symétrie false : vert. symétrie true : violet.
	GPIO_PinState symetrieBouton = GPIO_PIN_SET;

	// On attend d'avoir la communication établie avant d'envoyer les paramètres
	while(!ping)
		vTaskDelay(10);

	vTaskDelay(200);

	sendBalise(balisePresente);
	sendCouleur(symetrie);
	sendCoquillage(codeCoquillage);

	/**
	 * Les leds : SET pour allumer, RESET pour éteindre
	 * Interrupteurs : pull-up
	 */
	GPIO_PinState tmp;
	while(!matchDemarre)
	{
		/**
		 * Input :
		 * - coquillage B13
		 * - couleur robot B15
		 * - jumper D13
		 * - balise présente D15
		 *
		 * Output :
		 * - balise présente C13
		 * - coquillage 1 (E1) 2 (E2) 3 (E3) 4 (E4) 5 (E5)
		 * - buzzer (E0)
		 * - diode couleur verte B8
		 * - diode couleur bleue B9
		 * - batterie (D0 à D4)
		 * - ping raspberry C15
		 */

		// coquillage

		tmp = HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_13);
		if(tmp != coquillageBouton)
		{
			coquillageBouton = tmp;
			if(coquillageBouton == GPIO_PIN_RESET)
			{
				codeCoquillage++;
				codeCoquillage %= 5;
				sendCoquillage(codeCoquillage);
				if(codeCoquillage != 0)
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_1, GPIO_PIN_RESET);
				else
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_1, GPIO_PIN_SET);
				if(codeCoquillage != 1)
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_2, GPIO_PIN_RESET);
				else
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_2, GPIO_PIN_SET);
				if(codeCoquillage != 2)
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_3, GPIO_PIN_RESET);
				else
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_3, GPIO_PIN_SET);
				if(codeCoquillage != 3)
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_4, GPIO_PIN_RESET);
				else
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_4, GPIO_PIN_SET);
				if(codeCoquillage != 4)
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_5, GPIO_PIN_RESET);
				else
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_5, GPIO_PIN_SET);
			}
		}

		// couleur

		tmp = HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_15);
		if(tmp != symetrieBouton)
		{
			symetrieBouton = tmp;
			if(symetrieBouton == GPIO_PIN_RESET)
			{
				symetrie = !symetrie;
				sendCouleur(symetrie);
				if(symetrie)
				{
					HAL_GPIO_WritePin(GPIOB, GPIO_PIN_8, GPIO_PIN_RESET);
					HAL_GPIO_WritePin(GPIOB, GPIO_PIN_9, GPIO_PIN_SET);
				}
				else
				{
					HAL_GPIO_WritePin(GPIOB, GPIO_PIN_8, GPIO_PIN_SET);
					HAL_GPIO_WritePin(GPIOB, GPIO_PIN_9, GPIO_PIN_RESET);
				}
			}
		}

		// balise

		tmp = HAL_GPIO_ReadPin(GPIOD, GPIO_PIN_15);
		if(tmp != balisePresenteBouton)
		{
			balisePresenteBouton = tmp;
			if(balisePresenteBouton == GPIO_PIN_RESET)
			{
				balisePresente = !balisePresente;
				sendBalise(balisePresente);
				if(balisePresente)
					HAL_GPIO_WritePin(GPIOC, GPIO_PIN_13, GPIO_PIN_SET);
				else
					HAL_GPIO_WritePin(GPIOC, GPIO_PIN_13, GPIO_PIN_RESET);
			}
		}

		matchDemarre = HAL_GPIO_ReadPin(GPIOD, GPIO_PIN_13) == GPIO_PIN_RESET;
		vTaskDelay(50);
	}
	sendDebutMatch();
	while(1)
	{

		uint16_t x, y, orientation;
		uint8_t courbure;
		bool marcheAvantTmp;
		// l'envoi série n'est pas fait quand on a le mutex d'odo afin d'éviter de ralentir le thread d'odo
		while(xSemaphoreTake(odo_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);
			x = (uint16_t) x_odo;
			y = (uint16_t) y_odo;
			orientation = (uint16_t) orientation_odo;
			courbure = (uint8_t) courbure_odo;
			marcheAvantTmp = marcheAvant;
		xSemaphoreGive(odo_mutex);
		sendCapteur(x, y, orientation, courbure, marcheAvantTmp, 0);
		vTaskDelay(300);

//		sendDebug(10, 0, 0, 0, 0, 0, 0, 0); // debug

	}

}

#endif
