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
void thread_capteurs(void* p)
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

	GPIO_InitTypeDef GPIO_InitStruct;

	GPIO_InitStruct.Pin = GPIO_PIN_13;
	GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
	GPIO_InitStruct.Speed = GPIO_SPEED_FAST;
	GPIO_InitStruct.Pull = GPIO_NOPULL;
	HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);

	HAL_GPIO_WritePin(GPIOC, GPIO_PIN_13, GPIO_PIN_SET);

	uint8_t codeCoquillage = 0;
	bool balisePresente = false;
	bool symetrie = false; // symétrie false : vert. symétrie true : violet.

	sendBalise(balisePresente);
	sendCouleur(symetrie);
	sendCoquillage(codeCoquillage);

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
		 * - diode couleur verte B8
		 * - diode couleur bleue B9
		 * - batterie (D0 à D4)
		 * - ping raspberry C15
		 */


	}
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
//		sendCapteur(x, y, orientation, courbure, marcheAvantTmp, 0);
		vTaskDelay(10);

//		sendDebug(10, 0, 0, 0, 0, 0, 0, 0); // debug

	}

}

#endif
