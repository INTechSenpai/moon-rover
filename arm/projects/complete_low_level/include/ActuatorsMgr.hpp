#ifndef ACTUATORSMGR_HPP
#define ACTUATORSMGR_HPP

#include <ax12.hpp>
#include <Uart.hpp>
#include <Singleton.hpp>
#include "stm32f4xx_gpio.h"
#include "stm32f4xx_rcc.h"
#include "stm32f4xx_exti.h"
#include "stm32f4xx_syscfg.h"
#include "misc.h"

extern Uart<1> serial;

#define bgOuvert 300
#define bgFerme 98
#define bgMilieu 208
#define bdOuvert 0
#define bdMilieu 95
#define bdFerme 203
#define mgOuvert 240
#define mgFerme 65
#define mdOuvert 80
#define mdFerme 230
#define tdPose 211
#define tdRange 150
#define tgPose 160
#define tgRange 222
#define cdHaut 65
#define cdMilieu 100
#define cdBas 148
#define cgHaut 237
#define cgMilieu 200
#define cgBas 152
#define ggOuvert 180
#define ggFerme 143
#define ggIntermediaire 153
#define gdOuvert 30
#define gdFerme 64
#define gdIntermediaire 54

class ActuatorsMgr : public Singleton<ActuatorsMgr>
{
private:
	typedef Uart<2> serial_ax;
	AX<serial_ax>* machoireDroite;
	AX<serial_ax>* machoireGauche;
	AX<serial_ax>* brasDroit;
	AX<serial_ax>* brasGauche;
	AX<serial_ax>* guideDroit;
	AX<serial_ax>* guideGauche;
	AX<serial_ax>* tapisDroit;
	AX<serial_ax>* tapisGauche;
	AX<serial_ax>* clapDroit;
	AX<serial_ax>* clapGauche;

	enum EtatAscenseur
	{
		Haut = 6,			//Position extrème haute (dépassement de l'anti-retour, pour monter un plot)
		Milieu = 5,			//Quelque part entre 'Estrade' et 'Haut' (très peu précis)
		Estrade = 4,		//A plus de 22mm du sol (et pas beaucoup plus haut)
		SousEstrade = 3,
		Bas = 2,			//Ne touche ni le sol ni les plots supérieurs (position pour rouler)
		Sol = 1,			//Position extrème basse (touche le sol)
		SousSol = 0			//Position de blocage contre le sol (le moteur force, il faut détecter cet état pour en sortir au plus vite)
	};

	volatile EtatAscenseur etatAscenseur;
	volatile EtatAscenseur consigneAscenseur;
	uint16_t vitesseOuvertureBrasLente;
	uint16_t vitesseFermetureBrasLente;

public:
	ActuatorsMgr()
	{
		machoireDroite = new AX<serial_ax>(0,1,1023);
		machoireGauche = new AX<serial_ax>(1,1,1023);
		brasDroit = new AX<serial_ax>(2,1,1023);
		brasGauche = new AX<serial_ax>(3,1,1023);
		guideDroit = new AX<serial_ax>(4,1,1023);
		guideGauche = new AX<serial_ax>(5,1,1023);
		tapisDroit = new AX<serial_ax>(6,1,1023);
		tapisGauche = new AX<serial_ax>(7,1,1023);
		clapDroit = new AX<serial_ax>(8,1,1023);
		clapGauche = new AX<serial_ax>(9,1,1023);

		machoireDroite->init();

		etatAscenseur = Sol;
		consigneAscenseur = Sol;
		vitesseOuvertureBrasLente = 25;
		vitesseFermetureBrasLente = 20;
		/* Set variables used */
		GPIO_InitTypeDef GPIO_InitStruct;
		GPIO_StructInit(&GPIO_InitStruct); //Remplit avec les valeurs par défaut


		/*
		 * Initialisation des PIN du moteur du monte-plot
		 *
		 * Pin_PWM : PC8
		 * Pin_Sens: PD14
		 */

		/* Activation de l'horloge du port GPIOC */
		RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);

		/*Réglages de la pin*/
		GPIO_InitStruct.GPIO_Mode = GPIO_Mode_OUT;
		GPIO_InitStruct.GPIO_Pin = GPIO_Pin_8;
		GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
		GPIO_Init(GPIOC, &GPIO_InitStruct);

