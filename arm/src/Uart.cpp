#include "Uart.h"

#define BAUDRATE              9600
#define TXPIN                 GPIO_PIN_6
#define RXPIN                 GPIO_PIN_7
#define DATAPORT              GPIOB
#define UART_PRIORITY         6
#define UART_RX_SUBPRIORITY   0
#define MAXCLISTRING          100 // Biggest string the user will type

uint8_t rxBuffer = '\000'; // where we store that one character that just came in
uint8_t rxString[MAXCLISTRING]; // where we build our string from characters coming in
int rxindex = 0; // index for going though rxString

void Uart::init()
{
	// initialisation des GPIO utilisés par l'UART
	__GPIOB_CLK_ENABLE();
	__USART1_CLK_ENABLE();
	__DMA2_CLK_ENABLE();

	GPIO_InitTypeDef GPIO_InitStruct;

	GPIO_InitStruct.Pin = TXPIN | RXPIN;
	GPIO_InitStruct.Mode = GPIO_MODE_AF_PP;
	GPIO_InitStruct.Pull = GPIO_NOPULL;
	GPIO_InitStruct.Speed = GPIO_SPEED_LOW;
	GPIO_InitStruct.Alternate = GPIO_AF7_USART1;
	HAL_GPIO_Init(DATAPORT, &GPIO_InitStruct);

	// initialisation de l'UART

	huart1.Instance = USART1;
	huart1.Init.BaudRate = BAUDRATE;
	huart1.Init.WordLength = UART_WORDLENGTH_8B;
	huart1.Init.StopBits = UART_STOPBITS_1;
	huart1.Init.Parity = UART_PARITY_NONE;
	huart1.Init.Mode = UART_MODE_TX_RX;
	huart1.Init.HwFlowCtl = UART_HWCONTROL_NONE;
	huart1.Init.OverSampling = UART_OVERSAMPLING_16;
	HAL_UART_Init(&huart1);

	// Prépare la réception des données
	__HAL_UART_FLUSH_DRREGISTER(&huart1);
	HAL_UART_Receive_DMA(&huart1, &rxBuffer, 1);

	// initialisation du DMA, qui s'occupe de la mémoire pour l'UART sans avoir besoin de temps processeur
/*	DMA_HandleTypeDef hdma_usart1_rx;
	hdma_usart1_rx.Instance = DMA2_Stream2;
	hdma_usart1_rx.Init.Channel = DMA_CHANNEL_4;
	hdma_usart1_rx.Init.Direction = DMA_PERIPH_TO_MEMORY;
	hdma_usart1_rx.Init.PeriphInc = DMA_PINC_DISABLE;
	hdma_usart1_rx.Init.MemInc = DMA_MINC_DISABLE;
	hdma_usart1_rx.Init.PeriphDataAlignment = DMA_PDATAALIGN_BYTE;
	hdma_usart1_rx.Init.MemDataAlignment = DMA_MDATAALIGN_BYTE;
	hdma_usart1_rx.Init.Mode = DMA_CIRCULAR;
	hdma_usart1_rx.Init.Priority = DMA_PRIORITY_LOW;
	hdma_usart1_rx.Init.FIFOMode = DMA_FIFOMODE_DISABLE;
	HAL_DMA_Init(&hdma_usart1_rx);

	__HAL_LINKDMA(huart1, hdmarx, hdma_usart1_rx);

	HAL_NVIC_SetPriority(DMA2_Stream2_IRQn, UART_PRIORITY, UART_RX_SUBPRIORITY);
	HAL_NVIC_EnableIRQ(DMA2_Stream2_IRQn);*/
}

/* Prints the supplied string to uart */
void Uart::send(char string[])
{
	HAL_UART_Transmit(&huart1, (uint8_t*)string, strlen(string), 5);
}
/*
static inline void write(float value, int places) {
		int digit;
		float tens = 0.1;
		int tenscount = 0;
		int i;
		float tempfloat = value;

		// make sure we round properly. this could use pow from <math.h>, but doesn't seem worth the import
		// if this rounding step isn't here, the value  54.321 prints as 54.3209
		// calculate rounding term d:   0.5/pow(10,places)
		float d = 0.5;
		if (value < 0)
			d *= -1.0;
		// divide by ten for each decimal place
		for (i = 0; i < places; i++)
			d /= 10.0;
		// this small addition, combined with truncation will round our values properly
		tempfloat += d;

		// first get value tens to be the large power of ten less than value
		// tenscount isn't necessary but it would be useful if you wanted to know after this how many chars the number will take
		if (value < 0)
			tempfloat *= -1.0;
		while ((tens * 10.0) <= tempfloat) {
			tens *= 10.0;
			tenscount += 1;
		}

		// write out the negative if needed
		if (value < 0)
			write("-");

		if (tenscount == 0)
			write(0);

		for (i = 0; i < tenscount; i++) {
			digit = (int) (tempfloat / tens);
			write(digit);
			tempfloat = tempfloat - ((float) digit * tens);
			tens /= 10.0;
		}

		// if no places after decimal, stop now and return
		if (places <= 0)
			return;

		// otherwise, write the point and continue on
		write(".");

		// now write out each decimal place by shifting digits one by one into the ones place and writing the truncated value
		for (i = 0; i < places; i++) {
			tempfloat *= 10.0;
			digit = (int) tempfloat;
			write(digit);
			// once written, subtract off that digit
			tempfloat = tempfloat - (float) digit;
		}
	}
*/
/* UART TX complete callback */
void HAL_UART_TxCpltCallback(UART_HandleTypeDef *huart)
{

}

/* UART RX complete callback */
void HAL_UART_RxCpltCallback(UART_HandleTypeDef *huart)
{
//	__HAL_UART_FLUSH_DRREGISTER(&huart1); // Clear the buffer to prevent overrun

//	int i = 0;

//	send(&rxBuffer); // Echo the inputed character
}

/* Converts int to string */
char* Uart::intToString(int value, char* result, int base)
{
	// check that the base if valid
	if (base < 2 || base > 36) { *result = '\0'; return result; }

	char* ptr = result, *ptr1 = result, tmp_char;
	int tmp_value;

	do {
		tmp_value = value;
		value /= base;
		*ptr++ = "zyxwvutsrqponmlkjihgfedcba9876543210123456789abcdefghijklmnopqrstuvwxyz"[35 + (tmp_value - value * base)];
	} while (value);

	// Apply negative sign
	if (tmp_value < 0) *ptr++ = '-';
	*ptr-- = '\0';
	while (ptr1 < ptr) {
		tmp_char = *ptr;
		*ptr-- = *ptr1;
		*ptr1++ = tmp_char;
	}
	return result;
}
