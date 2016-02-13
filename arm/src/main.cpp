//
// This file is part of the GNU ARM Eclipse distribution.
// Copyright (c) 2014 Liviu Ionescu.
//

// ----------------------------------------------------------------------------

#include <stdio.h>
#include <stdlib.h>
#include <vector>
#include "diag/Trace.h"

#include "math.h"
#include "Timer.h"
#include "FreeRTOS.h"
#include "task.h"
#include "_initialize_hardware.c"
#include "Executable.h"
#include "Hook.h"
#include "Uart.hpp"
#include "global.h"
#include "ax12.hpp"
#include "serie.h"
#include "serialProtocol.h"
using namespace std;

// ----- main() ---------------------------------------------------------------

// Sample pragmas to cope with warnings. Please note the related line at
// the end of this function, used to pop the compiler diagnostics status.
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma GCC diagnostic ignored "-Wmissing-declarations"
#pragma GCC diagnostic ignored "-Wreturn-type"


TIM_Encoder_InitTypeDef encoder, encoder2;
TIM_HandleTypeDef timer, timer2, timer3;
vector<Hook*> listeHooks;
volatile bool startOdo = false;
volatile bool matchDemarre = true; // TODO
Uart<6> serial_ax;
AX<Uart<6>>* ax12;

/**
 * Thread qui écoute la série
 */
void thread_ecoute_serie(void* p)
{
	 serial_rb.init(115200);
	 serial_ax.init(57600);
	 ax12 = new AX<Uart<6>>(0, 0, 1023);
    uint16_t idDernierPaquet = -1;

		while(1)
		{
			if(serial_rb.available())
			{
				unsigned char out[50];
				unsigned char lecture[50];
                unsigned char entete;
                uint8_t index = 0;
                // Vérification de l'entête

                serial_rb.read_char(&entete, SERIE_TIMEOUT);
                if(entete != 0x55)
                    continue;

                serial_rb.read_char(&entete, SERIE_TIMEOUT);
                if(entete != 0xAA)
                    continue;

                // Récupération de l'id
                serial_rb.read_char(lecture+index++, SERIE_TIMEOUT); // id point fort
                serial_rb.read_char(lecture+index++, SERIE_TIMEOUT); // id point faible

                uint16_t idPaquet = (lecture[ID_FORT] << 8) + lecture[ID_FAIBLE];
                
                // On redemande les paquets manquants si besoin est
                if(idPaquet > idDernierPaquet)
                {
                    idDernierPaquet++; // id paquet théoriquement reçu
                    while(idPaquet > idDernierPaquet)
                        askResend(idDernierPaquet++);
                }

				serial_rb.read_char(lecture+index++, SERIE_TIMEOUT); // lecture de la commande


				if(lecture[COMMANDE] == IN_PING)
				{
					out[3] = OUT_PONG1;
					out[4] = OUT_PONG2;
					send(out, 5+2);
				}
				else if(lecture[COMMANDE] == IN_ACTIONNEURS)
				{
					serial_rb.read_char(lecture+index++, SERIE_TIMEOUT);
					ax12->goTo(lecture[PARAM]);
				}
				else if(lecture[COMMANDE] == IN_TOURNER)
				{
					serial_rb.read_char(lecture+index++, SERIE_TIMEOUT);
					serial_rb.read_char(lecture+index++, SERIE_TIMEOUT);
					uint16_t angle = (lecture[PARAM] << 8) + lecture[PARAM + 1];
					vTaskDelay(1000);
					sendArrive();
				}
				else if((lecture[COMMANDE] & 0xFE) == IN_AVANCER)
				{
					serial_rb.read_char(lecture+index++, SERIE_TIMEOUT);
					serial_rb.read_char(lecture+index++, SERIE_TIMEOUT);
					uint16_t distance = (lecture[PARAM] << 8) + lecture[PARAM + 1];
					bool mur = lecture[COMMANDE] == IN_AVANCER_MUR;
					vTaskDelay(1000);
					sendArrive();
				}
				else if(lecture[COMMANDE] == IN_INIT_ODO)
				{
					serial_rb.read_char(lecture+index++, SERIE_TIMEOUT); // x
					serial_rb.read_char(lecture+index++, SERIE_TIMEOUT); // xy
					serial_rb.read_char(lecture+index++, SERIE_TIMEOUT); // y
					serial_rb.read_char(lecture+index++, SERIE_TIMEOUT); // o
					serial_rb.read_char(lecture+index++, SERIE_TIMEOUT); // o

					int16_t x = (lecture[PARAM] << 4) + (lecture[PARAM + 1] >> 4);
					x -= 1500;
					uint16_t y = ((lecture[PARAM + 1] & 0x0F) << 8) + lecture[PARAM + 2];
					uint16_t o = (lecture[PARAM + 3] << 8) + lecture[PARAM + 4];

					if(!startOdo)
					{
						x_odo = x;
						y_odo = y;
						orientation_odo = o/1000.;

	//					orientationTick = RAD_TO_TICK(parseInt(lecture, &(++index))/1000.);
//						serial_rb.printfln("%d",(int)orientationTick);
//						serial_rb.printfln("%d",(int)(TICK_TO_RAD(orientationTick)*1000));
						startOdo = true;
					}
//					else
//						serial_rb.printfln("ERR_ODO",(int)orientationTick);
				}

				// POUR TEST UNIQUEMENT
				else if(lecture[COMMANDE] == IN_GET_XYO)
				{
					unsigned char out[] = {0, 0, 0, 0, OUT_PONG1, OUT_PONG2, 0};

				}

				//					serial_rb.printfln("color rouge");
			}
			else // on n'attend que s'il n'y avait rien. Ainsi, si la série prend du retard elle n'attend pas pour traiter toutes les données entrantes suivantes
			{
//				vTaskDelay(1);
			}
//			serial_rb.printfln("%d", TIM5->CNT);

		}
}

