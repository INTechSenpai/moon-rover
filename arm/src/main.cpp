//
// This file is part of the GNU ARM Eclipse distribution.
// Copyright (c) 2014 Liviu Ionescu.
//

// ----------------------------------------------------------------------------

#include <stdio.h>
#include <stdlib.h>
#include <vector>
#include "diag/Trace.h"

#include "Timer.h"
#include "FreeRTOS.h"
#include "task.h"
#include "_initialize_hardware.c"
#include "Executable.h"
#include "Hook.h"
#include "Uart.hpp"

using namespace std;

// ----- main() ---------------------------------------------------------------

// Sample pragmas to cope with warnings. Please note the related line at
// the end of this function, used to pop the compiler diagnostics status.
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma GCC diagnostic ignored "-Wmissing-declarations"
#pragma GCC diagnostic ignored "-Wreturn-type"

TIM_Encoder_InitTypeDef encoder, encoder2;
TIM_HandleTypeDef timer, timer2;
Uart<2> serial_rb;
vector<Hook*> listeHooks;

bool verifieSousChaine(const char* chaine, int* index, const char* comparaison)
{
	int i = 0;
	while(comparaison[i] != '\0')
	{
		if(chaine[*index] != comparaison[i])
		{
			*index -= i;
			return false;
		}
		(*index)++;
		i++;
	}
	return true;
}

int parseInt(char* chaine, int* index)
{
	int somme = 0;
	while(chaine[*index] >= '0' && chaine[*index] <= '9')
	{
		somme = 10*somme + chaine[*index] - '0';
		(*index)++;
	}
	return somme;
}

/**
 * Thread qui écoute la série
 */
void thread_ecoute_serie(void* p)
{
//		char lecture[] = "Hda 1000 1 tbl 14 truc";
//		char lecture[] = "Hct 8 T 1 tbl 14 truc";
//		char lecture[] = "Hdp 1000 2000 10 0 3 tbl 14 scr 1 act 8";
//		char lecture[] = "Hpo 1000 2000 10 F 1 tbl 14 truc";
//		char lecture[] = "Hda 5000 3 tbl 14 scr 5 act 2";
		char lecture[300];
		while(1)
		{
			if(serial_rb.available())
			{
				serial_rb.read(lecture);
				int index = 0;

				if(lecture[index] == 'H') // parse de hook
				{
					index++;
					Hook* hookActuel;
					uint8_t nbcallbacks;

					if(verifieSousChaine(lecture, &index, "da"))
					{
						serial_rb.printfln("hook de date");
						index++;
						uint32_t date = parseInt(lecture, &index);
						serial_rb.printfln("date : %d",date);
						index++;
						nbcallbacks = parseInt(lecture, &index);
						serial_rb.printfln("nbCallback : %d",nbcallbacks);
						index++;
						hookActuel = new(pvPortMalloc(sizeof(HookTemps))) HookTemps(nbcallbacks, date);
						listeHooks.push_back(hookActuel);
					}
					else if(verifieSousChaine(lecture, &index, "ct"))
					{
						serial_rb.printfln("hook de contact");
						index++;
						uint8_t nbContact = parseInt(lecture, &index);
						serial_rb.printfln("nbContact : %d",nbContact);
						index++;
						bool unique = lecture[index++] == 'T';
						serial_rb.printfln("unique? %d", unique);
						index++;
						nbcallbacks = parseInt(lecture, &index);
						serial_rb.printfln("nbCallback : %d",nbcallbacks);
						index++;
						hookActuel = new(pvPortMalloc(sizeof(HookContact))) HookContact(unique, nbcallbacks, nbContact);
						listeHooks.push_back(hookActuel);
					}
					else if(verifieSousChaine(lecture, &index, "dp"))
					{
						serial_rb.printfln("hook de demi plan");
						index++;
						uint32_t x = parseInt(lecture, &index);
						serial_rb.printfln("x : %d",x);
						index++;
						uint32_t y = parseInt(lecture, &index);
						serial_rb.printfln("y : %d",y);
						index++;
						uint32_t dir_x = parseInt(lecture, &index);
						serial_rb.printfln("dirx : %d",dir_x);
						index++;
						uint32_t dir_y = parseInt(lecture, &index);
						serial_rb.printfln("diry : %d",dir_y);
						index++;
						nbcallbacks = parseInt(lecture, &index);
						serial_rb.printfln("nbCallback : %d",nbcallbacks);
						index++;
						hookActuel = new(pvPortMalloc(sizeof(HookDemiPlan))) HookDemiPlan(nbcallbacks, x, y, dir_x, dir_y);
						listeHooks.push_back(hookActuel);
					}
					else if(verifieSousChaine(lecture, &index, "po"))
					{
						serial_rb.printfln("hook de position");
						index++;
						uint32_t x = parseInt(lecture, &index);
						serial_rb.printfln("x : %d",x);
						index++;
						uint32_t y = parseInt(lecture, &index);
						serial_rb.printfln("y : %d",y);
						index++;
						uint32_t tolerance = parseInt(lecture, &index);
						serial_rb.printfln("tolerance : %d",tolerance);
						index++;
						bool unique = lecture[index++] == 'T';
						serial_rb.printfln("unique? %d", unique);
						index++;
						nbcallbacks = parseInt(lecture, &index);
						serial_rb.printfln("nbCallback : %d",nbcallbacks);
						index++;
						hookActuel = new(pvPortMalloc(sizeof(HookPosition))) HookPosition(unique, nbcallbacks, x, y, tolerance);
						listeHooks.push_back(hookActuel);
					}
					else
					{
						serial_rb.printfln("Erreur de parsing : %s",lecture);
						continue;
					}

					serial_rb.printfln("nbCallback : %d",nbcallbacks);

					for(int i = 0; i < nbcallbacks; i++)
					{
						if(verifieSousChaine(lecture, &index, "tbl"))
						{
							serial_rb.printfln("callback : table");
							int nbElem = parseInt(lecture, &(++index));
							index++;
							serial_rb.printfln("element : %d", nbElem);
							Exec_Update_Table* tmp = new(pvPortMalloc(sizeof(Exec_Update_Table))) Exec_Update_Table(nbElem);
							hookActuel->insert(tmp, i);
						}
						else if(verifieSousChaine(lecture, &index, "scr"))
						{
							serial_rb.printfln("callback : script");
							int nbScript = parseInt(lecture, &(++index));
							index++;
							serial_rb.printfln("script : %d", nbScript);
							Exec_Script* tmp = new(pvPortMalloc(sizeof(Exec_Script))) Exec_Script(nbScript);
							hookActuel->insert(tmp, i);
						}
						else if(verifieSousChaine(lecture, &index, "act"))
						{
							serial_rb.printfln("callback : actionneurs");
							int nbAct = parseInt(lecture, &(++index));
							index++;
							serial_rb.printfln("act : %d", nbAct);
							Exec_Act* tmp = new(pvPortMalloc(sizeof(Exec_Act))) Exec_Act(nbAct);
							hookActuel->insert(tmp, i);
						}
						else
						{
							serial_rb.printfln("Erreur de parsing : %s",lecture);
							break;
						}
					}
				}
//					serial_rb.printfln("color rouge");
			}
//			serial_rb.printfln("%d", TIM5->CNT);

			vTaskDelay(50);
		}
}

