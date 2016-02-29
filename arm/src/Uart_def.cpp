#ifndef UART_DEF_HPP
#define UART_DEF_HPP

#include <stm32f4xx_hal.h>
#include <stm32f4xx_hal_uart.h>

#include "Uart.hpp"
/**
 * Initialisation of USART number
 *
 */
UART_HandleTypeDef u;
template<> UART_HandleTypeDef Uart<1>::UART = u;
template<> UART_HandleTypeDef Uart<2>::UART = u;
template<> UART_HandleTypeDef Uart<6>::UART = u;

/**
 * Interrupt service routines definitions
 *
 */

#ifdef __cplusplus
extern "C" {
#endif
void USART1_IRQHandler(void) {
	if (USART_SR_RXNE & USART1->SR)                     // Read Data Reg Not Empty ?
	{
		uint16_t c = USART1->DR & 0xFF;                          // read Data Register
		Uart<1>::store_char(c);
	}
/*	//HAL_UART_IRQHandler(&Uart<1>::UART);
	uint8_t c[1];
	HAL_UART_Receive(&Uart<1>::UART, c, 1, 1);
	Uart<1>::store_char(c[0]);*/
}

void USART2_IRQHandler(void) {
		uint16_t c = USART2->DR & 0xFF;                          // read Data Register
		Uart<2>::store_char(c);
	//	HAL_UART_IRQHandler(&Uart<2>::UART);
//	uint8_t c[1];
//	HAL_UART_Receive(&Uart<2>::UART, c, 1, 1);
//	Uart<2>::store_char(c[0]);
}

void USART6_IRQHandler(void) {
	HAL_UART_IRQHandler(&Uart<6>::UART);
	//uint8_t c[1];
	//HAL_UART_Receive(&Uart<6>::UART, c, 1, 1);
	//Uart<6>::store_char(c[0]);
}
#ifdef __cplusplus
}
#endif

#endif  /* UART_DEF_HPP */
