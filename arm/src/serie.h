#ifndef SERIE
#define SERIE

#include "global.h"
#include "serialProtocol.h"

extern uint16_t volatile idPaquetEnvoi;
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

void inline sendCouleur(bool symetrie)
{
	unsigned char out[] = {0, 0, 0, 0, 0, 0};
	if(symetrie)
		out[4] = OUT_COULEUR_ROBOT_AVEC_SYMETRIE;
	else
		out[4] = OUT_COULEUR_ROBOT_SANS_SYMETRIE;
	send(out, 5+1);
}

void inline sendCoquillage(uint8_t code)
{
	unsigned char out[] = {0, 0, 0, 0, OUT_CODE_COQUILLAGES, code, 0};
	send(out, 5+2);
}

void inline sendBalise(bool present)
{
	unsigned char out[] = {0, 0, 0, 0, 0, 0};
	if(present)
		out[4] = OUT_BALISE_PRESENTE;
	else
		out[4] = OUT_BALISE_NON_PRESENTE;
	send(out, 5+1);
}

void inline sendDebutMatch()
{
	unsigned char out[] = {0, 0, 0, 0, OUT_DEBUT_MATCH, 0};
	send(out, 5+1);
}

void inline sendArrive()
{
	unsigned char out[] = {0, 0, 0, 0, OUT_ROBOT_ARRIVE, 0};
	send(out, 5+1);
}

void inline sendProblemeMeca()
{
	unsigned char out[] = {0, 0, 0, 0, OUT_PROBLEME_MECA, 0};
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
	unsigned char out[] = {0, 0, 0, 0, OUT_DEBUG_ASSER, (unsigned char) (PWMgauche >> 8),  (unsigned char) (PWMgauche & 0xFF),  (unsigned char) (PWMdroit >> 8),  (unsigned char) (PWMdroit & 0xFF),  (unsigned char) (vitesseGauche >> 8),  (unsigned char) (vitesseGauche & 0xFF),  (unsigned char) (vitesseDroite >> 8),  (unsigned char) (vitesseDroite & 0xFF),  (unsigned char) (distance >> 8),  (unsigned char) (distance & 0xFF),  (unsigned char) (orientation >> 8),  (unsigned char) (orientation & 0xFF),  (unsigned char) (vitesseLineaire >> 8),  (unsigned char) (vitesseLineaire & 0xFF),  (unsigned char) (courbure >> 8),  (unsigned char) (courbure & 0xFF), 0};
	send(out, 5+17);
}

void inline sendCapteur(uint16_t x, uint16_t y, uint16_t orientation, uint8_t courbure, bool marcheAvant, uint16_t c)
{
	uint8_t code = OUT_CAPTEURS;
	if(!marcheAvant)
		code++;
	unsigned char out[] = {0, 0, 0, 0, code, (uint8_t) ((x+1500) >> 4), (uint8_t) (((x+1500) << 4) + (y >> 8)), (uint8_t) y, (uint8_t) (orientation >> 8), (uint8_t) orientation, courbure, (uint8_t) (c >> 4), (uint8_t) (c << 4), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	send(out, 5+28);
}

#endif