/**
 * Thread qui vérifie les hooks
 */
void thread_hook(void* p)
{
	while(1)
	{
		vector<Hook*>::iterator it;
		for(it = listeHooks.begin(); it < listeHooks.end(); it++)
		{
			Hook* hook = *it;
			serial_rb.printfln("Eval hook");
			if((*hook).evalue())
			{
				serial_rb.printfln("Execution!");
				if((*hook).execute()) // suppression demandée
				{
					vPortFree(hook);
					it = listeHooks.erase(it);
					it--;
				}
			}
		}
//		serial_rb.printfln("%d",xTaskGetTickCount());
		vTaskDelay(100);
	}
}

void hello_world_task2(void* p)
{
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


	 if (HAL_TIM_Encoder_Init(&timer, &encoder) != HAL_OK)
	 {
		 serial_rb.printfln("Erreur 1");
	 }

	 if(HAL_TIM_Encoder_Start_IT(&timer,TIM_CHANNEL_1)!=HAL_OK)
	 {
		 serial_rb.printfln("Erreur 2");
	 }

		 timer2.Instance = TIM2;
		 timer2.Init.Period = 0xFFFF;
		 timer2.Init.CounterMode = TIM_COUNTERMODE_UP;
		 timer2.Init.Prescaler = 0;
		 timer2.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;

		 HAL_TIM_Encoder_MspInit(0);

		 encoder2.EncoderMode = TIM_ENCODERMODE_TI12;

		 encoder2.IC1Filter = 0x0F;
		 encoder2.IC1Polarity = TIM_INPUTCHANNELPOLARITY_RISING;
		 encoder2.IC1Prescaler = TIM_ICPSC_DIV4;
		 encoder2.IC1Selection = TIM_ICSELECTION_DIRECTTI;

		 encoder2.IC2Filter = 0x0F;
		 encoder2.IC2Polarity = TIM_INPUTCHANNELPOLARITY_FALLING;
		 encoder2.IC2Prescaler = TIM_ICPSC_DIV4;
		 encoder2.IC2Selection = TIM_ICSELECTION_DIRECTTI;


		 if (HAL_TIM_Encoder_Init(&timer2, &encoder2) != HAL_OK)
		 {
			 serial_rb.printfln("Erreur 1");
		 }

		 if(HAL_TIM_Encoder_Start_IT(&timer2,TIM_CHANNEL_1)!=HAL_OK)
		 {
			 serial_rb.printfln("Erreur 2");
		 }

//	char lecture[100];
	while(1)
	{
/*		if(serial_rb.available())
		{
			serial_rb.read(lecture);
			if(strcmp(lecture,"color?") == 0)
				serial_rb.printfln("color rouge");
		}*/
		serial_rb.printfln("%d %d", TIM2->CNT, TIM5->CNT);

		vTaskDelay(200);
	}
}

int main(int argc, char* argv[])
{
	 HAL_Init();
	 SystemClock_Config();

	 HAL_NVIC_SetPriorityGrouping(NVIC_PRIORITYGROUP_4);
	 HAL_NVIC_SetPriority(SysTick_IRQn, 0, 1);

	 Executable::setSerie(serial_rb);
	 HookTemps::setDateDebutMatch();
	 listeHooks.reserve(100);
	 serial_rb.init(115200);
	 xTaskCreate(thread_hook, (char*)"TH_HOOK", 2048, 0, 1, 0);
	 xTaskCreate(thread_ecoute_serie, (char*)"TH_LISTEN", 2048, 0, 1, 0);
	 xTaskCreate(hello_world_task2, (char*)"TEST2", 2048, 0, 1, 0);
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

}


/*void TIM5_IRQHandler(void)
{
	HAL_TIM_IRQHandler(&timer);
}*/
#pragma GCC diagnostic pop

// ----------------------------------------------------------------------------
