#ifndef TH_ODO_ASSER
#define TH_ODO_ASSER

#include <stdio.h>
#include <stdlib.h>
#include <vector>
#include "diag/Trace.h"

#include "math.h"
#include "Timer.h"
#include "FreeRTOS.h"
#include "task.h"
#include "Hook.h"
#include "Uart.hpp"
#include "global.h"
#include "ax12.hpp"
#include "serialProtocol.h"
#include "asserSimple.hpp"
#include "serie.h"

using namespace std;


/**
 * Thread d'odométrie et d'asservissement
 */
void thread_odometrie_asser(void*)
{
	uint8_t debugCompteur = 0;

	int16_t positionGauche[MEMOIRE_MESURE_INT]; // on introduit un effet de mémoire afin de pouvoir mesurer la vitesse sur un intervalle pas trop petit
	int16_t positionDroite[MEMOIRE_MESURE_INT];
//	int16_t vitesseGauche[MEMOIRE_MESURE]; // on introduit un effet de mémoire afin de pouvoir mesurer l'accélération sur un intervalle pas trop petit
//	int16_t vitesseDroite[MEMOIRE_MESURE];
	uint8_t indiceMemoire = 0;
	x_odo = 0;
	y_odo = 0;
	orientation_odo = 0;
	courbure_odo = 0;
	currentRightSpeed = 0;
	currentLeftSpeed = 0;
	currentAngle = 0;
	uint32_t orientationMoyTick = 0;;
	uint16_t old_tick_gauche = TICK_CODEUR_GAUCHE, old_tick_droit = TICK_CODEUR_DROIT, tmp;
	int16_t distanceTick, delta_tick_droit, delta_tick_gauche, deltaOrientationTick;
	double k, distance, deltaOrientation;

	for(int i = 0; i < MEMOIRE_MESURE_INT; i++)
	{
		positionDroite[i] = old_tick_droit;
		positionGauche[i] = old_tick_gauche;
//		vitesseDroite[i] = 0;
//		vitesseGauche[i] = 0;

	}

	TIM_Encoder_InitTypeDef encoder, encoder2;
	TIM_HandleTypeDef timer, timer2;

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

/*
	while(1)
	{
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_10, GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12, GPIO_PIN_SET);
		for(uint32_t i = 0; i < 50; i++)
		{
			TIM8->CCR1 = 6*i;
			TIM8->CCR2 = 6*i;
			vTaskDelay(10);
		}
		for(uint32_t i = 50; i > 0; i--)
		{
			TIM8->CCR1 = 6*i;
			TIM8->CCR2 = 6*i;
			vTaskDelay(10);
		}

		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_10, GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12, GPIO_PIN_RESET);
		for(uint32_t i = 0; i < 50; i++)
		{
			TIM8->CCR1 = 6*i;
			TIM8->CCR2 = 6*i;
			vTaskDelay(10);
		}
		for(uint32_t i = 50; i > 0; i--)
		{
			TIM8->CCR1 = 6*i;
			TIM8->CCR2 = 6*i;
			vTaskDelay(10);
		}
	}
*/


	// On attend l'initialisation de xyo avant de démarrer l'odo, sinon ça casse tout.
	while(!startOdo)
		vTaskDelay(5);

	currentAngle = RAD_TO_TICK(orientation_odo);

	TickType_t xLastWakeTime;
	const TickType_t periode = 1000 / FREQUENCE_ODO_ASSER;
	xLastWakeTime = xTaskGetTickCount();

	while(1)
	{
		vTaskDelayUntil(&xLastWakeTime, periode);

		// ODOMÉTRIE
		while(xSemaphoreTake(odo_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);

		// La formule d'odométrie est corrigée pour tenir compte des trajectoires
		// (au lieu d'avoir une approximation linéaire, on a une approximation circulaire)
		tmp = TICK_CODEUR_GAUCHE;
		delta_tick_gauche = tmp - old_tick_gauche;
		old_tick_gauche = tmp;
		currentLeftSpeed = (tmp - positionGauche[indiceMemoire]) / MEMOIRE_MESURE;
//		currentLeftAcceleration = (currentLeftSpeed - vitesseGauche[indiceMemoire]) / MEMOIRE_MESURE;
		positionGauche[indiceMemoire] = tmp;
//		vitesseGauche[indiceMemoire] = currentLeftSpeed;

		tmp = TICK_CODEUR_DROIT;
		delta_tick_droit = tmp - old_tick_droit;
		old_tick_droit = tmp;
		currentRightSpeed = (tmp - positionDroite[indiceMemoire]) / MEMOIRE_MESURE;
//		currentRightAcceleration = (currentRightSpeed - vitesseDroite[indiceMemoire]) / MEMOIRE_MESURE;
		positionDroite[indiceMemoire] = tmp;
//		vitesseDroite[indiceMemoire] = currentLeftSpeed;

		// Test jumper
//		HAL_GPIO_WritePin(GPIOE, GPIO_PIN_1, HAL_GPIO_ReadPin(GPIOD, GPIO_PIN_13));

		vitesseLineaireReelle = (currentRightSpeed + currentLeftSpeed) / 2;

		if(!isSymmetry)
			vitesseRotationReelle = (currentRightSpeed - currentLeftSpeed) / 2;
		else
			vitesseRotationReelle = (currentLeftSpeed - currentRightSpeed) / 2;

		// Calcul issu de Thalès. Positif si le robot tourne vers la droite (pour être cohérent avec l'orientation)
		courbure_odo = 2 / LONGUEUR_CODEUSE_A_CODEUSE_EN_MM * (currentLeftSpeed - currentRightSpeed) / (currentLeftSpeed + currentRightSpeed);

		indiceMemoire++;
		indiceMemoire %= MEMOIRE_MESURE_INT;

		// on évite les formules avec "/ 2", qui font perdre de l'information et qui peuvent s'accumuler

		distanceTick = delta_tick_droit + delta_tick_gauche;
		distance = TICK_TO_MM(distanceTick);

		// gestion de la symétrie : en effet, toutes les variables sont symétrisées, y compris l'orientation
		if(!isSymmetry)
			deltaOrientationTick = delta_tick_droit - delta_tick_gauche;
		else
			deltaOrientationTick = delta_tick_gauche - delta_tick_droit;

		// l'erreur à cause du "/2" ne s'accumule pas
		orientationMoyTick = currentAngle + deltaOrientationTick/2;

		if(orientationMoyTick > (uint32_t)TICKS_PAR_TOUR_ROBOT)
		{
			if(orientationMoyTick < (uint32_t)FRONTIERE_MODULO)
				orientationMoyTick -= (uint32_t)TICKS_PAR_TOUR_ROBOT;
			else
				orientationMoyTick += (uint32_t)TICKS_PAR_TOUR_ROBOT;
		}
		currentAngle += deltaOrientationTick;
		deltaOrientation = TICK_TO_RAD(deltaOrientationTick);

//		serial_rb.printfln("TICKS_PAR_TOUR_ROBOT = %d", (int)TICKS_PAR_TOUR_ROBOT);
//		serial_rb.printfln("orientationMoyTick = %d", orientationMoyTick);
//		serial_rb.printfln("orientation = %d", (int)(orientation_odo*1000));

		if(deltaOrientationTick == 0) // afin d'éviter la division par 0
			k = 1.;
		else
			k = sin(deltaOrientation/2)/(deltaOrientation/2);

		if(distance == 0) //  ça va arriver quand on fait par exemple une rotation sur place.
			courbure_odo = 0;
		else
			courbure_odo = deltaOrientationTick / distance;

		orientation_odo = TICK_TO_RAD(orientationMoyTick);
        cos_orientation_odo = cos(orientation_odo);
        sin_orientation_odo = sin(orientation_odo);

		x_odo += k*distance*cos_orientation_odo;
		y_odo += k*distance*sin_orientation_odo;
		xSemaphoreGive(odo_mutex);


		if(debugMode)
		{
			if((debugCompteur & 0x07) == 0)
				sendDebug(leftPWM, rightPWM, (int32_t)(currentLeftSpeed*100), (int32_t)(currentRightSpeed*100), (int32_t)(errorLeftSpeed*100), (int32_t)(errorRightSpeed*100), vitesseLineaireReelle, courbureReelle);
//				sendDebug(leftPWM, rightPWM, (int32_t)(currentLeftSpeed*100), (int32_t)(currentRightSpeed*100), errorTranslation, errorAngle, vitesseLineaireReelle, courbureReelle);
			debugCompteur++;
		}

		// ASSERVISSEMENT
        if(checkBlocageMecanique())
        {
            modeAsserActuel = STOP;
            sendProblemeMeca();
        }

        modeAsserActuel = ASSER_VITESSE;

		// on empêche toute modification de consigne
		while(xSemaphoreTake(consigneAsser_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);
		if(modeAsserActuel == ROTATION)
			controlRotation();
		else if(modeAsserActuel == STOP)
			controlStop();
		else if(modeAsserActuel == VA_AU_POINT)
			controlVaAuPoint();
		else if(modeAsserActuel == COURBE)
			controlTrajectoire();
		else if(modeAsserActuel == ASSER_VITESSE)
			controlVitesse();
		if(needArrive && checkArrivee()) // gestion de la fin du mouvement
		{
			needArrive = false;
			modeAsserActuel = VA_AU_POINT;
			consigneX = x_odo;
			consigneY = y_odo;
			sendArrive();
		}
		// si ça vaut ASSER_OFF, il n'y a pas d'asser
		xSemaphoreGive(consigneAsser_mutex);

	}
}

#endif
