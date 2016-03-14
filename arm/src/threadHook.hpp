#ifndef TH_HOOK
#define TH_HOOK

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
#include "serie.h"
#include "serialProtocol.h"
#include "asserSimple.hpp"

using namespace std;

/**
 * Thread qui vérifie les hooks
 */
void thread_hook(void* p)
{

	while(!matchDemarre)
		vTaskDelay(10);
	while(1)
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
		vTaskDelay(10);
	}
}

#endif
