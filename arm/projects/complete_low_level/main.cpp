#include "Uart.hpp"
#include "MotionControlSystem.h"
#include "delay.h"
#include "ActuatorsMgr.hpp"
#include "SensorMgr.h"



int main(void)
{
	Delay_Init();
	Uart<1> serial;
	Uart<2> serial_ax;
	serial.init(115200);
	serial_ax.init(9600);

	MotionControlSystem* motionControlSystem = &MotionControlSystem::Instance();
	motionControlSystem->init(100, 100);
	ActuatorsMgr* actuatorsMgr = &ActuatorsMgr::Instance();
	SensorMgr* sensorMgr = &SensorMgr::Instance();

	char order[64];//Permet le stockage du message reçu par la liaison série

	bool translation = true;//permet de basculer entre les réglages de cte d'asserv en translation et en rotation

	while(1)
	{
		sensorMgr->refresh();

		uint8_t tailleBuffer = serial.available();

		if (tailleBuffer && tailleBuffer < RX_BUFFER_SIZE - 1)
		{
			serial.read(order);
			serial.printfln("_");//Acquittement

			if(!strcmp("?",order))				//Ping
			{
				serial.printfln("0");
			}
			else if(!strcmp("f",order))			//Indiquer l'état du mouvement du robot
			{
				serial.printfln("%d", motionControlSystem->isMoving());//Robot en mouvement ou pas ?
				serial.printfln("%d", motionControlSystem->isMoveAbnormal());//Cet état du mouvement est il anormal ?
			}
			else if(!strcmp("?xyo",order))		//Indiquer la position du robot (en mm et radians)
			{
				motionControlSystem->track();
				serial.printfln("%f", motionControlSystem->getX());
				serial.printfln("%f", motionControlSystem->getY());
				serial.printfln("%f", motionControlSystem->getAngleRadian());
			}
			else if(!strcmp("d", order))		//Ordre de déplacement rectiligne (en mm)
			{
				int deplacement = 0;
				serial.read(deplacement);
				serial.printfln("_");//Acquittement
				motionControlSystem->orderTranslation(deplacement);
			}
			else if(!strcmp("t", order))		//Ordre de rotation via un angle absolu (en radians)
			{
				float angle = motionControlSystem->getAngleRadian();
				serial.read(angle);
				serial.printfln("_");//Acquittement
				motionControlSystem->orderRotation(angle);
			}
			else if(!strcmp("t3", order))		//Ordre de rotation via un angle relatif (en radians)
			{
				float angle_actuel = motionControlSystem->getAngleRadian(), delta_angle = 0;
				serial.read(delta_angle);
				serial.printfln("_");
				motionControlSystem->orderRotation(angle_actuel + delta_angle);
			}
			else if(!strcmp("r", order))		//Ordre de rotation via un angle relatif (en degrés)
			{
				float angle_actuel = motionControlSystem->getAngleRadian()*180/PI, delta_angle = 0;
				serial.read(delta_angle);
				serial.printfln("_");
				motionControlSystem->orderRotation((angle_actuel + delta_angle)*PI/180);
			}
			else if(!strcmp("stop",order))		//Ordre d'arrêt (asservissement à la position actuelle)
			{
				motionControlSystem->stop();
			}
			else if(!strcmp("us_av",order))		//Indiquer les distances mesurées par les capteurs avant
			{
				serial.printfln("%d", sensorMgr->getLeftFrontValue());//Distance mesurée par l'ultrason avant gauche, en mm
				serial.printfln("%d", sensorMgr->getRightFrontValue());//Distance mesurée par l'ultrason avant droit, en mm
			}
			else if(!strcmp("us_ar",order))		//Indiquer les distances mesurées par les capteurs arrière
			{
				serial.printfln("%d", sensorMgr->getLeftBackValue());//Distance mesurée par l'ultrason arrière gauche, en mm
				serial.printfln("%d", sensorMgr->getRightBackValue());//Distance mesurée par l'ultrason arrière droit, en mm
			}
			else if(!strcmp("j",order))			//Indiquer l'état du jumper (0='en place'; 1='dehors')
			{
				serial.printfln("%d", sensorMgr->isJumperOut());
			}
			else if(!strcmp("ccg",order))		//Indiquer l'état du contacteur du porte-gobelet gauche
			{
				serial.printfln("%d", sensorMgr->isLeftGlassInside());
			}
			else if(!strcmp("ccd",order))		//Indiquer l'état du contacteur du porte-gobelet droit
			{
				serial.printfln("%d", sensorMgr->isRightGlassInside());
			}
			else if(!strcmp("ccm",order))		//Indiquer l'état du contacteur intérieur du monte-plot
			{
				serial.printfln("%d", sensorMgr->isPlotInside());
			}
			else if(!strcmp("ct0",order))		//Désactiver l'asservissement en translation
			{
				motionControlSystem->enableTranslationControl(false);
			}
			else if(!strcmp("ct1",order))		//Activer l'asservissement en translation
			{
				motionControlSystem->enableTranslationControl(true);
			}
			else if(!strcmp("cr0",order))		//Désactiver l'asservissement en rotation
			{
				motionControlSystem->enableRotationControl(false);
			}
			else if(!strcmp("cr1",order))		//Activer l'asservissement en rotation
			{
				motionControlSystem->enableRotationControl(true);
			}
			else if(!strcmp("cx",order))		//Régler la composante x de la position (en mm)
			{
				float x;
				serial.read(x);
				serial.printfln("_");//Acquittement
				motionControlSystem->setX(x);
			}
			else if(!strcmp("cy",order))		//Régler la composante y de la position (en mm)
			{
				float y;
				serial.read(y);
				serial.printfln("_");//Acquittement
				motionControlSystem->setY(y);
			}
			else if(!strcmp("co",order))		//Régler l'orientation du robot (en radians)
			{
				float o;
				serial.read(o);
				serial.printfln("_");//Acquittement
				motionControlSystem->setOriginalAngle(o);
			}





/*			 __________________
 * 		   *|                  |*
 *		   *|COMMANDES DE DEBUG|*
 *		   *|__________________|*
 */
			else if(!strcmp("!",order))//Test quelconque
			{

			}
			else if(!strcmp("oxy",order))
			{
				serial.printfln("x=%f\r\ny=%f", motionControlSystem->getX(), motionControlSystem->getY());
				serial.printfln("o=%f", motionControlSystem->getAngleRadian());
			}
			else if(!strcmp("ticks", order))
			{
				serial.printfln("%d", Counter::getLeftValue());
				serial.printfln("%d", Counter::getRightValue());
				serial.printfln("%d", motionControlSystem->currentDistance);
				serial.printfln("%d", motionControlSystem->currentAngle);
			}
			else if(!strcmp("c", order))
			{
				serial.printfln("Valeurs des codeuses : %d a gauche", motionControlSystem->getLeftEncoder());
				serial.printfln("Valeurs des codeuses : %d a droite", motionControlSystem->getRightEncoder());
			}
			else if (!strcmp("broad",order))
			{
				actuatorsMgr->broad();
			}
			else if (!strcmp("at", order))	// Commute l'asservissement en translation
			{
				static bool asservTranslation = false;
				motionControlSystem->enableTranslationControl(asservTranslation);
				serial.printfln("l'asserv en translation est désormais");
				if (asservTranslation)
				{
					serial.printfln("asservi en translation");
				}
				else
				{
					serial.printfln("non asservi en translation");
				}
				asservTranslation = !asservTranslation;
			}
			else if (!strcmp("ar", order)) // Commute l'asservissement en rotation
			{
				static bool asservRotation = false;
				motionControlSystem->enableRotationControl(asservRotation);
				serial.printfln("l'asserv en rotation est désormais");
				if (asservRotation)
				{
					serial.printfln("asservi en rotation");
				}
				else
				{
					serial.printfln("non asservi en rotation");
				}
				asservRotation = !asservRotation;
			}
			else if(!strcmp("unitMove", order))
			{
				motionControlSystem->orderRawPwm(Side::LEFT, 90);
				motionControlSystem->orderRawPwm(Side::RIGHT, 90);
				Delay(500);
				motionControlSystem->stop();
			}
			else if(!strcmp("pwm",order))
			{
				serial.printfln("Pwm trans : %d", motionControlSystem->getPWMTranslation());
				serial.printfln("Pwm rotation : %d", motionControlSystem->getPWMRotation());
			}
			else if(!strcmp("g",order))
			{
				serial.printfln("Objectif en translation: %d   actuel : %d", motionControlSystem->getTranslationGoal(), motionControlSystem->currentDistance);
				serial.printfln("Objectif en rotation : %d    actuel : %d", motionControlSystem->getRotationGoal(), motionControlSystem->currentAngle);

			}
			else if(!strcmp("kp",order))//Test d'une valeur de Kp
			{
				float kp, ki, kd;
				serial.printfln("kp?");
				if (translation)
				{
					motionControlSystem->getTranslationTunings(kp,ki,kd);
					serial.read(kp);
					serial.printfln("kp_trans = %f", kp);
					motionControlSystem->setTranslationTunings(kp,ki,kd);
					motionControlSystem->orderTranslation(300);
					for(int t=0; t<12; t++)
					{
//						serial.printfln("Pwm trans : %d", motionControlSystem->getPWMTranslation());
//						serial.printfln("Pwm rotation : %d", motionControlSystem->getPWMRotation());
						Delay(250);
					}
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
					motionControlSystem->orderTranslation(-300);
					for(int t=0; t<12; t++)
					{
//						serial.printfln("Pwm trans : %d", motionControlSystem->getPWMTranslation());
//						serial.printfln("Pwm rotation : %d", motionControlSystem->getPWMRotation());
						Delay(250);
					}
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
				}
				else
				{
					motionControlSystem->getRotationTunings(kp,ki,kd);
					serial.read(kp);
					serial.printfln("kp_rot = %f", kp);
					motionControlSystem->setRotationTunings(kp,ki,kd);
					motionControlSystem->orderRotation(2*PI/3);
					Delay(3000);
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
					motionControlSystem->orderRotation(0);
					Delay(3000);
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
				}
			}
			else if(!strcmp("kd",order))//Test d'une valeur de Kd
			{
				float kp, ki, kd;
				serial.printfln("kd ?");
				if (translation)
				{
					motionControlSystem->getTranslationTunings(kp,ki,kd);
					serial.read(kd);
					serial.printfln("kd_trans = %f", kd);
					motionControlSystem->setTranslationTunings(kp,ki,kd);
					motionControlSystem->orderTranslation(300);
					Delay(3000);
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
					motionControlSystem->orderTranslation(-300);
					Delay(3000);
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
				}
				else
				{
					motionControlSystem->getRotationTunings(kp,ki,kd);
					serial.read(kd);
					serial.printfln("kd_rot = %f", kd);
					motionControlSystem->setRotationTunings(kp,ki,kd);
					motionControlSystem->orderRotation(2*PI/3);
					Delay(3000);
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
					motionControlSystem->orderRotation(0);
					Delay(3000);
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
				}
			}
			else if(!strcmp("toggle",order))//Bascule entre le réglage d'asserv en translation et en rotation
			{
				translation = !translation;
				if(translation)
					serial.printfln("reglage de la transation");
				else
					serial.printfln("reglage de la rotation");
			}
			else if(!strcmp("display",order))
			{
				float kp_trans, ki_trans, kd_trans,
					kp_rot, ki_rot, kd_rot;
				motionControlSystem->getTranslationTunings(kp_trans,ki_trans,kd_trans);
				motionControlSystem->getRotationTunings(kp_rot,ki_rot,kd_rot);
				serial.printfln("translation : kp= %f  ; kd= %f", kp_trans, kd_trans);
				serial.printfln("rotation :    kp= %f  ; kd= %f", kp_rot, kd_rot);
				serial.printfln("balance = %f", motionControlSystem->getBalance());
			}
			else if(!strcmp("balance",order))
			{
				float balance;
				serial.printfln("nouvelle balance ?");
				serial.read(balance);
				motionControlSystem->setBalance(balance);
				serial.printfln("balance = %f", motionControlSystem->getBalance());
			}
			else if(!strcmp("setPWMt",order))
			{
				uint8_t pwm;
				serial.printfln("nouveau pwm max en translation ?");
				serial.read(pwm);
				motionControlSystem->setMaxPWMtranslation(pwm);
				serial.printfln("nouveau pwm max en translation = %d", motionControlSystem->getMaxPWMtranslation());
			}
			else if(!strcmp("setPWMr",order))
			{
				uint8_t pwm;
				serial.printfln("nouveau pwm max en rotation ?");
				serial.read(pwm);
				motionControlSystem->setMaxPWMrotation(pwm);
				serial.printfln("nouveau pwm max en rotation = %d", motionControlSystem->getMaxPWMrotation());
			}
			else if(!strcmp("kpt",order))
			{
				float kp, ki, kd;
				serial.printfln("kp_trans ?");
				motionControlSystem->getTranslationTunings(kp,ki,kd);
				serial.read(kp);
				motionControlSystem->setTranslationTunings(kp,ki,kd);
				serial.printfln("kp_trans = %f", kp);
			}
			else if(!strcmp("kdt",order))
			{
				float kp, ki, kd;
				serial.printfln("kd_trans ?");
				motionControlSystem->getTranslationTunings(kp,ki,kd);
				serial.read(kd);
				motionControlSystem->setTranslationTunings(kp,ki,kd);
				serial.printfln("kd_trans = %f", kd);
			}
			else if(!strcmp("kpr",order))
			{
				float kp, ki, kd;
				serial.printfln("kp_rot ?");
				motionControlSystem->getRotationTunings(kp,ki,kd);
				serial.read(kp);
				motionControlSystem->setRotationTunings(kp,ki,kd);
				serial.printfln("kp_rot = %f", kp);
			}
			else if(!strcmp("kdr",order))
			{
				float kp, ki, kd;
				serial.printfln("kd_rot ?");
				motionControlSystem->getRotationTunings(kp,ki,kd);
				serial.read(kd);
				motionControlSystem->setRotationTunings(kp,ki,kd);
				serial.printfln("kd_rot = %f", kd);
			}
			else if(!strcmp("trackOXY",order))
			{
				motionControlSystem->printTrackingOXY();
			}
			else if(!strcmp("trackAll",order))
			{
				motionControlSystem->printTrackingAll();
			}
			else if(!strcmp("trackLocomotion",order))
			{
				motionControlSystem->printTrackingLocomotion();
			}
			else if(!strcmp("trackSerie",order))
			{
				motionControlSystem->printTrackingSerie();
			}




/*			 ___________
 * 		   *|           |*
 *		   *|ACTIONNEURS|*
 *		   *|___________|*
 */
			else if(!strcmp("ss", order))
		{
			uint16_t a17 = 0x19;
			serial.read(a17);
			actuatorsMgr->setArmSpeed(a17);
		}
			else if(!strcmp("obd",order))
		{
			actuatorsMgr->obd();//		Ouvrir bras droit
		}
		else if(!strcmp("fbd",order))
		{
			actuatorsMgr->fbd();//		Fermer bras droit
		}
		else if(!strcmp("obg",order))
		{
			actuatorsMgr->obg();//		Ouvrir bras gauche
		}
		else if(!strcmp("fbg",order))
		{
			actuatorsMgr->fbg();//		Fermer bras gauche
		}
		else if(!strcmp("obdl",order))
		{
			actuatorsMgr->obdl();//		Ouvrir bras droit lentement
		}
		else if(!strcmp("fbdl",order))
		{
			actuatorsMgr->fbdl();//		Fermer bras droit lentement
		}
		else if(!strcmp("obgl",order))
		{
			actuatorsMgr->obgl();//		Ouvrir bras gauche lentement
		}
		else if(!strcmp("fbgl",order))
		{
			actuatorsMgr->fbgl();//		Fermer bras gauche lentement
		}
		else if(!strcmp("omd",order))
		{
			actuatorsMgr->omd();//		Ouvrir machoire droite
		}
		else if(!strcmp("fmd",order))
		{
			actuatorsMgr->fmd();//		Fermer machoire droite
		}
		else if(!strcmp("omg",order))
		{
			actuatorsMgr->omg();//		Ouvrir machoire gauche
		}
		else if(!strcmp("fmg",order))
		{
			actuatorsMgr->fmg();//		Fermer machoire gauche
		}
		else if(!strcmp("om_",order))
		{//								Ouvrir les deux machoires [TEST]
			actuatorsMgr->omg();
			actuatorsMgr->omd();
		}
		else if(!strcmp("om",order))
		{//								Ouvrir les deux machoires
			actuatorsMgr->omg();
			actuatorsMgr->omd();
		}
		else if(!strcmp("fm",order))
		{//								Fermer les deux machoires
			actuatorsMgr->fmg();
			actuatorsMgr->fmd();
		}
		else if(!strcmp("ah",order))
		{
			actuatorsMgr->ah();//		Ascenseur en position haute
		}
		else if(!strcmp("ab",order))
		{
			actuatorsMgr->ab();//		Ascenseur en position basse
		}
		else if(!strcmp("as",order))
		{
			actuatorsMgr->as();//		Ascenseur au niveau du sol
		}
		else if(!strcmp("ae",order))
		{
			actuatorsMgr->ae();//		Ascenseur au niveau de l'estrade
		}
		else if(!strcmp("ogd",order))
		{
			actuatorsMgr->ogd();//		Ouvrir guide droit
		}
		else if(!strcmp("fgd",order))
		{
			actuatorsMgr->fgd();//		Fermer guide droit
		}
		else if(!strcmp("gdi",order))
		{
			actuatorsMgr->gdi();//		Guide droit en position intermédiaire
		}
		else if(!strcmp("ogg",order))
		{
			actuatorsMgr->ogg();//		Ouvrir guide gauche
		}
		else if(!strcmp("fgg",order))
		{
			actuatorsMgr->fgg();//		Fermer guide gauche
		}
		else if(!strcmp("ggi",order))
		{
			actuatorsMgr->ggi();//		Guide gauche en position intermédiaire
		}
		else if(!strcmp("go",order))
		{//								Ouvrir le guide
			actuatorsMgr->ogg();
			//Delay(10);
			actuatorsMgr->ogd();
		}
		else if(!strcmp("gf",order))
		{//								Fermer le guide
			actuatorsMgr->fgg();
			//Delay(10);
			actuatorsMgr->fgd();
		}
		else if(!strcmp("gi",order))
		{//								Guide en position intermédiaire
			actuatorsMgr->ggi();
			//Delay(10);
			actuatorsMgr->gdi();
		}
		else if(!strcmp("ptd",order))
		{
			actuatorsMgr->ptd();//		Poser tapis droit
		}
		else if(!strcmp("rtd",order))
		{
			actuatorsMgr->rtd();//		Ranger tapis droit
		}
		else if(!strcmp("ptg",order))
		{
			actuatorsMgr->ptg();//		Poser tapis gauche
		}
		else if(!strcmp("rtg",order))
		{
			actuatorsMgr->rtg();//		Ranger tapis gauche
		}
		else if(!strcmp("cdh",order))
		{
			actuatorsMgr->cdh();//		Clap droit en haut
		}
		else if(!strcmp("cdm",order))
		{
			actuatorsMgr->cdm();//		Clap droit au milieu
		}
		else if(!strcmp("cdb",order))
		{
			actuatorsMgr->cdb();//		Clap droit en bas
		}
		else if(!strcmp("cgh",order))
		{
			actuatorsMgr->cgh();//		Clap gauche en haut
		}
		else if(!strcmp("cgm",order))
		{
			actuatorsMgr->cgm();//		Clap gauche au milieu
		}
		else if(!strcmp("cgb",order))
		{
			actuatorsMgr->cgb();//		Clap gauche en bas
		} else if(!strcmp("bordel",order)){
			float dummy;
				// Test des actionneurs //
				Delay(5000);
				actuatorsMgr->obd();//		Ouvrir bras droit
				serial.printfln("obd");
				serial.read(dummy);
				actuatorsMgr->fbd();//		Fermer bras droit
				serial.printfln("fbd");
				serial.read(dummy);
				actuatorsMgr->obg();//		Ouvrir bras gauche
				serial.printfln("obg");
				serial.read(dummy);
				actuatorsMgr->fbg();//		Fermer bras gauche
				serial.printfln("fbg");
				serial.read(dummy);
				actuatorsMgr->obdl();//		Ouvrir bras droit lentement
				serial.printfln("obdl");
				serial.read(dummy);
				actuatorsMgr->fbdl();//		Fermer bras droit lentement
				serial.printfln("obd");
				serial.printfln("fbdl");
				serial.read(dummy);
				actuatorsMgr->obgl();//		Ouvrir bras gauche lentement
				serial.printfln("obgl");
				serial.read(dummy);
				actuatorsMgr->fbgl();//		Fermer bras gauche lentement
				serial.printfln("fbgl");
				serial.read(dummy);
				actuatorsMgr->omd();//		Ouvrir machoire droite
				serial.printfln("omd");
				serial.read(dummy);
				actuatorsMgr->fmd();//		Fermer machoire droite
				serial.printfln("fmd");
				serial.read(dummy);
				actuatorsMgr->omg();//		Ouvrir machoire gauche
				serial.printfln("omg");
				serial.read(dummy);
				actuatorsMgr->fmg();//		Fermer machoire gauche
				serial.printfln("fmg");
				serial.read(dummy);
				actuatorsMgr->omg();
				actuatorsMgr->omd();
				serial.printfln("omg plus omd");
				serial.read(dummy);
				actuatorsMgr->fmg();
				actuatorsMgr->fmd();
				serial.printfln("fmg plus fmd");
				serial.read(dummy);
				actuatorsMgr->ah();//		Ascenseur en position haute
				serial.printfln("ah");
				serial.read(dummy);
				actuatorsMgr->ab();//		Ascenseur en position basse
				serial.printfln("ab");
				serial.read(dummy);
				actuatorsMgr->as();//		Ascenseur au niveau du sol
				serial.printfln("as");
				serial.read(dummy);
				actuatorsMgr->ae();//		Ascenseur au niveau de l'estrade
				serial.printfln("ae");
				serial.read(dummy);
				actuatorsMgr->omg();
				actuatorsMgr->omd();
				serial.read(dummy);
				actuatorsMgr->ogd();//		Ouvrir guide droit
				serial.printfln("ogd");
				serial.read(dummy);
				actuatorsMgr->fgd();//		Fermer guide droit
				serial.printfln("fgd");
				serial.read(dummy);
				actuatorsMgr->gdi();//		Guide droit en position intermédiaire
				serial.printfln("gdi");
				serial.read(dummy);
				actuatorsMgr->ogg();//		Ouvrir guide gauche
				serial.printfln("ogg");
				serial.read(dummy);
				actuatorsMgr->fgg();//		Fermer guide gauche
				serial.printfln("fgg");
				serial.read(dummy);
				actuatorsMgr->ggi();//		Guide gauche en position intermédiaire
				serial.printfln("ggi");
				serial.read(dummy);
				actuatorsMgr->ogg();
				actuatorsMgr->ogd();
				serial.printfln("ogg plus ogd");
				serial.read(dummy);
				actuatorsMgr->fgg();
				actuatorsMgr->fgd();
				serial.printfln("fgg plus fgd");
				serial.read(dummy);
				actuatorsMgr->ggi();
				actuatorsMgr->gdi();
				serial.printfln("ggi plus gdi");
				serial.read(dummy);
				actuatorsMgr->ptd();//		Poser tapis droit
				serial.printfln("ptd");
				serial.read(dummy);
				actuatorsMgr->rtd();//		Ranger tapis droit
				serial.printfln("rtd");
				serial.read(dummy);
				actuatorsMgr->ptg();//		Poser tapis gauche
				serial.printfln("ptg");
				serial.read(dummy);
				actuatorsMgr->rtg();//		Ranger tapis gauche
				serial.printfln("rtg");
				serial.read(dummy);
				actuatorsMgr->cdh();//		Clap droit en haut
				serial.printfln("cdh");
				serial.read(dummy);
				actuatorsMgr->cdm();//		Clap droit au milieu
				serial.printfln("cdm");
				serial.read(dummy);
				actuatorsMgr->cdb();//		Clap droit en bas
				serial.printfln("cdb");
				serial.read(dummy);
				actuatorsMgr->cgh();//		Clap gauche en haut
				serial.printfln("cgh");
				serial.read(dummy);
				actuatorsMgr->cgm();//		Clap gauche au milieu
				serial.printfln("cgm");
				serial.read(dummy);
				actuatorsMgr->cgb();//		Clap gauche en bas
				serial.printfln("cgb");
				Delay(5000);
			}
		}
		else if(tailleBuffer == RX_BUFFER_SIZE - 1)
		{
			serial.printfln("CRITICAL OVERFLOW !");
			motionControlSystem->enableTranslationControl(false);
			motionControlSystem->enableRotationControl(false);
			actuatorsMgr->cdm();
			while(true)
				;
		}
	}
}