/**
 * Thread qui vérifie les hooks
 */
void thread_hook(void* p)
{

	while(1)
	{
		if(indexWrite != indexRead) // tiens, un nouveau message !
		{
			char* lecture = lectures[indexRead];
			int index = 1;
			Hook* hookActuel;
			uint8_t nbcallbacks;
			uint16_t id;

			if(lecture[0] == 'H')
			{
				if(verifieSousChaine(lecture, &index, "da"))
				{
	//						serial_rb.printfln("hook de date");
					index++;
					uint32_t date = parseInt(lecture, &index);
	//						serial_rb.printfln("date : %d",date);
					index++;
					id = parseInt(lecture, &index);
					index++;
					nbcallbacks = parseInt(lecture, &index);
	//						serial_rb.printfln("nbCallback : %d",nbcallbacks);
					index++;
					hookActuel = new(pvPortMalloc(sizeof(HookTemps))) HookTemps(id, nbcallbacks, date);
					listeHooks.push_back(hookActuel);
				}
				else if(verifieSousChaine(lecture, &index, "ct"))
				{
	//						serial_rb.printfln("hook de contact");
					index++;
					uint8_t nbContact = parseInt(lecture, &index);
	//						serial_rb.printfln("nbContact : %d",nbContact);
					index++;
					bool unique = lecture[index++] == 'T';
	//						serial_rb.printfln("unique? %d", unique);
					index++;
					id = parseInt(lecture, &index);
					index++;
					nbcallbacks = parseInt(lecture, &index);
	//						serial_rb.printfln("nbCallback : %d",nbcallbacks);
					index++;
					hookActuel = new(pvPortMalloc(sizeof(HookContact))) HookContact(id, unique, nbcallbacks, nbContact);
					listeHooks.push_back(hookActuel);
				}
				else if(verifieSousChaine(lecture, &index, "dp"))
				{
	//						serial_rb.printfln("hook de demi plan");
					index++;
					uint32_t x = parseInt(lecture, &index);
	//						serial_rb.printfln("x : %d",x);
					index++;
					uint32_t y = parseInt(lecture, &index);
	//						serial_rb.printfln("y : %d",y);
					index++;
					uint32_t dir_x = parseInt(lecture, &index);
	//						serial_rb.printfln("dirx : %d",dir_x);
					index++;
					uint32_t dir_y = parseInt(lecture, &index);
	//						serial_rb.printfln("diry : %d",dir_y);
					index++;
					id = parseInt(lecture, &index);
					index++;
					nbcallbacks = parseInt(lecture, &index);
	//						serial_rb.printfln("nbCallback : %d",nbcallbacks);
					index++;
					hookActuel = new(pvPortMalloc(sizeof(HookDemiPlan))) HookDemiPlan(id, nbcallbacks, x, y, dir_x, dir_y);
					listeHooks.push_back(hookActuel);
				}
				else if(verifieSousChaine(lecture, &index, "po"))
				{
	//						serial_rb.printfln("hook de position");
					index++;
					uint32_t x = parseInt(lecture, &index);
	//						serial_rb.printfln("x : %d",x);
					index++;
					uint32_t y = parseInt(lecture, &index);
	//						serial_rb.printfln("y : %d",y);
					index++;
					uint32_t tolerance = parseInt(lecture, &index);
	//						serial_rb.printfln("tolerance : %d",tolerance);
					index++;
					id = parseInt(lecture, &index);
					index++;
					nbcallbacks = parseInt(lecture, &index);
	//						serial_rb.printfln("nbCallback : %d",nbcallbacks);
					index++;
					hookActuel = new(pvPortMalloc(sizeof(HookPosition))) HookPosition(id, nbcallbacks, x, y, tolerance);
					listeHooks.push_back(hookActuel);
				}
				else
				{
					serial_rb.printfln("Erreur de parsing hook : %s",lecture);
					indexRead++;
					indexRead %= TAILLE_BUFFER_ECRITURE_SERIE;
					continue;
				}

				for(int i = 0; i < nbcallbacks; i++)
				{
					if(verifieSousChaine(lecture, &index, "tbl"))
					{
	//							serial_rb.printfln("callback : table");
						int nbElem = parseInt(lecture, &(++index));
						index++;
	//							serial_rb.printfln("element : %d", nbElem);
						Exec_Update_Table* tmp = new(pvPortMalloc(sizeof(Exec_Update_Table))) Exec_Update_Table(nbElem);
						hookActuel->insert(tmp, i);
					}
					else if(verifieSousChaine(lecture, &index, "scr"))
					{
	//							serial_rb.printfln("callback : script");
						int nbScript = parseInt(lecture, &(++index));
						index++;
	//							serial_rb.printfln("script : %d", nbScript);
						Exec_Script* tmp = new(pvPortMalloc(sizeof(Exec_Script))) Exec_Script(nbScript);
						hookActuel->insert(tmp, i);
					}
					else if(verifieSousChaine(lecture, &index, "act"))
					{
	//							serial_rb.printfln("callback : actionneurs");
						uint8_t nbAct = parseInt(lecture, &(++index));
						uint16_t angle = parseInt(lecture, &(++index));
						index++;
	//							serial_rb.printfln("act : %d", nbAct);
						Exec_Act* tmp = new(pvPortMalloc(sizeof(Exec_Act))) Exec_Act(ax12, angle);
						hookActuel->insert(tmp, i);
					}
					else
					{
						serial_rb.printfln("Erreur de parsing callback : %s",lecture);
						break;
					}
				}
			}
			else if(lecture[0] == 'h')
			{
				if(verifieSousChaine(lecture, &index, "hkclrall"))
				{
					listeHooks.clear();
				}

				else if(verifieSousChaine(lecture, &index, "hkclr"))
				{
					index++;
					uint8_t nbIds = parseInt(lecture, &index);
					vector<Hook*>::iterator it = listeHooks.begin();

					for(uint8_t i = 0; i < nbIds; i++)
					{
						uint16_t id = parseInt(lecture, &(++index));

						for(uint8_t j = 0; j < listeHooks.size(); j++)
						{
							Hook* hook = listeHooks[j];
							if(hook->getId() == id)
							{
								vPortFree(hook);
								listeHooks[j] = listeHooks.back();
								listeHooks.pop_back();
								break; // l'id est unique
							}
						}

					}
				}
			}
//			else
//				serial_rb.printfln("Erreur parsing hook : %s", lecture);

			indexRead++;
			indexRead %= TAILLE_BUFFER_ECRITURE_SERIE;
		}

		if(matchDemarre)
		{
			for(uint8_t i = 0; i < listeHooks.size(); i++)
			{
				Hook* hook = listeHooks[i];
//				serial_rb.printfln("Eval hook");
	//			serial_rb.printfln("%d %d %d",(int)x_odo, (int)y_odo, (int)(orientation*1000));
				if((*hook).evalue())
				{
	//				serial_rb.printfln("Execution!");
					if((*hook).execute()) // suppression demandée
					{
						vPortFree(hook);
						listeHooks[i] = listeHooks.back();
						listeHooks.pop_back();
						i--;
					}
				}
			}
		}
		vTaskDelay(10);
	}
}

