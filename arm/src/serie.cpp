#include "serie.h"

uint16_t idPaquetEnvoi = 0;
SemaphoreHandle_t serial_rb_mutex_TX = xSemaphoreCreateMutex();
unsigned char paquetsEnvoyes[TAILLE_BUFFER_ECRITURE_SERIE][20];
uint8_t longueurPaquets[TAILLE_BUFFER_ECRITURE_SERIE];
