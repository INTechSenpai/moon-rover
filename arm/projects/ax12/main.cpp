#include "ManagerMgr.h"
#include "ActuatorsMgr.hpp"
#include "Uart.hpp"

int main(void)
{
	Uart<1> serial_pc;
	Uart<2> serial_ax;
	serial_pc.init(115200);
	serial_ax.init(9600);

	ManagerMgr managerMgr = ManagerMgr();
	ActuatorsMgr* actuatorsMgr = managerMgr.getActuatorsMgr();
	char order[100];
	while(true)
	{
		if (serial_pc.available())
		{
			serial_pc.read(order, 200);
			if(!strcmp("?", order))
			{
				serial_pc.printfln("actionneurs");
			}
			else if(!strcmp("!", order))
			{
				actuatorsMgr->monterBras();
			}
		}
	}
}
