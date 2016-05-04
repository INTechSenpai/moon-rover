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
 * Correspondance entre la tension et la valeur de l'ADC
 * valeur lue = 1341*V+24
 */

uint8_t delay = 0;

void inline ledLipo(uint32_t tensionLipo)
{
	delay++;
	if(tensionLipo > 3528) // 24V
	{
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_SET);
	}
	else if(tensionLipo > 3455) // 23.5V
	{
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_SET);
		if((delay & 0x02) == 0)
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_SET);
		else
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else if(tensionLipo > 3382) // 23V
	{
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else if(tensionLipo > 3309) // 22.5V
	{
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_SET);
		if((delay & 0x02) == 0)
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_SET);
		else
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else if(tensionLipo > 3236) // 22V
	{
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else if(tensionLipo > 3163) // 21.5V
	{
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		if((delay & 0x02) == 0)
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_SET);
		else
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else if(tensionLipo > 3090) // 21V
	{
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else if(tensionLipo > 3017) // 20.5V
	{
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		if((delay & 0x02) == 0)
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		else
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else if(tensionLipo > 2944) // 20V
	{
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else if(tensionLipo > 1484) // tension trop basse : allumage du buzzer
	{
		if((delay & 0x02) == 0)
		{
			HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_SET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_SET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_SET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_RESET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_RESET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_RESET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
		}
	}
	else // pas de lipo branch�e
	{
		if((delay & 0x02) == 0)
		{
			HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_RESET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_SET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_SET);
		}
		else
		{
			HAL_GPIO_WritePin(GPIOE, GPIO_PIN_0, GPIO_PIN_RESET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_RESET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_RESET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_SET);
			HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
		}
	}


}

/**
 * Thread des capteurs
 */
void thread_capteurs(void*)
{

	/**
	 * Configuration des capteurs analogiques
	 */

    GPIO_InitTypeDef gpioInit;

    __ADC1_CLK_ENABLE();

    // Pin analogiques :�A0 A1 A2 A3 A4 A5 A6 A7 B0 B1 C0 C1 C2 C3 C4 C5
    // (A0 n'est pas utilisé)

    gpioInit.Pin = GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_6 | GPIO_PIN_7;
    gpioInit.Mode = GPIO_MODE_ANALOG;
    gpioInit.Pull = GPIO_NOPULL;
    HAL_GPIO_Init(GPIOA, &gpioInit);

    gpioInit.Pin = GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4 | GPIO_PIN_5;
    HAL_GPIO_Init(GPIOC, &gpioInit);

    gpioInit.Pin = GPIO_PIN_0 | GPIO_PIN_1;
    HAL_GPIO_Init(GPIOB, &gpioInit);

    HAL_NVIC_SetPriority(ADC_IRQn, 0, 0);
    HAL_NVIC_EnableIRQ(ADC_IRQn);

    ADC_ChannelConfTypeDef adcChannel;
    ADC_HandleTypeDef g_AdcHandle;

    g_AdcHandle.Instance = ADC1;

    g_AdcHandle.Init.ClockPrescaler = ADC_CLOCKPRESCALER_PCLK_DIV2;
    g_AdcHandle.Init.Resolution = ADC_RESOLUTION_12B;
    g_AdcHandle.Init.ScanConvMode = DISABLE;
    g_AdcHandle.Init.ContinuousConvMode = ENABLE;
    g_AdcHandle.Init.DiscontinuousConvMode = DISABLE;
    g_AdcHandle.Init.NbrOfDiscConversion = 0;
    g_AdcHandle.Init.ExternalTrigConvEdge = ADC_EXTERNALTRIGCONVEDGE_NONE;
    g_AdcHandle.Init.ExternalTrigConv = ADC_EXTERNALTRIGCONV_T1_CC1;
    g_AdcHandle.Init.DataAlign = ADC_DATAALIGN_RIGHT;
    g_AdcHandle.Init.NbrOfConversion = 1;
    g_AdcHandle.Init.DMAContinuousRequests = ENABLE;
    g_AdcHandle.Init.EOCSelection = DISABLE;

    HAL_ADC_Init(&g_AdcHandle);

    adcChannel.Rank = 1;
    adcChannel.SamplingTime = ADC_SAMPLETIME_480CYCLES;
    adcChannel.Offset = 0;
    // Pin analogiques :�A0 A1 A2 A3 A4 A5 A6 A7 B0 B1 C0 C1 C2 C3 C4 C5

    uint32_t canaux[] = {
    		ADC_CHANNEL_1, // Lipo
			ADC_CHANNEL_11, // IR avant droite
			ADC_CHANNEL_15, // IR avant gauche
			ADC_CHANNEL_4, // IR arrière droit
			ADC_CHANNEL_5, // IR arrière gauche
			ADC_CHANNEL_9, // IR objet devant
			ADC_CHANNEL_10, // IR objet devant
			ADC_CHANNEL_6, // IR objet arrière
			ADC_CHANNEL_7}; // IR objet arrière

    uint16_t capteurs[8];

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
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_1, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_2, GPIO_PIN_SET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_3, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_4, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOE, GPIO_PIN_5, GPIO_PIN_RESET);

	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);

	HAL_GPIO_WritePin(GPIOB, GPIO_PIN_8, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(GPIOB, GPIO_PIN_9, GPIO_PIN_SET);

	/**
	 * Configuration des entr�es
	 */

	GPIO_InitStruct.Mode = GPIO_MODE_INPUT;
	GPIO_InitStruct.Speed = GPIO_SPEED_FAST;
	GPIO_InitStruct.Pull = GPIO_PULLUP;

	GPIO_InitStruct.Pin = GPIO_PIN_13 | GPIO_PIN_15; // B13 et B15
	HAL_GPIO_Init(GPIOB, &GPIO_InitStruct);

	GPIO_InitStruct.Pin = GPIO_PIN_11 | GPIO_PIN_13 | GPIO_PIN_15; // D11, D13 et D15
	HAL_GPIO_Init(GPIOD, &GPIO_InitStruct);

	uint8_t codeCoquillage = 0;
	GPIO_PinState coquillageBouton = GPIO_PIN_SET;
	bool balisePresente = false;
	GPIO_PinState balisePresenteBouton = GPIO_PIN_SET;
	GPIO_PinState symetrieBouton = GPIO_PIN_SET;

	// On attend d'avoir la communication �tablie avant d'envoyer les param�tres
	while(!ping)
		vTaskDelay(10);

	vTaskDelay(200);

	sendBalise(balisePresente);
	sendCouleur(isSymmetry);
	sendCoquillage(codeCoquillage);

	/**
	 * Les leds : SET pour allumer, RESET pour �teindre
	 * Interrupteurs :�pull-up
	 */
	GPIO_PinState tmp;

    adcChannel.Channel = canaux[0];
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    HAL_ADC_Start(&g_AdcHandle);

    bool rabEnvoye = false;

	while(!matchDemarre)
	{
		/**
		 * Input :
		 * - coquillage B13
		 * - couleur robot B15
		 * - jumper D13
		 * - balise pr�sente D15
		 *
		 * Output :
		 * - balise pr�sente C13
		 * - coquillage 1 (E1) 2 (E2) 3 (E3) 4 (E4) 5 (E5)
		 * - buzzer (E0)
		 * - diode couleur verte B8
		 * - diode couleur bleue B9
		 * - batterie (D0 � D4)
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
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_2, GPIO_PIN_RESET);
				else
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_2, GPIO_PIN_SET);
				if(codeCoquillage != 1)
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_5, GPIO_PIN_RESET);
				else
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_5, GPIO_PIN_SET);
				if(codeCoquillage != 2)
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_3, GPIO_PIN_RESET);
				else
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_3, GPIO_PIN_SET);
				if(codeCoquillage != 3)
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_1, GPIO_PIN_RESET);
				else
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_1, GPIO_PIN_SET);
				if(codeCoquillage != 4)
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_4, GPIO_PIN_RESET);
				else
					HAL_GPIO_WritePin(GPIOE, GPIO_PIN_4, GPIO_PIN_SET);
			}
		}

		// couleur

		tmp = HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_15);
		if(!rabEnvoye && tmp != symetrieBouton)
		{
			symetrieBouton = tmp;
			if(symetrieBouton == GPIO_PIN_RESET)
			{
				isSymmetry = !isSymmetry;
				sendCouleur(isSymmetry);
				if(isSymmetry)
				{
					HAL_GPIO_WritePin(GPIOB, GPIO_PIN_9, GPIO_PIN_RESET);
					HAL_GPIO_WritePin(GPIOB, GPIO_PIN_8, GPIO_PIN_SET);
				}
				else
				{
					HAL_GPIO_WritePin(GPIOB, GPIO_PIN_9, GPIO_PIN_SET);
					HAL_GPIO_WritePin(GPIOB, GPIO_PIN_8, GPIO_PIN_RESET);
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

		//�Jumper
		if(rabEnvoye && HAL_GPIO_ReadPin(GPIOD, GPIO_PIN_13) == GPIO_PIN_RESET)
		{
			HookTemps::setDateDebutMatch();
			sendDebutMatch();
			matchDemarre = true;
		}
		else
			vTaskDelay(50);

		// Bouton en rab. Initialise l'odo
		if(!rabEnvoye && HAL_GPIO_ReadPin(GPIOD, GPIO_PIN_11) == GPIO_PIN_RESET)
		{
			HAL_GPIO_WritePin(GPIOC, GPIO_PIN_15, GPIO_PIN_SET);
			rabEnvoye = true;
			sendRab();
		}

		// Affichage lipo
		if(HAL_ADC_PollForConversion(&g_AdcHandle, 1000000) == HAL_OK)
			ledLipo(HAL_ADC_GetValue(&g_AdcHandle));

	}

    HAL_ADC_Stop(&g_AdcHandle);

	while(1)
	{
		uint16_t x, y, orientation;
		uint8_t courbure;
		bool marcheAvantTmp;
		// l'envoi s�rie n'est pas fait quand on a le mutex d'odo afin d'�viter de ralentir le thread d'odo
		while(xSemaphoreTake(odo_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);
			x = (uint16_t) (x_odo + 1500);
			y = (uint16_t) y_odo;
			orientation = (orientation_odo*1000);
			courbure = (uint8_t) courbure_odo;
			marcheAvantTmp = marcheAvant;
		xSemaphoreGive(odo_mutex);

	    adcChannel.Channel = canaux[0];
//	    adcChannel.Channel = ADC_CHANNEL_11;
	    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
	    HAL_ADC_Start(&g_AdcHandle);

		if(HAL_ADC_PollForConversion(&g_AdcHandle, 1000000) == HAL_OK)
			ledLipo(HAL_ADC_GetValue(&g_AdcHandle));

	    HAL_ADC_Stop(&g_AdcHandle);

		for(uint8_t i = 0; i < 8; i++)
		{
		    adcChannel.Channel = canaux[i+1];
		    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
		    HAL_ADC_Start(&g_AdcHandle);

			if(HAL_ADC_PollForConversion(&g_AdcHandle, 1000000) == HAL_OK)
				capteurs[i] = HAL_ADC_GetValue(&g_AdcHandle);

		    HAL_ADC_Stop(&g_AdcHandle);
		}

		sendCapteur(x, y, orientation, courbure, marcheAvantTmp, (int16_t) vitesseLineaireReelle, (int16_t) vitesseRotationReelle, capteurs);
		vTaskDelay(50);

	}

}

#endif
