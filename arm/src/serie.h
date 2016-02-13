#include "global.h"

/**
 * Fonction "haut niveau" de la série.
 */

void resend(uint16_t id);

void send(unsigned char* m, uint8_t longueur);

void askResend(uint16_t id);

void sendArrive();

void sendDebutMatch();

void sendElementShoot(uint8_t nbElem);

void sendCapteur(uint16_t x, uint16_t y, uint16_t orientation, uint8_t courbure, bool marcheAvant, uint16_t c);
