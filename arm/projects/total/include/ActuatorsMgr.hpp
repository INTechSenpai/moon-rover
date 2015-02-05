#ifndef ACTUATORSMGR_HPP
#define ACTUATORSMGR_HPP

#include <ax12.hpp>
#include <Uart.hpp>
#include <Singleton.hpp>

#define bgOuvert 300
#define bgFerme 98
#define bdOuvert 5
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
#define cdMilieu 90
#define cdBas 148
#define cgHaut 237
#define cgMilieu 207
#define cgBas 152
#define ggOuvert 180
#define ggFerme 143
#define ggIntermediaire 153
#define gdOuvert 30
#define gdFerme 64
#define gdIntermediaire 54

class ActuatorsMgr: public Singleton<ActuatorsMgr> {
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
	AX<serial_ax>* bouffeBillesGauche;
	AX<serial_ax>* bouffeBillesDroit;

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
		bouffeBillesGauche = new AX<serial_ax>(10,1,1023);
		bouffeBillesDroit = new AX<serial_ax>(11,1,1023);
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
	void obd() {
		brasDroit->goTo(bdOuvert);
	}
	void fbd() {
		brasDroit->goTo(bdFerme);
	}
	void obg() {
		brasGauche->goTo(bgOuvert);
	}
	void fbg() {
		brasGauche->goTo(bgFerme);
	}
	void obdl() {

	}
	void fbdl() {

	}
	void obgl() {

	}
	void fbgl() {

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

	}
	void ab() {

	}
	void as() {

	}
	void ae() {

	}

};

#endif /* ACTUATORSMGR_HPP */
