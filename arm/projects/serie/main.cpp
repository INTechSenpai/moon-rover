/**
 * Example of code using UART library
 */

#include "stm32f4xx.h"
#include "Uart.hpp"

int main(void) {

	Uart<1> serial;
	serial.init(115200);

	char order[100];
	while (1) {
		if (serial.available()) {
			serial.read(order, 200);
			if(!strcmp("!", order)) {
				serial.change_baudrate(9600);
			}
			else if(!strcmp("?", order)) {
				serial.change_baudrate(115200);
			}
			else if(!strcmp("a", order)) {
				serial.disable_rx(); //Ne fait plus que printer "a" après ça!
			}
		}
		serial.println(order);
	}

	return 0;
}


