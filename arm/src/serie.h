#ifndef __SERIE_h__
#define __SERIE_h__

#include "global.h"
#include "serialProtocol.h"

extern volatile uint16_t idPaquetEnvoi;
extern SemaphoreHandle_t serial_rb_mutex_TX;
extern unsigned char paquetsEnvoyes[TAILLE_BUFFER_ECRITURE_SERIE][40];
extern uint8_t longueurPaquets[TAILLE_BUFFER_ECRITURE_SERIE];

bool inline verifieChecksum(unsigned char* m, uint8_t longueur)
{
	uint8_t lu, c = 0;
	for(uint8_t i = 0; i <= longueur; i++)
		c += m[i];
	serial_rb.read_char(&lu);
	return (~c & 0xFF) == lu; // c est casté en int avant de faire ~, donc il faut vérifier uniquement l'octet de poids faible
}

void inline resend(uint16_t id)
{
	while(xSemaphoreTake(serial_rb_mutex_TX, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);
	if(id > idPaquetEnvoi - TAILLE_BUFFER_ECRITURE_SERIE)
	{
		unsigned char* m = paquetsEnvoyes[id % TAILLE_BUFFER_ECRITURE_SERIE];
		uint8_t longueur = longueurPaquets[id % TAILLE_BUFFER_ECRITURE_SERIE];
		*(m+2) = idPaquetEnvoi >> 8; // on actualise l'ID et le checksum
		*(m+3) = (uint8_t) idPaquetEnvoi & 0xFF;
		uint8_t c = 0;
		for(int i = 2; i < longueur-1; i++)
			c += m[i];
		*(m+longueur-1) = ~c;
//		serial_rb.write((char*)&longueur);
		serial_rb.write(m, longueur);
		// étant donné que la source peut être la même que la destination, on utilise memmove à la place de memcpy
		memmove(paquetsEnvoyes[idPaquetEnvoi % TAILLE_BUFFER_ECRITURE_SERIE], m, longueur);
		longueurPaquets[idPaquetEnvoi % TAILLE_BUFFER_ECRITURE_SERIE] = longueur;
		idPaquetEnvoi++;
	}
	xSemaphoreGive(serial_rb_mutex_TX);
}

void inline send(unsigned char* m, uint8_t longueur)
{
	while(xSemaphoreTake(serial_rb_mutex_TX, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);
	*m = 0x55;
	*(m+1) = 0xAA;
	*(m+2) = idPaquetEnvoi >> 8;
	*(m+3) = (uint8_t) idPaquetEnvoi;
	uint8_t c = 0;
	for(int i = 2; i < longueur-1; i++)
		c += m[i];
	*(m+longueur-1) = ~c;
	serial_rb.write(m, longueur);
	memcpy(paquetsEnvoyes[idPaquetEnvoi % TAILLE_BUFFER_ECRITURE_SERIE], m, longueur);
	longueurPaquets[idPaquetEnvoi % TAILLE_BUFFER_ECRITURE_SERIE] = longueur;
	idPaquetEnvoi++;
	xSemaphoreGive(serial_rb_mutex_TX);
}

void inline askResend(uint16_t id)
{
	unsigned char out[] = {0, 0, 0, 0, OUT_RESEND_PACKET, (uint8_t) (id >> 8), (uint8_t) (id), 0};
	send(out, 5+3);
}

void inline sendArrive()
{
	unsigned char out[] = {0, 0, 0, 0, OUT_ROBOT_ARRIVE, 0};
	send(out, 5+1);
}

void inline sendPong()
{
	unsigned char out[] = {0, 0, 0, 0, OUT_PONG1, OUT_PONG2, 0};
	send(out, 5+2);
}

void inline sendElementShoot(uint8_t nbElem)
{
	unsigned char out[] = {0, 0, 0, 0, OUT_ELEMENT_SHOOTE, nbElem, 0};
	send(out, 5+2);
}

void inline sendXYO(uint16_t x, uint16_t y, uint16_t orientation, uint8_t courbure, bool marcheAvant)
{
	uint8_t code = OUT_XYO;
	if(!marcheAvant)
		code++;
	unsigned char out[] = {0, 0, 0, 0, code, (uint8_t) ((x+1500) >> 4), (uint8_t)  (((x+1500) << 4) + (y >> 8)), (uint8_t)  y, (uint8_t) (orientation >> 8), (uint8_t) orientation, courbure, 0};
	send(out, 5+7);
}

void inline sendDebug(uint16_t PWMgauche, uint16_t PWMdroit, int16_t vitesseGauche, int16_t vitesseDroite, int16_t distance, int16_t orientation, int16_t vitesseLineaire, int16_t courbure)
{
	unsigned char out[] = {0, 0, 0, 0, OUT_DEBUG_ASSER, PWMgauche >> 8, PWMgauche & 0xFF, PWMdroit >> 8, PWMdroit & 0xFF, vitesseGauche >> 8, vitesseGauche & 0xFF, vitesseDroite >> 8, vitesseDroite & 0xFF, distance >> 8, distance & 0xFF, orientation >> 8, orientation & 0xFF, vitesseLineaire >> 8, vitesseLineaire & 0xFF, courbure >> 8, courbure & 0xFF, 0};
	send(out, 5+17);
}

void inline sendCapteur(uint16_t x, uint16_t y, uint16_t orientation, uint8_t courbure, bool marcheAvant, uint16_t c)
{
	uint8_t code = OUT_CAPTEURS;
	if(!marcheAvant)
		code++;
	unsigned char out[] = {0, 0, 0, 0, code, (uint8_t) ((x+1500) >> 4), (uint8_t) (((x+1500) << 4) + (y >> 8)), (uint8_t) y, (uint8_t) (orientation >> 8), (uint8_t) orientation, courbure, (uint8_t) (c >> 4), (uint8_t) (c << 4), 0};
	send(out, 5+8);
}

#endif
