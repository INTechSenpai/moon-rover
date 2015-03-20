#ifndef CAPTEUR_SRF05_HPP
#define CAPTEUR_SRF05_HPP

#include <stdint.h>
#include "delay.h"
#include "ring_buffer.hpp"
#include "Uart.hpp"

#define NB_VALEURS_MEDIANE_SRF  4

typedef ring_buffer<uint32_t, NB_VALEURS_MEDIANE_SRF> ringBufferSRF;
extern Uart<1> serial;

/** @file library/capteur_srf05.hpp
 *  @brief Ce fichier crée une classe capteur_srf05 pour pouvoir utiliser simplement les capteurs SRF05.
 *  @author Sylvain (adaptation du travail de Thibaut ~MissFrance~)
 *  @date 23 février 2015
 */

//Angle du cône de vision: 38°
//Distance maximale: 230cm


/** @class capteur_srf05
 *  \brief Classe pour pouvoir gérer facilement les capteurs srf05.
 * 
 * 
 *  La classe gère la récupération d'une distance entre le capteur et un obstacle.
 *  
 *  Protocole de ces capteurs :
 *  ---------------------------
 *
 *  La carte envoie une impulsion sur la pin pendant une durée de ~10µs. Puis, après
 *  une durée inconnue, le capteur envoie une impulsion sur cette même pin. La durée
 *  de cette impulsion est proportionnelle à la distance entre les capteurs et l'objet
 *  détecté.
 */


class CapteurSRF
{
public:
	CapteurSRF()
	{
		derniereDistance = 0;
		origineTimer = 0;
	}

	void init(GPIO_TypeDef* GPIOx, GPIO_InitTypeDef GPIO_sensor, EXTI_InitTypeDef EXTI_sensor)
	{
		this->GPIOx = GPIOx;
		this->GPIO_sensor = GPIO_sensor;
		this->EXTI_sensor = EXTI_sensor;
	}

	uint32_t value() const
	{
		return derniereDistance;
	}

	void refresh()
	{
		EXTI_sensor.EXTI_LineCmd = DISABLE;
		EXTI_Init(&EXTI_sensor);

			// On met la pin en output
		GPIO_sensor.GPIO_Mode = GPIO_Mode_OUT;
		GPIO_Init(GPIOx, &GPIO_sensor);

			// On met un zéro sur la pin pour 2 µs
		GPIO_ResetBits(GPIOx, GPIO_sensor.GPIO_Pin);
		Delay_us(2);

			// On met un "un" sur la pin pour 10 µs
		GPIO_SetBits(GPIOx, GPIO_sensor.GPIO_Pin);
		Delay_us(10);

		GPIO_ResetBits(GPIOx, GPIO_sensor.GPIO_Pin);

			// Le signal a été envoyé, maintenant on attend la réponse dans l'interruption
		GPIO_sensor.GPIO_Mode = GPIO_Mode_IN;
		GPIO_Init(GPIOx, &GPIO_sensor);
		EXTI_sensor.EXTI_Trigger = EXTI_Trigger_Rising;		//On va maintenant recevoir un front montant, il faut se préparer pour ça
		EXTI_sensor.EXTI_LineCmd = ENABLE;					//On accepte donc de lire les interruptions sur la pin du capteur à partir de maintenant
		EXTI_Init(&EXTI_sensor);
	}


	/** Fonction appellée par l'interruption. S'occupe d'enregistrer la valeur de la longueur
	 *  de l'impulsion retournée par le capteur, et de la convertir en une distance en mm.
	 */
//	void interruption()
//	{
//		// Front montant si bit == 1, descendant sinon.
//		static uint8_t ancienBit=0;
//		uint8_t bit = GPIO_ReadInputDataBit(GPIOx, GPIO_sensor.GPIO_Pin);
//
//		// Début de l'impulsion
//		if (bit && bit!=ancienBit)
//		{
//			origineTimer = Micros();
//			ancienBit=bit;
//		}
//
//		// Fin de l'impulsion
//		else if(!(bit) && bit!=ancienBit)
//		{
//			uint32_t temps_impulsion;
//			ancienBit=bit;
//				//Enregistrement de la dernière distance calculée, mais sans l'envoyer (l'envoi se fait par la méthode value)
//			uint32_t current_time;
//			current_time = Micros();
//			temps_impulsion = current_time - origineTimer;
//			ringBufferValeurs.append( 10*temps_impulsion/58 );
//			derniereDistance = mediane(ringBufferValeurs);
//			serial.printf("");//No hack here, follow your path...
//		}
//		else
//		{
//			//serial.printfln("I knew it !");
//		}
//	}

	void interruption()
	{
		static bool risingEdgeTrigger = true;

		if(risingEdgeTrigger)
		{
			origineTimer = Micros();
			risingEdgeTrigger = false;
			EXTI_sensor.EXTI_Trigger = EXTI_Trigger_Falling;	//On devrait recevoir désormais un front descendant
			EXTI_Init(&EXTI_sensor);
		}
		else
		{
			uint32_t temps_impulsion, current_time;
			current_time = Micros();
			temps_impulsion = current_time - origineTimer;		//Le temps entre les deux fronts
			ringBufferValeurs.append( 10*temps_impulsion/58 );	//On ajoute la distance mesurée à cet instant dans un buffer, calculé ainsi en fonction du temps entre les fronts
			derniereDistance = mediane(ringBufferValeurs);		//Ce qu'on renvoie est la médiane du buffer, ainsi on élimine les valeurs extrêmes qui peuvent être absurdes
			//serial.printfln("%d", 10*temps_impulsion/58);//No hack here, follow your path...
			risingEdgeTrigger = true;
			EXTI_sensor.EXTI_LineCmd = DISABLE;					//On a reçu la réponse qui nous intéressait, on désactive donc les lectures d'interruptions sur ce capteur
			EXTI_Init(&EXTI_sensor);
		}
	}

private:
	GPIO_InitTypeDef GPIO_sensor;//Variable permettant de régler les paramètres de la pin du capteur
	EXTI_InitTypeDef EXTI_sensor;//Variable permettant de régler le vecteur d'interruptions associé au capteur
	GPIO_TypeDef* GPIOx;//Port de la pin du capteur
	ringBufferSRF ringBufferValeurs;
	uint32_t derniereDistance;		//contient la dernière distance acquise, prête à être envoyée
	uint32_t origineTimer;			//origine de temps afin de mesurer une durée
};

#endif
