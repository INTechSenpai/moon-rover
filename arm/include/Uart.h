#include "stm32f4xx_hal.h"
#include "stm32f4xx_hal_uart.h"
#include <string.h>
#include <stdlib.h>

class Uart
{
    public:

	void init();
	void send(char string[]);
	char* intToString(int value, char* result, int base);

    private:
	UART_HandleTypeDef huart1;

};