// TODO : les volatile
// TODO : les mutex
// TODO : tester #include "arm_math.h"


/**
 * Thread des capteurs
 */
void thread_capteurs(void* p)
{
	while(1)
	{
		uint16_t x, y, orientation, courbure, marcheAvantTmp;
		// l'envoi série n'est pas fait quand on a le mutex d'odo afin d'éviter de ralentir le thread d'odo
		while(xSemaphoreTake(odo_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);
			x = x_odo;
			y = y_odo;
			orientation = (uint16_t) orientation_odo;
			courbure = courbure_odo;
			marcheAvantTmp = marcheAvant;
		xSemaphoreGive(odo_mutex);
		sendCapteur(x, y, orientation, courbure, marcheAvantTmp, 0);
		vTaskDelay(100);

	}

}


/**
 * Thread d'odométrie
 */
void thread_odometrie(void* p)
{
	int16_t positionGauche[MEMOIRE_VITESSE]; // on introduit un effet de mémoire afin de pouvoir mesurer la vitesse sur un intervalle pas trop petit
	int16_t positionDroite[MEMOIRE_VITESSE];
	uint8_t indiceMemoire = 0;
	x_odo = 0;
	y_odo = 0;
	orientation_odo = 0;
	courbure_odo = 0;
	vg_odo = 0;
	vd_odo = 0;
	uint32_t orientationTick = 0;
	uint32_t orientationMoyTick = 0;;
	uint16_t old_tick_gauche = TICK_CODEUR_GAUCHE, old_tick_droit = TICK_CODEUR_DROIT, tmp;
	int16_t distanceTick, delta_tick_droit, delta_tick_gauche, deltaOrientationTick;
	double k, distance, deltaOrientation;

	for(int i = 0; i < MEMOIRE_VITESSE; i++)
	{
		positionDroite[i] = old_tick_droit;
		positionGauche[i] = old_tick_gauche;
	}

	// On attend l'initialisation de xyo avant de démarrer l'odo, sinon ça casse tout.
	while(!startOdo)
		vTaskDelay(5);
	orientationTick = RAD_TO_TICK(orientation_odo);
	while(1)
	{
		while(xSemaphoreTake(odo_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);

		// La formule d'odométrie est corrigée pour tenir compte des trajectoires
		// (au lieu d'avoir une approximation linéaire, on a une approximation circulaire)
		tmp = TICK_CODEUR_GAUCHE;
		delta_tick_gauche = tmp - old_tick_gauche;
		old_tick_gauche = tmp;
		vg_odo = (tmp - positionGauche[indiceMemoire]) / MEMOIRE_VITESSE;
		positionGauche[indiceMemoire] = tmp;

		tmp = TICK_CODEUR_DROIT;
		delta_tick_droit = tmp - old_tick_droit;
		old_tick_droit = tmp;
		vd_odo = (tmp - positionDroite[indiceMemoire]) / MEMOIRE_VITESSE;
		positionDroite[indiceMemoire] = tmp;

		// Calcul issu de Thalès. Position si le robot tourne vers la droite (pour être cohérent avec l'orientation)
		courbure_odo = 2 / LONGUEUR_CODEUSE_A_CODEUSE_EN_MM * (vg_odo - vd_odo) / (vg_odo + vd_odo);

		indiceMemoire++;
		indiceMemoire %= MEMOIRE_VITESSE;

		// on évite les formules avec "/ 2", qui font perdre de l'information et qui peuvent s'accumuler

		distanceTick = delta_tick_droit + delta_tick_gauche;
		distance = TICK_TO_MM(distanceTick);

		// gestion de la symétrie
		if(!isSymmetry)
			deltaOrientationTick = delta_tick_droit - delta_tick_gauche;
		else
			deltaOrientationTick = delta_tick_gauche - delta_tick_droit;

		// l'erreur à cause du "/2" ne s'accumule pas
		orientationMoyTick = orientationTick + deltaOrientationTick/2;

		if(orientationMoyTick > (uint32_t)TICKS_PAR_TOUR_ROBOT)
		{
			if(orientationMoyTick < (uint32_t)FRONTIERE_MODULO)
				orientationMoyTick -= (uint32_t)TICKS_PAR_TOUR_ROBOT;
			else
				orientationMoyTick += (uint32_t)TICKS_PAR_TOUR_ROBOT;
		}
		orientationTick += deltaOrientationTick;
		deltaOrientation = TICK_TO_RAD(deltaOrientationTick);

//		serial_rb.printfln("TICKS_PAR_TOUR_ROBOT = %d", (int)TICKS_PAR_TOUR_ROBOT);
//		serial_rb.printfln("orientationMoyTick = %d", orientationMoyTick);
//		serial_rb.printfln("orientation = %d", (int)(orientation_odo*1000));

		if(deltaOrientationTick == 0) // afin d'éviter la division par 0
			k = 1.;
		else
			k = sin(deltaOrientation/2)/(deltaOrientation/2);

		orientation_odo = TICK_TO_RAD(orientationMoyTick);
        cos_orientation_odo = cos(orientation_odo);
        sin_orientation_odo = sin(orientation_odo);

		x_odo += k*distance*cos_orientation_odo;
		y_odo += k*distance*sin_orientation_odo;
		xSemaphoreGive(odo_mutex);

//		vTaskDelay(1000);
		vTaskDelay(1000 / FREQUENCE_ODO_ASSER);
	}
}

void TIM3_Init(void)
{
    // Configure TIM4 for PWM
	timer3.Instance = TIM3;
	// Calcul du prescaler qui vient directement d'INTech
	timer3.Init.Prescaler= (uint16_t)((SystemCoreClock / 2) / 256000) - 1; //le deuxième /2 est dû au changement pour un timer de clock doublée
	timer3.Init.CounterMode = TIM_COUNTERMODE_UP;
	timer3.Init.Period = 8000;
	timer3.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
    HAL_TIM_PWM_Init(&timer3);

    // Configure channels 1-4 for TIM4,
    // each channel is mapped to a GPIO pin
    TIM_OC_InitTypeDef oc_config;
    oc_config.OCMode = TIM_OCMODE_PWM1;
    oc_config.Pulse = 6000;
    oc_config.OCPolarity = TIM_OCPOLARITY_LOW;
//    oc_config.OCNIdleState = TIM_OCNIDLESTATE_RESET;
//    oc_config.OCIdleState = TIM_OCIDLESTATE_RESET;
    oc_config.OCFastMode = TIM_OCFAST_DISABLE;

    HAL_TIM_PWM_ConfigChannel(&timer3, &oc_config, TIM_CHANNEL_1);

    // Flip the OC polarity for channels 2 and 4
    oc_config.OCMode = TIM_OCMODE_PWM1;
    HAL_TIM_PWM_ConfigChannel(&timer3, &oc_config, TIM_CHANNEL_2);

    // I want to shift channel 1-4 90 degrees apart...
    HAL_TIM_PWM_Start(&timer3, TIM_CHANNEL_1);
    HAL_TIM_PWM_Start(&timer3, TIM_CHANNEL_2);

}

int main(int argc, char* argv[])
{
	 HAL_Init();
	 SystemClock_Config();

	 HAL_NVIC_SetPriorityGrouping(NVIC_PRIORITYGROUP_4);
	 HAL_NVIC_SetPriority(SysTick_IRQn, 0, 1);
	 HookTemps::setDateDebutMatch();
	 listeHooks.reserve(100);

	 timer.Instance = TIM5;
	 timer.Init.Period = 0xFFFF;
	 timer.Init.CounterMode = TIM_COUNTERMODE_UP;
	 timer.Init.Prescaler = 0;
	 timer.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;

	 HAL_TIM_Encoder_MspInit(&timer);

	 encoder.EncoderMode = TIM_ENCODERMODE_TI12;

	 encoder.IC1Filter = 0x0F;
	 encoder.IC1Polarity = TIM_INPUTCHANNELPOLARITY_RISING;
	 encoder.IC1Prescaler = TIM_ICPSC_DIV4;
	 encoder.IC1Selection = TIM_ICSELECTION_DIRECTTI;

	 encoder.IC2Filter = 0x0F;
	 encoder.IC2Polarity = TIM_INPUTCHANNELPOLARITY_FALLING;
	 encoder.IC2Prescaler = TIM_ICPSC_DIV4;
	 encoder.IC2Selection = TIM_ICSELECTION_DIRECTTI;

/*
	 if (HAL_TIM_Encoder_Init(&timer, &encoder) != HAL_OK)
	 {
		 serial_rb.printfln("Erreur 1");
	 }

	 if(HAL_TIM_Encoder_Start_IT(&timer,TIM_CHANNEL_1)!=HAL_OK)
	 {
		 serial_rb.printfln("Erreur 2");
	 }
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


/*	 if (HAL_TIM_Encoder_Init(&timer2, &encoder2) != HAL_OK)
	 {
		 serial_rb.printfln("Erreur 1");
	 }

	 if(HAL_TIM_Encoder_Start_IT(&timer2,TIM_CHANNEL_1)!=HAL_OK)
	 {
		 serial_rb.printfln("Erreur 2");
	 }
*/
	 HAL_TIM_Encoder_MspInit(0);
//	 TIM3_Init();

	 __GPIOC_CLK_ENABLE();
	 __GPIOD_CLK_ENABLE();

	    GPIO_InitTypeDef GPIO_InitStruct;
	    GPIO_InitStruct.Pin = GPIO_PIN_6 | GPIO_PIN_7;
	    GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
	    GPIO_InitStruct.Speed = GPIO_SPEED_FAST;
	    GPIO_InitStruct.Pull = GPIO_NOPULL;

	    HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);

	    HAL_GPIO_WritePin(GPIOC, GPIO_PIN_6, GPIO_PIN_SET);
	    HAL_GPIO_WritePin(GPIOC, GPIO_PIN_7, GPIO_PIN_SET);

	 // Pins de direction moteur
//	    GPIO_InitTypeDef GPIO_InitStruct;
	    GPIO_InitStruct.Pin = GPIO_PIN_14 | GPIO_PIN_12;
	    GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
	    GPIO_InitStruct.Speed = GPIO_SPEED_FAST;
	    GPIO_InitStruct.Pull = GPIO_NOPULL;
	    HAL_GPIO_Init(GPIOD, &GPIO_InitStruct);

	    HAL_GPIO_WritePin(GPIOD, GPIO_PIN_14, GPIO_PIN_SET);
	    HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12, GPIO_PIN_SET);

//	 TIM3->CCR1 = 150;
//	 TIM3->CCR2 = 200;


	 xTaskCreate(thread_hook, (char*)"TH_HOOK", 2048, 0, 1, 0);
	 xTaskCreate(thread_ecoute_serie, (char*)"TH_LISTEN", 2048, 0, 1, 0);
	 xTaskCreate(thread_odometrie, (char*)"TH_ODO", 2048, 0, 1, 0);
	 xTaskCreate(thread_capteurs, (char*)"TH_CPT", 2048, 0, 1, 0);
	 vTaskStartScheduler();
	 while(1)
	 {
		 serial_rb.printfln("ERREUR!!!");
	//	  vTaskDelay(1000);
	 }

  return 0;
}


