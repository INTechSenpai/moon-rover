/**
 * pidvitesse.hpp
 *
 * Classe PIDvitesse : implémente un régulateur PID (proportionnel intégral dérivé) adapté à la vitesse
 *
 * Auteur : Paul BERNIER - bernier.pja@gmail.com
 */

#ifndef PID_HPP
#define PID_HPP

#include <stdint.h>
#include "utils.h"


class PIDvitesse
{
public:


	PIDvitesse(volatile int32_t* vitesseGaucheReelle, volatile int32_t* vitesseDroiteReelle, volatile int32_t* commandePWMGauche, volatile int32_t* commandePWMDroite, volatile int32_t* consigneVitesseGauche, volatile int32_t* consigneVitesseDroite)
	{
		this->vitesseGaucheReelle = vitesseGaucheReelle;
		this->vitesseDroiteReelle = vitesseDroiteReelle;
		this->commandePWMGauche = commandePWMGauche;
		this->commandePWMDroite = commandePWMDroite;
		this->consigneVitesseGauche = consigneVitesseGauche;
		this->consigneVitesseDroite = consigneVitesseDroite;

		setTunings(0, 0, 0);
		epsilon = 0;
		PMWmax = 1000;
		pre_errorG = 0;
		derivativeG = 0;
		integralG = 0;

		pre_errorD = 0;
		derivativeD = 0;
		integralD = 0;
	}

	void compute() {
// TODO modification de la consigne afin de respecter les contraintes mécaniques
// notamment : accélération limitée, vitesse limitée, somme des vitesses limitées, …

		int32_t errorG = (*consigneVitesseGauche) - (*vitesseGaucheReelle);
		derivativeG = errorG - pre_errorG;
		integralG += errorG;
		pre_errorG = errorG;

		int32_t resultG = (int32_t)(
				kp * errorG + ki * integralG + kd * derivativeG);

		int32_t errorD = (*consigneVitesseDroite) - (*vitesseDroiteReelle);
		derivativeD = errorD - pre_errorD;
		integralD += errorD;
		pre_errorD = errorD;

		int32_t resultD = (int32_t)(
				kp * errorD + ki * integralD + kd * derivativeD);

		// saturation
		if(resultG > PWMmax)
			resultG = PWMmax;
		else if(resultG < -PWMmax)
			resultG = -PWMmax;

		if(resultD > PWMmax)
			resultD = PWMmax;
		else if(resultD < -PWMmax)
			resultD = -PWMmax;

		//Seuillage de la commande
		if (ABS(resultD) < epsilon)
			resultD = 0;
		if (ABS(resultG) < epsilon)
			resultG = 0;

		(*commandePWMGauche) = resultG;
		(*commandePWMDroite) = resultD;
	}

	void setTunings(float kp, float ki, float kd) {
		if (kp < 0 || ki < 0 || kd < 0)
			return;

		this->kp = kp;
		this->ki = ki;
		this->kd = kd;
	}

	void setEpsilon(int32_t seuil) {
		if(seuil < 0)
			return;
		epsilon = seuil;
	}

	void resetErrors() {
		pre_errorG = 0;
		integralG = 0;
		pre_errorD = 0;
		integralD = 0;
	}
private:

	float kp;
	float ki;
	float kd;

	volatile int32_t* vitesseGaucheReelle;
	volatile int32_t* vitesseDroiteReelle;
	volatile int32_t* commandePWMGauche;
	volatile int32_t* commandePWMDroite;
	volatile int32_t* consigneVitesseGauche;
	volatile int32_t* consigneVitesseDroite

	int32_t epsilon;

	int32_t pre_errorG;
	int32_t derivativeG;
	int32_t integralG;

	int32_t pre_errorD;
	int32_t derivativeD;
	int32_t integralD;
};

#endif
