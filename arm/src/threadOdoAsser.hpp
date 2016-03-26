#ifndef TH_ODO_ASSER
#define TH_ODO_ASSER

#include <stdio.h>
#include <stdlib.h>
#include <vector>
#include "diag/Trace.h"

#include "math.h"
#include "Timer.h"
#include "FreeRTOS.h"
#include "task.h"
#include "Hook.h"
#include "Uart.hpp"
#include "global.h"
#include "ax12.hpp"
#include "serialProtocol.h"
#include "asserSimple.hpp"
#include "serie.h"

using namespace std;


/**
 * Thread d'odométrie et d'asservissement
 */
void thread_odometrie_asser(void*)
{
	uint8_t debugCompteur = 0;

	int16_t positionGauche[MEMOIRE_MESURE]; // on introduit un effet de mémoire afin de pouvoir mesurer la vitesse sur un intervalle pas trop petit
	int16_t positionDroite[MEMOIRE_MESURE];
//	int16_t vitesseGauche[MEMOIRE_MESURE]; // on introduit un effet de mémoire afin de pouvoir mesurer l'accélération sur un intervalle pas trop petit
//	int16_t vitesseDroite[MEMOIRE_MESURE];
	uint8_t indiceMemoire = 0;
	x_odo = 0;
	y_odo = 0;
	orientation_odo = 0;
	courbure_odo = 0;
	currentRightSpeed = 0;
	currentLeftSpeed = 0;
	currentAngle = 0;
	uint32_t orientationMoyTick = 0;;
	uint16_t old_tick_gauche = TICK_CODEUR_GAUCHE, old_tick_droit = TICK_CODEUR_DROIT, tmp;
	int16_t distanceTick, delta_tick_droit, delta_tick_gauche, deltaOrientationTick;
	double k, distance, deltaOrientation;

	for(int i = 0; i < MEMOIRE_MESURE; i++)
	{
		positionDroite[i] = old_tick_droit;
		positionGauche[i] = old_tick_gauche;
//		vitesseDroite[i] = 0;
//		vitesseGauche[i] = 0;

	}

	// On attend l'initialisation de xyo avant de démarrer l'odo, sinon ça casse tout.
	while(!startOdo)
		vTaskDelay(5);
	currentAngle = RAD_TO_TICK(orientation_odo);

	TickType_t xLastWakeTime;
	const TickType_t periode = 1000 / FREQUENCE_ODO_ASSER;
	xLastWakeTime = xTaskGetTickCount();

	while(1)
	{
		vTaskDelayUntil(&xLastWakeTime, periode);

		// ODOMÉTRIE
		while(xSemaphoreTake(odo_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);

		// La formule d'odométrie est corrigée pour tenir compte des trajectoires
		// (au lieu d'avoir une approximation linéaire, on a une approximation circulaire)
		tmp = TICK_CODEUR_GAUCHE;
		delta_tick_gauche = tmp - old_tick_gauche;
		old_tick_gauche = tmp;
		currentLeftSpeed = (tmp - positionGauche[indiceMemoire]) / MEMOIRE_MESURE;
//		currentLeftAcceleration = (currentLeftSpeed - vitesseGauche[indiceMemoire]) / MEMOIRE_MESURE;
		positionGauche[indiceMemoire] = tmp;
//		vitesseGauche[indiceMemoire] = currentLeftSpeed;

		tmp = TICK_CODEUR_DROIT;
		delta_tick_droit = tmp - old_tick_droit;
		old_tick_droit = tmp;
		currentRightSpeed = (tmp - positionDroite[indiceMemoire]) / MEMOIRE_MESURE;
//		currentRightAcceleration = (currentRightSpeed - vitesseDroite[indiceMemoire]) / MEMOIRE_MESURE;
		positionDroite[indiceMemoire] = tmp;
//		vitesseDroite[indiceMemoire] = currentLeftSpeed;

		vitesseLineaireReelle = (currentRightSpeed + currentLeftSpeed) / 2;

		if(!isSymmetry)
			vitesseRotationReelle = (currentRightSpeed - currentLeftSpeed) / 2;
		else
			vitesseRotationReelle = (currentLeftSpeed - currentRightSpeed) / 2;

		// Calcul issu de Thalès. Positif si le robot tourne vers la droite (pour être cohérent avec l'orientation)
		courbure_odo = 2 / LONGUEUR_CODEUSE_A_CODEUSE_EN_MM * (currentLeftSpeed - currentRightSpeed) / (currentLeftSpeed + currentRightSpeed);

		indiceMemoire++;
		indiceMemoire %= MEMOIRE_MESURE;

		// on évite les formules avec "/ 2", qui font perdre de l'information et qui peuvent s'accumuler

		distanceTick = delta_tick_droit + delta_tick_gauche;
		distance = TICK_TO_MM(distanceTick);

		// gestion de la symétrie : en effet, toutes les variables sont symétrisées, y compris l'orientation
		if(!isSymmetry)
			deltaOrientationTick = delta_tick_droit - delta_tick_gauche;
		else
			deltaOrientationTick = delta_tick_gauche - delta_tick_droit;

		// l'erreur à cause du "/2" ne s'accumule pas
		orientationMoyTick = currentAngle + deltaOrientationTick/2;

		if(orientationMoyTick > (uint32_t)TICKS_PAR_TOUR_ROBOT)
		{
			if(orientationMoyTick < (uint32_t)FRONTIERE_MODULO)
				orientationMoyTick -= (uint32_t)TICKS_PAR_TOUR_ROBOT;
			else
				orientationMoyTick += (uint32_t)TICKS_PAR_TOUR_ROBOT;
		}
		currentAngle += deltaOrientationTick;
		deltaOrientation = TICK_TO_RAD(deltaOrientationTick);

//		serial_rb.printfln("TICKS_PAR_TOUR_ROBOT = %d", (int)TICKS_PAR_TOUR_ROBOT);
//		serial_rb.printfln("orientationMoyTick = %d", orientationMoyTick);
//		serial_rb.printfln("orientation = %d", (int)(orientation_odo*1000));

		if(deltaOrientationTick == 0) // afin d'éviter la division par 0
			k = 1.;
		else
			k = sin(deltaOrientation/2)/(deltaOrientation/2);

		if(distance == 0) //  ça va arriver quand on fait par exemple une rotation sur place.
			courbure_odo = 0;
		else
			courbure_odo = deltaOrientationTick / distance;

		orientation_odo = TICK_TO_RAD(orientationMoyTick);
        cos_orientation_odo = cos(orientation_odo);
        sin_orientation_odo = sin(orientation_odo);

		x_odo += k*distance*cos_orientation_odo;
		y_odo += k*distance*sin_orientation_odo;
		xSemaphoreGive(odo_mutex);

		// ASSERVISSEMENT
        if(checkBlocageMecanique())
        {
            modeAsserActuel = STOP;
            sendProblemeMeca();
        }

		// on empêche toute modification de consigne
		while(xSemaphoreTake(consigneAsser_mutex, (TickType_t) (ATTENTE_MUTEX_MS / portTICK_PERIOD_MS)) != pdTRUE);
		if(modeAsserActuel == ROTATION)
			controlRotation();
		else if(modeAsserActuel == STOP)
			controlStop();
		else if(modeAsserActuel == VA_AU_POINT)
			controlVaAuPoint();
		else if(modeAsserActuel == COURBE)
			controlTrajectoire();
		if(needArrive && checkArrivee()) // gestion de la fin du mouvement
		{
			needArrive = false;
			modeAsserActuel = VA_AU_POINT;
			consigneX = x_odo;
			consigneY = y_odo;
			sendArrive();
		}
		// si ça vaut ASSER_OFF, il n'y a pas d'asser

		if(debugMode)
		{
			if((debugCompteur & 0x0F) == 0)
				sendDebug(leftPWM, rightPWM, currentLeftSpeed, currentRightSpeed, errorTranslation, errorAngle, vitesseLineaireReelle, courbureReelle);
			debugCompteur++;
		}
		xSemaphoreGive(consigneAsser_mutex);

	}
}

#endif