void HAL_TIM_Encoder_MspInit(TIM_HandleTypeDef *htim)
{
 GPIO_InitTypeDef GPIO_InitStructA, GPIO_InitStructA2, GPIO_InitStructB;

 __TIM5_CLK_ENABLE();

 __GPIOA_CLK_ENABLE();

 GPIO_InitStructA.Pin = GPIO_PIN_0 | GPIO_PIN_1;
 GPIO_InitStructA.Mode = GPIO_MODE_AF_PP;
 GPIO_InitStructA.Pull = GPIO_PULLUP;
 GPIO_InitStructA.Speed = GPIO_SPEED_HIGH;
 GPIO_InitStructA.Alternate = GPIO_AF2_TIM5;
 HAL_GPIO_Init(GPIOA, &GPIO_InitStructA);

 GPIO_InitStructA2.Pin = GPIO_PIN_15;
 GPIO_InitStructA2.Mode = GPIO_MODE_AF_PP;
 GPIO_InitStructA2.Pull = GPIO_PULLUP;
 GPIO_InitStructA2.Speed = GPIO_SPEED_HIGH;
 GPIO_InitStructA2.Alternate = GPIO_AF1_TIM2;
 HAL_GPIO_Init(GPIOA, &GPIO_InitStructA2);

 HAL_NVIC_SetPriority(TIM5_IRQn, 0, 1);

 __TIM2_CLK_ENABLE();

 __GPIOA_CLK_ENABLE();
 __GPIOB_CLK_ENABLE();

 GPIO_InitStructB.Pin = GPIO_PIN_3;
 GPIO_InitStructB.Mode = GPIO_MODE_AF_PP;
 GPIO_InitStructB.Pull = GPIO_PULLUP;
 GPIO_InitStructB.Speed = GPIO_SPEED_HIGH;
 GPIO_InitStructB.Alternate = GPIO_AF1_TIM2;
 HAL_GPIO_Init(GPIOB, &GPIO_InitStructB);

 HAL_NVIC_SetPriority(TIM2_IRQn, 0, 1);
/*
 __TIM3_CLK_ENABLE();

 __GPIOC_CLK_ENABLE();

 GPIO_InitStructC.Pin = GPIO_PIN_6 | GPIO_PIN_7;
 GPIO_InitStructC.Mode = GPIO_MODE_AF_PP;
 GPIO_InitStructC.Pull = GPIO_NOPULL;
 GPIO_InitStructC.Speed = GPIO_SPEED_HIGH;
 GPIO_InitStructC.Alternate = GPIO_AF2_TIM3;
 HAL_GPIO_Init(GPIOC, &GPIO_InitStructC);

 HAL_NVIC_SetPriority(TIM3_IRQn, 0, 1);
*/
}


/*void TIM5_IRQHandler(void)
{
	HAL_TIM_IRQHandler(&timer);
}*/
#pragma GCC diagnostic pop

// ----------------------------------------------------------------------------
