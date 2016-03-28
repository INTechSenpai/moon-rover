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

void inline ledLipo(uint32_t tensionLipo)
{
	if(tensionLipo > 4200)
	{
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_SET);
	}
	else if(tensionLipo > 3400)
	{
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else if(tensionLipo > 2600)
	{
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else if(tensionLipo > 1800)
	{
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else if(tensionLipo > 1000)
	{
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
	else
	{
		// buzzer
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_0, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_1, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_2, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_3, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_4, GPIO_PIN_RESET);
	}
}

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
	 * Configuration des capteurs analogiques
	 */

    GPIO_InitTypeDef gpioInit;

    __ADC1_CLK_ENABLE();

    // Pin analogiques : A0 A1 A2 A3 A4 A5 A6 A7 B0 B1 C0 C1 C2 C3 C4 C5

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
    g_AdcHandle.Init.ScanConvMode = ENABLE;
    g_AdcHandle.Init.ContinuousConvMode = ENABLE;
    g_AdcHandle.Init.DiscontinuousConvMode = DISABLE;
    g_AdcHandle.Init.NbrOfDiscConversion = 0;
    g_AdcHandle.Init.ExternalTrigConvEdge = ADC_EXTERNALTRIGCONVEDGE_NONE;
    g_AdcHandle.Init.ExternalTrigConv = ADC_EXTERNALTRIGCONV_T1_CC1;
    g_AdcHandle.Init.DataAlign = ADC_DATAALIGN_RIGHT;
    g_AdcHandle.Init.NbrOfConversion = 16;
    g_AdcHandle.Init.DMAContinuousRequests = ENABLE;
    g_AdcHandle.Init.EOCSelection = DISABLE;

    HAL_ADC_Init(&g_AdcHandle);

    adcChannel.Channel = ADC_CHANNEL_0;
    adcChannel.Rank = 1;
    adcChannel.SamplingTime = ADC_SAMPLETIME_480CYCLES;
    adcChannel.Offset = 0;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_1;
    adcChannel.Rank = 2;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_2;
    adcChannel.Rank = 3;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_3;
    adcChannel.Rank = 4;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_4;
    adcChannel.Rank = 5;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_5;
    adcChannel.Rank = 6;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_6;
    adcChannel.Rank = 7;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_7;
    adcChannel.Rank = 8;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_8;
    adcChannel.Rank = 9;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_9;
    adcChannel.Rank = 10;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_10;
    adcChannel.Rank = 11;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_11;
    adcChannel.Rank = 12;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_12;
    adcChannel.Rank = 13;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_13;
    adcChannel.Rank = 14;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_14;
    adcChannel.Rank = 15;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);
    adcChannel.Channel = ADC_CHANNEL_15;
    adcChannel.Rank = 16;
    HAL_ADC_ConfigChannel(&g_AdcHandle, &adcChannel);

    HAL_ADC_Start(&g_AdcHandle);

    uint16_t capteurs[14];

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


	GPIO_InitStruct.Pin = GPIO_PIN_13; // TEST
    HAL_GPIO_Init(GPIOD, &GPIO_InitStruct);
/*
while(true)
{
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_13, GPIO_PIN_SET);
	vTaskDelay(500);
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_13, GPIO_PIN_RESET);
	vTaskDelay(500);
}
*/

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

/*
	GPIO_InitStruct.Pin = GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_3; // B13 et B15
	HAL_GPIO_Init(GPIOB, &GPIO_InitStruct);
	GPIO_InitStruct.Pin = GPIO_PIN_15; // B13 et B15
	HAL_GPIO_Init(GPIOA, &GPIO_InitStruct);

	while(true)
	{
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_1, HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_3));
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_2, HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_4));
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_3, HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_5));
		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_4, HAL_GPIO_ReadPin(GPIOA, GPIO_PIN_15));
	}
*/

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
//	while(!matchDemarre)
	while(false)
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

		if(HAL_GPIO_ReadPin(GPIOD, GPIO_PIN_13) == GPIO_PIN_RESET)
		{
			HookTemps::setDateDebutMatch();
			sendDebutMatch();
			matchDemarre = true;
		}
		else
			vTaskDelay(50);
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

		if(HAL_ADC_PollForConversion(&g_AdcHandle, 1000000) == HAL_OK)
			HAL_ADC_GetValue(&g_AdcHandle);// ADC en rab
		if(HAL_ADC_PollForConversion(&g_AdcHandle, 1000000) == HAL_OK)
			ledLipo(HAL_ADC_GetValue(&g_AdcHandle));

		for(int i = 0; i < 14; i++)
			if(HAL_ADC_PollForConversion(&g_AdcHandle, 1000000) == HAL_OK)
				capteurs[i] = HAL_ADC_GetValue(&g_AdcHandle);

		sendCapteur(x, y, orientation, courbure, marcheAvantTmp, capteurs);
		vTaskDelay(300);

	}

}

#endif
