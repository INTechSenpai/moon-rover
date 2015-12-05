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

TIM_Encoder_InitTypeDef encoder;
TIM_HandleTypeDef timer;
Uart<2> serial_rb;
vector<Hook*> listeHooks;

bool verifieSousChaine(char* chaine, int* index, char* comparaison)
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
//		char lecture[] = "hda 1000 1 tbl 14 T truc";
		char lecture[] = "hct 8 1 tbl 14 T truc";
		while(1)
		{
//			if(serial_rb.available())
			{
//				serial_rb.read(lecture);
				int index = 0;
				Hook* hookActuel;
				if(verifieSousChaine(lecture, &index, "hda"))
				{
					serial_rb.printfln("hook de date");
					index++;
					uint32_t date = parseInt(lecture, &index);
					index++;
					serial_rb.printfln("date : %d",date);
					uint8_t nbcallbacks = parseInt(lecture, &index);
					serial_rb.printfln("nbCallback : %d",nbcallbacks);
					index++;
					HookTemps hook = HookTemps(nbcallbacks, date);
					hookActuel = &hook;
					listeHooks.push_back(&hook);
				}
				else if(verifieSousChaine(lecture, &index, "hct"))
				{
					serial_rb.printfln("hook de contact");
					index++;
					uint8_t nbContact = parseInt(lecture, &index);
					index++;
					serial_rb.printfln("nbContact : %d",nbContact);
//					HookTemps hook = HookTemps(nbcallbacks, date);
//					hookActuel = &hook;
//					listeHooks.push_back(&hook);
				}

				if(verifieSousChaine(lecture, &index, "tbl"))
				{
					serial_rb.printfln("callback : table");
					int nbElem = parseInt(lecture, &(++index));
					serial_rb.printfln("element : %d", nbElem);
					bool unique = lecture[++index] == 'T';
					serial_rb.printfln("unique? %d", unique);
					index++;
					serial_rb.printfln("");
				}
//					serial_rb.printfln("color rouge");
			}
//			serial_rb.printfln("%d", TIM5->CNT);

			vTaskDelay(1000);
		}
}

/**
 * Thread qui vérifie les hooks
 */
void thread_hook(void* p)
{
	Exec_Update_Table::setSerie(serial_rb);
	HookTemps::setDateDebutMatch();
	Exec_Update_Table exec;

	HookTemps testHook(2, 3000);
	testHook.insert(&exec, 0);
	testHook.insert(&exec, 1);
	while(1)
	{
		if(testHook.evalue())
			testHook.execute();
//		serial_rb.printfln("truc");
		vTaskDelay(1000);
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

//	char lecture[100];
	while(1)
	{
/*		if(serial_rb.available())
		{
			serial_rb.read(lecture);
			if(strcmp(lecture,"color?") == 0)
				serial_rb.printfln("color rouge");
		}*/
		serial_rb.printfln("%d", TIM5->CNT);

		vTaskDelay(100);
	}
}

int main(int argc, char* argv[])
{
	 HAL_Init();
	 SystemClock_Config();

	 HAL_NVIC_SetPriorityGrouping(NVIC_PRIORITYGROUP_4);
	 HAL_NVIC_SetPriority(SysTick_IRQn, 0, 1);


  serial_rb.init(115200);
  xTaskCreate(thread_hook, (char*)"TH_HOOK", 2048, 0, 1, 0);
  xTaskCreate(thread_ecoute_serie, (char*)"TH_LISTEN", 2048, 0, 1, 0);
//  xTaskCreate(hello_world_task2, (char*)"TEST2", 2048, 0, 1, 0);
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
 GPIO_InitTypeDef GPIO_InitStruct;

 if (htim->Instance == TIM5)
 {

 __TIM5_CLK_ENABLE();

 __GPIOA_CLK_ENABLE();

 GPIO_InitStruct.Pin = GPIO_PIN_0 | GPIO_PIN_1;
 GPIO_InitStruct.Mode = GPIO_MODE_AF_PP;
 GPIO_InitStruct.Pull = GPIO_PULLUP;
 GPIO_InitStruct.Speed = GPIO_SPEED_HIGH;
 GPIO_InitStruct.Alternate = GPIO_AF2_TIM5;
 HAL_GPIO_Init(GPIOA, &GPIO_InitStruct);

 HAL_NVIC_SetPriority(TIM5_IRQn, 0, 1);

// HAL_NVIC_EnableIRQ(TIM5_IRQn);
 }
}

/*void TIM5_IRQHandler(void)
{
	HAL_TIM_IRQHandler(&timer);
}*/
#pragma GCC diagnostic pop

// ----------------------------------------------------------------------------
