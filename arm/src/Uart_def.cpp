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
template<> UART_HandleTypeDef Uart<3>::UART = u;
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
}

void USART2_IRQHandler(void) {
	if (USART_SR_RXNE & USART2->SR)                     // Read Data Reg Not Empty ?
	{
		uint16_t c = USART2->DR & 0xFF;                          // read Data Register
		Uart<2>::store_char(c);
	}
}

void USART3_IRQHandler(void) {
	if (USART_SR_RXNE & USART3->SR)                     // Read Data Reg Not Empty ?
	{
		uint16_t c = USART3->DR & 0xFF;                          // read Data Register
		Uart<3>::store_char(c);
	}
}
#ifdef __cplusplus
}
#endif

#endif  /* UART_DEF_HPP */
