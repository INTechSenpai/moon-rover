/**
 * pidvitesse.hpp
 *
 * Classe PIDvitesse : implémente un régulateur PID (proportionnel intégral dérivé) adapté à la vitesse
 *
 * Auteur : Paul BERNIER - bernier.pja@gmail.com
 */

#ifndef PID_VITESSE_HPP
#define PID_VITESSE_HPP

#include <stdint.h>
#include "utils.h"

/**
 *
 * on ne peut pas juste faire deux asser en vitesse, un pour chaque roue
 * déjà parce que les contraintes mécaniques affectent les deux de manière interdépendante
 * ensuite parce qu'il faut faire attention à la courbure. Par exemple, si un moteur est moins péchu que l'autre, il ne faut pas que la trajectoire fasse n'importe quoi
 * dans ce cas, le moteur le plus péchu devra peut-être attendre l'autre afin de conserver une courbure convenable
 *
 */

class PIDvitesse
{
public:

	PIDvitesse(volatile int32_t* vitesseLineaireReelle, volatile int32_t* courbureReelle, volatile int32_t* commandePWMGauche, volatile int32_t* commandePWMDroite, volatile int32_t* consigneVitesseLineaire, volatile int32_t* consigneCourbure)
	{
		this->vitesseLineaireReelle = vitesseLineaireReelle;
		this->courbureReelle = courbureReelle;
		this->commandePWMGauche = commandePWMGauche;
		this->commandePWMDroite = commandePWMDroite;
		this->consigneVitesseLineaire = consigneVitesseLineaire;
		this->consigneCourbure = consigneCourbure;

		setTuningsC(0, 0, 0);
		setTuningsV(0, 0, 0);
		epsilon = 0;
		PWMmax = 100;

		pre_errorC = 0;
		derivativeC = 0;
		integralC = 0;
		pre_errorV = 0;
		derivativeV = 0;
		integralV = 0;
	}

	void compute() {
	// On suppose que consigneVitesseLineaire et consigneCourbure ont déjà été limitées
		int32_t errorV = (*consigneVitesseLineaire) - (*vitesseLineaireReelle);
		derivativeV = errorV - pre_errorV;
		integralV += errorV;
		pre_errorV = errorV;

		// Commande pour ajuster la vitesse linéaire
		int32_t resultV = (int32_t)(kpV * errorV + kiV * integralV + kdV * derivativeV);

// TODO limitations en accélération linéaire

		int32_t consigneRotation = (*consigneVitesseLineaire) * demiDistance * (*consigneCourbure);
// TODO limitation pour la vitesse de rotation

		*consigneCourbure = consigneRotation / ((*consigneVitesseLineaire) * demiDistance);

		int32_t errorC = (*consigneCourbure) - (*courbureReelle);
		derivativeC = errorC - pre_errorC;
		integralC += errorC;
		pre_errorC = errorC;

		// Commande pour ajuster la courbure
		int32_t resultC = (int32_t)(kpC * errorC + kiC * integralC + kdC * derivativeC);

// TODO limitations de la dérivée de la courbure

		// Commande pour ajuster la vitesse de rotation
		int32_t resultR = resultV * demiDistance * resultC;

// TODO limitations en accélération de rotation


		// On en déduit la commande pour chaque moteur
		int32_t resultG = resultV - resultR;
		int32_t resultD = resultV + resultR;

// TODO limitations de l'accélération de chaque roue

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

	void setTuningsV(float kp, float ki, float kd) {
		if (kp < 0 || ki < 0 || kd < 0)
			return;

		this->kpV = kp;
		this->kiV = ki;
		this->kdV = kd;
	}

	void setTuningsC(float kp, float ki, float kd) {
		if (kp < 0 || ki < 0 || kd < 0)
			return;

		this->kpC = kp;
		this->kiC = ki;
		this->kdC = kd;
	}

	void setEpsilon(int32_t seuil) {
		if(seuil < 0)
			return;
		epsilon = seuil;
	}

	void setDemiDistance(int32_t demiDistance) {
		if(demiDistance < 0)
			return;
		this->demiDistance = demiDistance;
	}

	void resetErrors() {
		pre_errorC = 0;
		derivativeC = 0;
		integralC = 0;
		pre_errorV = 0;
		derivativeV = 0;
		integralV = 0;
	}
private:

	float kpV;
	float kiV;
	float kdV;

	float kpC;
	float kiC;
	float kdC;

	volatile int32_t* vitesseLineaireReelle;
	volatile int32_t* courbureReelle;
	volatile int32_t* commandePWMGauche;
	volatile int32_t* commandePWMDroite;
	volatile int32_t* consigneVitesseLineaire;
	volatile int32_t* consigneCourbure;

	int32_t PWMmax;
	int32_t epsilon;

	int32_t pre_errorC;
	int32_t derivativeC;
	int32_t integralC;

	int32_t pre_errorV;
	int32_t derivativeV;
	int32_t integralV;

	// La demi-distance entre les deux roues de propulsion
	int32_t demiDistance;
};

#endif