		/* Activation de l'horloge du port GPIOD */
		RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOD, ENABLE);

		/*Réglages de la pin*/
		GPIO_InitStruct.GPIO_Mode = GPIO_Mode_OUT;
		GPIO_InitStruct.GPIO_Pin = GPIO_Pin_14;
		GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
		GPIO_Init(GPIOD, &GPIO_InitStruct);



		/*
		 * Capteur haut de l'ascenseur : PC13
		 */

		/* Activation de l'horloge du port GPIOC */
		RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);

		/*Réglages de la pin*/
		GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN;
		GPIO_InitStruct.GPIO_OType = GPIO_OType_PP;
		GPIO_InitStruct.GPIO_Pin = GPIO_Pin_13;
		GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_NOPULL;
		GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
		GPIO_Init(GPIOC, &GPIO_InitStruct);


		/*
		 * Capteur bas de l'ascenseur : PE5
		 */

		/* Activation de l'horloge du port GPIOE */
		RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOE, ENABLE);

		/*Réglages de la pin*/
		GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN;
		GPIO_InitStruct.GPIO_OType = GPIO_OType_PP;
		GPIO_InitStruct.GPIO_Pin = GPIO_Pin_5;
		GPIO_InitStruct.GPIO_PuPd = GPIO_PuPd_NOPULL;
		GPIO_InitStruct.GPIO_Speed = GPIO_Speed_100MHz;
		GPIO_Init(GPIOE, &GPIO_InitStruct);

	}

	~ActuatorsMgr()
	{
		delete(machoireDroite);
		delete(machoireGauche);
		delete(brasDroit);
		delete(brasGauche);
		delete(clapDroit);
		delete(clapGauche);
		delete(guideDroit);
		delete(guideGauche);
		delete(tapisDroit);
		delete(tapisGauche);
		delete(clapDroit);
		delete(clapGauche);
	}

	void refreshElevatorState()
	{

		//Capteur Haut : PC13
		//Capteur Bas  : PE5
		//Moteur-PWM   : PC8
		//Moteur-Sens  : PD14

		uint8_t captHautON = !GPIO_ReadInputDataBit(GPIOC, GPIO_Pin_13),
		captBasON = GPIO_ReadInputDataBit(GPIOE, GPIO_Pin_5),
		moteurON = GPIO_ReadOutputDataBit(GPIOC, GPIO_Pin_8),
		moteurMonte = GPIO_ReadOutputDataBit(GPIOD, GPIO_Pin_14);



		//Mise à jour de la position à partir des capteurs
		if(captHautON)//Si le contact du haut est appuyé
		{
			etatAscenseur = Haut;
			//serial.printfln("Haut");
		}
		else if(!captHautON && etatAscenseur == Haut)//Si le contact haut est relaché alors qu'on était en haut
		{
			etatAscenseur = Milieu;
			//serial.printfln("Milieu");
		}
		else if(captBasON && (etatAscenseur == Milieu || etatAscenseur == Estrade))//Contact bas appuyé alors qu'on était au milieu
		{
			etatAscenseur = SousEstrade;
			//serial.printfln("SousEstrade");
		}
		else if((!captBasON && (etatAscenseur == Bas || etatAscenseur == SousEstrade)) && (!moteurMonte || !moteurON))
		{
			etatAscenseur = Sol;
			//serial.printfln("Sol");
		}
		else if(captBasON && etatAscenseur == Sol && moteurMonte)
		{
			etatAscenseur = Bas;
			//serial.printfln("Bas");
		}
		else if(captBasON && etatAscenseur == Sol && (!moteurMonte || !moteurON))
		{
			etatAscenseur = SousSol;
			//serial.printfln("Bas");
		}
		else if(!captBasON && (etatAscenseur == SousEstrade || etatAscenseur == Bas) && moteurMonte)
		{
			etatAscenseur = Estrade;
			//serial.printfln("Estrade");
		}
		else if(!captBasON && etatAscenseur == SousSol)
		{
			etatAscenseur = Sol;
		}



		//Déplacement de l'ascenseur selon la consigne

		if(consigneAscenseur == etatAscenseur || (consigneAscenseur == Sol && etatAscenseur == SousSol))
		{
			GPIO_ResetBits(GPIOC, GPIO_Pin_8);//Arrêt du moteur
		}
		else if(consigneAscenseur > etatAscenseur)
		{
			GPIO_SetBits(GPIOD, GPIO_Pin_14);//Sens de l'ascenseur = Monter
			GPIO_SetBits(GPIOC, GPIO_Pin_8);//Mise en marche du moteur
		}
		else
		{
			GPIO_ResetBits(GPIOD, GPIO_Pin_14);//Sens de l'ascenseur = Descendre
			GPIO_SetBits(GPIOC, GPIO_Pin_8);//Mise en marche du moteur
		}
	}

	void e(uint16_t angle){
		clapGauche->goTo(angle);
	}
	void setAllID(){
		int i;
		serial.printfln("Reglage des ID des AX12");
		serial.printfln("Brancher uniquement l'AX12 indique");
		serial.printf("\n");

		serial.printfln("Machoire droite");
		serial.read(i);
		machoireDroite->initIDB(0);
		serial.printfln("done");

		serial.printfln("Machoire gauche");
		serial.read(i);
		machoireDroite->initIDB(1);
		serial.printfln("done");

		serial.printfln("Bras droit");
		serial.read(i);
		machoireDroite->initIDB(2);
		serial.printfln("done");

		serial.printfln("Bras gauche");
		serial.read(i);
		machoireDroite->initIDB(3);
		serial.printfln("done");

		serial.printfln("Guide droit");
		serial.read(i);
		machoireDroite->initIDB(4);
		serial.printfln("done");

		serial.printfln("Guide gauche");
		serial.read(i);
		machoireDroite->initIDB(5);
		serial.printfln("done");

		serial.printfln("Tapis droit");
		serial.read(i);
		machoireDroite->initIDB(6);
		serial.printfln("done");

		serial.printfln("Tapis gauche");
		serial.read(i);
		machoireDroite->initIDB(7);
		serial.printfln("done");

		serial.printfln("Clap droit");
		serial.read(i);
		machoireDroite->initIDB(8);
		serial.printfln("done");

		serial.printfln("Clap gauche");
		serial.read(i);
		machoireDroite->initIDB(9);
		serial.printfln("done");

		serial.printfln("Fin du reglage");
	}
	void omd() {
		machoireDroite->goTo(mdOuvert);
	}
	void fmd() {
		machoireDroite->goTo(mdFerme);
	}
	void omg() {
		machoireGauche->goTo(mgOuvert);
	}
	void fmg() {
		machoireGauche->goTo(mgFerme);
	}

	void setArmSpeed(uint16_t speed) {
		vitesseFermetureBrasLente = speed;
	}

	void obd() {
		brasDroit->changeSpeed(100);
		brasDroit->goTo(bdOuvert);
	}
	void fbd() {
		brasDroit->changeSpeed(100);
		brasDroit->goTo(bdFerme);
	}
	void mbd() {
		brasDroit->changeSpeed(100);
		brasDroit->goTo(bdMilieu);
	}
	void obg() {
		brasGauche->changeSpeed(100);
		brasGauche->goTo(bgOuvert);
	}
	void fbg() {
		brasGauche->changeSpeed(100);
		brasGauche->goTo(bgFerme);
	}
	void mbg(){
		brasGauche->changeSpeed(100);
		brasGauche->goTo(bgMilieu);
	}
	void obdl() {
		brasDroit->changeSpeed(vitesseOuvertureBrasLente);
		brasDroit->goTo(bdOuvert);
	}
	void fbdl() {
		brasDroit->changeSpeed(vitesseFermetureBrasLente);
		brasDroit->goTo(bdFerme);
	}
	void obgl() {
		brasGauche->changeSpeed(vitesseOuvertureBrasLente);
		brasGauche->goTo(bgOuvert);
	}
	void fbgl() {
		brasGauche->changeSpeed(vitesseFermetureBrasLente);
		brasGauche->goTo(bgFerme);
	}
	void ogd() {
		guideDroit->goTo(gdOuvert);
	}
	void fgd() {
		guideDroit->goTo(gdFerme);
	}
	void gdi() {
		guideDroit->goTo(gdIntermediaire);
	}
	void ogg() {
		guideGauche->goTo(ggOuvert);
	}
	void fgg() {
		guideGauche->goTo(ggFerme);
	}
	void ggi() {
		guideGauche->goTo(ggIntermediaire);
	}
	void ptd() {
		tapisDroit->goTo(tdPose);
	}
	void rtd() {
		tapisDroit->goTo(tdRange);
	}
	void ptg() {
		tapisGauche->goTo(tgPose);
	}
	void rtg() {
		tapisGauche->goTo(tgRange);
	}
	void cdh() {
		clapDroit->goTo(cdHaut);
	}
	void cdm() {
		clapDroit->goTo(cdMilieu);
	}
	void cdb() {
		clapDroit->goTo(cdBas);
	}
	void cgh() {
		clapGauche->goTo(cgHaut);
	}
	void cgm() {
		clapGauche->goTo(cgMilieu);
	}
	void cgb() {
		clapGauche->goTo(cgBas);
	}
	void ah() {
		consigneAscenseur = Haut;
		//refreshElevatorState();
	}
	void ab() {
		consigneAscenseur = Bas;
		//refreshElevatorState();
	}
	void as() {
		consigneAscenseur = Sol;
		//refreshElevatorState();
	}
	void ae() {
		consigneAscenseur = Estrade;
		//refreshElevatorState();
	}
	void ase() {
		consigneAscenseur = SousEstrade;
	}

	void broad(){
		machoireDroite->goToB(100);
		machoireDroite->goToB(0);
	}

	void reanimation()
	{
		serial.printfln("REANIMATION");
		machoireDroite->reanimationMode(9600);
		serial.printfln("next...");
		machoireGauche->reanimationMode(9600);
		serial.printfln("next...");
		brasDroit->reanimationMode(9600);
		serial.printfln("next...");
		brasGauche->reanimationMode(9600);
		serial.printfln("next...");
		guideDroit->reanimationMode(9600);
		serial.printfln("next...");
		guideGauche->reanimationMode(9600);
		serial.printfln("next...");
		tapisDroit->reanimationMode(9600);
		serial.printfln("next...");
		tapisGauche->reanimationMode(9600);
		serial.printfln("next...");
		clapDroit->reanimationMode(9600);
		serial.printfln("next...");
		clapGauche->reanimationMode(9600);
		serial.printfln("done");
	}

	void testSpeed()
	{
		serial.printfln("Test vitesse actionneurs");

		serial.printfln("Machoire droite");
		findTimeEllapsed(&ActuatorsMgr::fmd, &ActuatorsMgr::omd);

		serial.printfln("Bras droit 1");
		findTimeEllapsed(&ActuatorsMgr::fbd, &ActuatorsMgr::obd);

		serial.printfln("Bras droit 2");
		findTimeEllapsed(&ActuatorsMgr::fbd, &ActuatorsMgr::mbd);

		serial.printfln("Bras droit 3");
		findTimeEllapsed(&ActuatorsMgr::mbd, &ActuatorsMgr::obd);

		serial.printfln("Bras droit lentement");
		findTimeEllapsed(&ActuatorsMgr::fbdl, &ActuatorsMgr::obdl);

		serial.printfln("Clap droit 1");
		findTimeEllapsed(&ActuatorsMgr::cdb, &ActuatorsMgr::cdh);

		serial.printfln("Clap droit 2");
		findTimeEllapsed(&ActuatorsMgr::cdb, &ActuatorsMgr::cdm);

		serial.printfln("Clap droit 3");
		findTimeEllapsed(&ActuatorsMgr::cdm, &ActuatorsMgr::cdh);

		serial.printfln("Clap droit 4");
		findTimeEllapsed(&ActuatorsMgr::cdh, &ActuatorsMgr::cdb);

		serial.printfln("Clap droit 5");
		findTimeEllapsed(&ActuatorsMgr::cdm, &ActuatorsMgr::cdb);

		serial.printfln("Clap droit 6");
		findTimeEllapsed(&ActuatorsMgr::cdh, &ActuatorsMgr::cdm);

		serial.printfln("Tapis droit descente");
		findTimeEllapsed(&ActuatorsMgr::rtd, &ActuatorsMgr::ptd);

		serial.printfln("Tapis droit montee");
		findTimeEllapsed(&ActuatorsMgr::ptd, &ActuatorsMgr::rtd);

		omd();
		serial.printfln("Guide droit 1");
		findTimeEllapsed(&ActuatorsMgr::fgg, &ActuatorsMgr::ogg);

		serial.printfln("Guide droit 2");
		findTimeEllapsed(&ActuatorsMgr::fgg, &ActuatorsMgr::ggi);

		serial.printfln("Guide droit 3");
		findTimeEllapsed(&ActuatorsMgr::ggi, &ActuatorsMgr::ogg);
	}

	void testSpeedElevator()
	{
		serial.printfln("Test vitesse de l'ascenseur");
	}



