#ifndef _SENSORMGR_h
#define _SENSORMGR_h

#include "Singleton.h"
#include <vector>
#include "InfraredSensor.h"
#include "ToF_longRange.h"
#include "ToF_shortRange.h"
#include "pin_mapping.h"
#include <Printable.h>
#include "Log.h"
#include "Median.h"

#define NB_SENSORS 12


class SensorMgr : public Singleton<SensorMgr>, public Printable
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

	enum UpdatePattern
	{
		NONE,
		FRONT_AND_BACK,
		FRONT_AND_SIDE,
		BACK_AND_SIDE,
		ALL
	};

	void powerOn()
	{
		irGauche.init();
		irDroit.init();
		tofAVGauche.powerON("avg");
		tofFlanAVGauche.powerON("favg");
		tofFlanARGauche.powerON("farg");
		tofARGauche.powerON("arg");
		tofARDroit.powerON("ard");
		tofFlanARDroit.powerON("fard");
		tofFlanAVDroit.powerON("favd");		
		tofAVDroit.powerON("avd");
		tofLPAvant.powerON("lpav");
		tofLPArriere.powerON("lpar");
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
			uint32_t mesureLPAvant, mesureLPArriere;
			switch (index)
			{
			case 0:
				mesureLPAvant = (tofLPAvant.getMesure() - 30) / 2;
				if (mesureLPAvant > 255)
				{
					mesureLPAvant = 255;
				}
				values[0] = (uint8_t)mesureLPAvant;
				break;
			case 1:
				irGaucheMedian.add(irGauche.getMesure());
				values[1] = irGaucheMedian.value();
				break;
			case 2:
				mesureLPArriere = (tofLPArriere.getMesure() - 30) / 2;
				if (mesureLPArriere > 255)
				{
					mesureLPArriere = 255;
				}
				values[2] = (uint8_t)mesureLPArriere;
				break;
			case 3:
				irDroitMedian.add(irDroit.getMesure());
				values[3] = irDroitMedian.value();
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

			Log::data(Log::SENSORS, *this);
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

	size_t printTo(Print& p) const
	{
		size_t charCount = p.printf("%u", values[0]);
		for (size_t i = 1; i < NB_SENSORS; i++)
		{
			charCount += p.printf("_%u", values[i]);
		}
		return charCount;
	}

	void setUpdatePattern(UpdatePattern updatePattern)
	{
		uint32_t pattern_none[NB_SENSORS] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		uint32_t pattern_fb[NB_SENSORS] = { 4375, 4375, 4375, 4375, 4375, 0, 0, 4375, 4375, 0, 0, 4375 };
		uint32_t pattern_fs[NB_SENSORS] = { 4375, 4375, 0, 4375, 4375, 4375, 4375, 0, 0, 4375, 4375, 4375 };
		uint32_t pattern_bs[NB_SENSORS] = { 0, 0, 4375, 0, 0, 4375, 4375, 4375, 4375, 4375, 4375, 0 };
		uint32_t pattern_a[NB_SENSORS] = { 4375, 4375, 4375, 4375, 4375, 4375, 4375, 4375, 4375, 4375, 4375, 4375 };

		switch (updatePattern)
		{
		case SensorMgr::NONE:
			setUpdatePattern(pattern_none);
			break;
		case SensorMgr::FRONT_AND_BACK:
			setUpdatePattern(pattern_fb);
			break;
		case SensorMgr::FRONT_AND_SIDE:
			setUpdatePattern(pattern_fs);
			break;
		case SensorMgr::BACK_AND_SIDE:
			setUpdatePattern(pattern_bs);
			break;
		case SensorMgr::ALL:
			setUpdatePattern(pattern_a);
			break;
		default:
			break;
		}
	}

private:
	/* 
		Contient les mesures de distance de chaque capteur, dans l'ordre d�fini plus bas.
		Une valeur � 0 signifie que le capteur n'a pas encore fourni de mesure.
		L'unit�e (cm ou mm) d�pend du capteur.
	*/
	uint8_t values[NB_SENSORS];
	ToF_longRange tofLPAvant;		// #1	[mm]
	InfraredSensor irGauche;		// #2	[cm]
	ToF_longRange tofLPArriere;		// #3	[mm]
	InfraredSensor irDroit;			// #4	[cm]
	ToF_shortRange tofAVGauche;		// #5	[mm]
	ToF_shortRange tofFlanAVGauche;	// #6	[mm]
	ToF_shortRange tofFlanARGauche;	// #7	[mm]
	ToF_shortRange tofARGauche;		// #8	[mm]
	ToF_shortRange tofARDroit;		// #9	[mm]
	ToF_shortRange tofFlanARDroit;	// #10	[mm]
	ToF_shortRange tofFlanAVDroit;	// #11	[mm]
	ToF_shortRange tofAVDroit;		// #12	[mm]

	Median<uint8_t, 3> irGaucheMedian;
	Median<uint8_t, 3> irDroitMedian;

	/*
		Contient pour chaque capteur (dans l'odre d�fini ci-dessus), le delai � attendre
		(apr�s le pr�c�dent) avant de l'actualiser.
		Si ce d�lai vaut 0, le capteur n'est jamais actualis�.
		Unit� : �s
	*/
	uint32_t updatePattern[NB_SENSORS];

	void setUpdatePattern(uint32_t pattern[NB_SENSORS])
	{
		for (size_t i = 0; i < NB_SENSORS; i++)
		{
			updatePattern[i] = pattern[i];
		}
	}
};


#endif

