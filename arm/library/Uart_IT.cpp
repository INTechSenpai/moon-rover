#include "Uart.hpp"

/**
 * Initialisation of USART number
 *
 */

template<> USART_TypeDef* Uart<1>::USARTx = USART1;
template<> USART_TypeDef* Uart<2>::USARTx = USART2;
template<> USART_TypeDef* Uart<3>::USARTx = USART3;
template<> USART_TypeDef* Uart<4>::USARTx = UART4;
template<> USART_TypeDef* Uart<5>::USARTx = UART5;
template<> USART_TypeDef* Uart<6>::USARTx = USART6;

/**
 * Interrupt service routines definitions
 *
 */

#ifdef __cplusplus
extern "C" {
#endif
void USART1_IRQHandler(void) {
	if (USART_GetITStatus(USART1, USART_IT_RXNE)) {
		unsigned char c = USART_ReceiveData(USART1);
		Uart<1>::store_char(c);
	}
}
void USART2_IRQHandler(void) {
	if (USART_GetITStatus(USART2, USART_IT_RXNE)) {
		unsigned char c = USART_ReceiveData(USART2);
		Uart<2>::store_char(c);
	}
}
void USART3_IRQHandler(void) {
	if (USART_GetITStatus(USART3, USART_IT_RXNE)) {
		unsigned char c = USART_ReceiveData(USART3);
		Uart<3>::store_char(c);
	}
}
void UART4_IRQHandler(void) {
	if (USART_GetITStatus(UART4, USART_IT_RXNE)) {
		unsigned char c = USART_ReceiveData(UART4);
		Uart<4>::store_char(c);
	}
}
void UART5_IRQHandler(void) {
	if (USART_GetITStatus(UART5, USART_IT_RXNE)) {
		unsigned char c = USART_ReceiveData(UART5);
		Uart<5>::store_char(c);
	}
}
void USART6_IRQHandler(void) {
	if (USART_GetITStatus(USART6, USART_IT_RXNE)) {
		unsigned char c = USART_ReceiveData(USART6);
		Uart<6>::store_char(c);
	}
}
#ifdef __cplusplus
}
#endif