extern "C" {
//Interruption overflow TIMER4
void TIM4_IRQHandler(void) { //2kHz = 0.0005s = 0.5ms
	volatile static uint32_t i = 0, j = 0;
	static MotionControlSystem* motionControlSystem = &MotionControlSystem::Instance();
	static ActuatorsMgr* actuatorsMgr = &ActuatorsMgr::Instance();

	if (TIM_GetITStatus(TIM4, TIM_IT_Update) != RESET) {
		//Remise à 0 manuelle du flag d'interruption nécessaire
		TIM_ClearITPendingBit(TIM4, TIM_IT_Update);

		//Asservissement et mise à jour de la position
		motionControlSystem->control();
		motionControlSystem->updatePosition();

		if (i >= 10) { //5ms
			//Gestion de l'arrêt
			motionControlSystem->manageStop();
			actuatorsMgr->refreshElevatorState();
			i = 0;
		}

//		if(j >= 200){ //100ms
//			motionControlSystem->track();
//			j=0;
//		}

		i++;
		j++;
	}
}


void EXTI4_IRQHandler(void)
{
	static SensorMgr* sensorMgr = &SensorMgr::Instance();

	//Interruption de l'ultrason Avant Gauche
	if (EXTI_GetITStatus(EXTI_Line4) != RESET)
	{
		sensorMgr->leftFrontUSInterrupt();

		/* Clear interrupt flag */
		EXTI_ClearITPendingBit(EXTI_Line4);
	}
}


void EXTI1_IRQHandler(void)
{
	static SensorMgr* sensorMgr = &SensorMgr::Instance();

	//Interruption de l'ultrason Arrière Gauche
	if (EXTI_GetITStatus(EXTI_Line1) != RESET)
	{
		sensorMgr->leftBackUSInterrupt();

		/* Clear interrupt flag */
		EXTI_ClearITPendingBit(EXTI_Line1);
	}
}


void EXTI9_5_IRQHandler(void)
{
	static SensorMgr* sensorMgr = &SensorMgr::Instance();

	//Interruptions de l'ultrason Avant Droit
    if (EXTI_GetITStatus(EXTI_Line6) != RESET) {
        sensorMgr->rightFrontUSInterrupt();

        /* Clear interrupt flag */
        EXTI_ClearITPendingBit(EXTI_Line6);
    }

    //Interruptions de l'ultrason Arrière Droit
	if (EXTI_GetITStatus(EXTI_Line7) != RESET) {
		sensorMgr->rightBackUSInterrupt();

		/* Clear interrupt flag */
		EXTI_ClearITPendingBit(EXTI_Line7);
	}
}

}
