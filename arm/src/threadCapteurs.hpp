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
	while(!ping)
		vTaskDelay(10);
	while(!matchDemarre)
	{
		// Lecture des boutons : couleur, coquillage, etc
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
