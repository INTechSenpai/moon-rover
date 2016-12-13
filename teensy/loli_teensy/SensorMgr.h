#ifndef _SENSORMGR_h
#define _SENSORMGR_h

#include "Singleton.h"
#include <vector>
#include "InfraredSensor.h"
#include "ToF_longRange.h"
#include "ToF_shortRange.h"
#include "pin_mapping.h"

#define NB_SENSORS 12


class SensorMgr : public Singleton<SensorMgr>
{
public:
	SensorMgr() :
		tofLPAvant		(42, PIN_TOF_FRONT),
		irGauche		(0x00),
		tofLPArriere	(43, PIN_TOF_BACK),
		irDroit			(0x80),
		tofAVGauche		(44, PIN_TOF_PHARE_AV_G),
		tofFlanAVGauche	(45, PIN_TOF_FLAN_AV_G),
		tofFlanARGauche	(46, PIN_TOF_FLAN_AR_G),
		tofARGauche		(47, PIN_TOF_PHARE_AR_G),
		tofARDroit		(48, PIN_TOF_PHARE_AR_D),
		tofFlanARDroit	(49, PIN_TOF_FLAN_AR_D),
		tofFlanAVDroit	(50, PIN_TOF_FLAN_AV_D),
		tofAVDroit		(51, PIN_TOF_PHARE_AV_D)
	{
		for (int i = 0; i < NB_SENSORS; i++)
		{
			values[i] = 0;
			updatePattern[i] = 0;
		}
	}

	void powerOn()
	{
		tofLPAvant.powerON();
		irGauche.init();
		tofLPArriere.powerON();
		irDroit.init();
		tofAVGauche.powerON();
		tofFlanAVGauche.powerON();
		tofFlanARGauche.powerON();
		tofARGauche.powerON();
		tofARDroit.powerON();
		tofFlanARDroit.powerON();
		tofFlanAVDroit.powerON();
		tofAVDroit.powerON();
	}

	void powerOff()
	{
		tofLPAvant.standby();
		tofLPArriere.standby();
		tofAVGauche.standby();
		tofFlanAVGauche.standby();
		tofFlanARGauche.standby();
		tofARGauche.standby();
		tofARDroit.standby();
		tofFlanARDroit.standby();
		tofFlanAVDroit.standby();
		tofAVDroit.standby();
	}

	void update()
	{
		static int8_t index = 0;
		static uint32_t lastUpdateTime = 0;

		if (updatePattern[index] == 0)
		{
			index = (index + 1) % NB_SENSORS;
		}
		else if (micros() - lastUpdateTime >= updatePattern[index])
		{
			lastUpdateTime = micros();
			switch (index)
			{
			case 0:
				values[0] = tofLPAvant.getMesure();
				break;
			case 1:
				values[1] = irGauche.getMesure();
				break;
			case 2:
				values[2] = tofLPArriere.getMesure();
				break;
			case 3:
				values[3] = irDroit.getMesure();
				break;
			case 4:
				values[4] = tofAVGauche.getMesure();
				break;
			case 5:
				values[5] = tofFlanAVGauche.getMesure();
				break;
			case 6:
				values[6] = tofFlanARGauche.getMesure();
				break;
			case 7:
				values[7] = tofARGauche.getMesure();
				break;
			case 8:
				values[8] = tofARDroit.getMesure();
				break;
			case 9:
				values[9] = tofFlanARDroit.getMesure();
				break;
			case 10:
				values[10] = tofFlanAVDroit.getMesure();
				break;
			case 11:
				values[11] = tofAVDroit.getMesure();
				break;
			default:
				break;
			}
			index = (index + 1) % NB_SENSORS;
		}
	}

	void setUpdatePattern(uint32_t pattern[NB_SENSORS])
	{
		for (size_t i = 0; i < NB_SENSORS; i++)
		{
			updatePattern[i] = pattern[i];
		}
	}

	std::vector<uint8_t> getValues()
	{
		std::vector<uint8_t> values_vect;
		for (int i = 0; i < NB_SENSORS; i++)
		{
			values_vect.push_back(values[i]);
			values[i] = 0;
		}
		return values_vect;
	}

	void getValues_noReset(uint8_t tab[NB_SENSORS])
	{
		for (size_t i = 0; i < NB_SENSORS; i++)
		{
			tab[i] = values[i];
		}
	}

private:
	/* 
		Contient les mesures de distance de chaque capteur, dans l'ordre défini plus bas.
		Une valeur à 0 signifie que le capteur n'a pas encore fourni de mesure.
		L'unitée (cm ou mm) dépend du capteur.
	*/
	uint8_t values[NB_SENSORS];
	ToF_longRange tofLPAvant;		// #1	[cm]
	InfraredSensor irGauche;		// #2	[cm]
	ToF_longRange tofLPArriere;		// #3	[cm]
	InfraredSensor irDroit;			// #4	[cm]
	ToF_shortRange tofAVGauche;		// #5	[mm]
	ToF_shortRange tofFlanAVGauche;	// #6	[mm]
	ToF_shortRange tofFlanARGauche;	// #7	[mm]
	ToF_shortRange tofARGauche;		// #8	[mm]
	ToF_shortRange tofARDroit;		// #9	[mm]
	ToF_shortRange tofFlanARDroit;	// #10	[mm]
	ToF_shortRange tofFlanAVDroit;	// #11	[mm]
	ToF_shortRange tofAVDroit;		// #12	[mm]

	/*
		Contient pour chaque capteur (dans l'odre défini ci-dessus), le delai à attendre
		(après le précédent) avant de l'actualiser.
		Si ce délai vaut 0, le capteur n'est jamais actualisé.
		Unité : µs
	*/
	uint32_t updatePattern[NB_SENSORS];
};


#endif