private:
	void findTimeEllapsed(void (ActuatorsMgr::*gotoPositionA)(void), void (ActuatorsMgr::*gotoPositionB)(void))
	{
		char userSay[64];
		int delai = 1000, delai_trop_court = 0, delai_trop_long = 2000;
		(this->*gotoPositionA)();
		Delay(delai);

		while(delai_trop_long - delai_trop_court > 50)
		{
			serial.printf("Test avec %d ms", delai);
			serial.read(userSay);
			(this->*gotoPositionB)();
			Delay(delai);
			(this->*gotoPositionA)();
			bool redemander = true;
			while(redemander)
			{
				serial.printfln("Temps d'attente ? (+ augmenter ; - diminuer ; o OK)");
				serial.read(userSay);
				if(!strcmp("+",userSay))
				{
					delai_trop_court = delai;
					delai = (delai + delai_trop_long)/2;
					redemander = false;
				}
				else if (!strcmp("-",userSay))
				{
					delai_trop_long = delai;
					delai = (delai_trop_court + delai)/2;
					redemander = false;
				}
				else if (!strcmp("o",userSay))
				{
					delai_trop_court = delai;
					delai_trop_long = delai;
					redemander = false;
				}
			}
		}
		serial.printfln("Delai optimal : %d ms", delai);
		serial.read(userSay);
	}
};

#endif /* ACTUATORSMGR_HPP */
