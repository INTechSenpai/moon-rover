#include "Uart.hpp"
#include "MotionControlSystem.h"
#include "delay.h"
#include "include/ActuatorsMgr.hpp"

int main(void)
{
	Delay_Init();
	Uart<1> serial;
	Uart<2> serial_ax;
	serial.init(115200);
	serial_ax.init(9600);

	MotionControlSystem* motionControlSystem = &MotionControlSystem::Instance();
	motionControlSystem->init(100, 100);
	ActuatorsMgr actuatorsMgr;

	bool translation = true;//permet de basculer entre les réglages de cte d'asserv en translation et en rotation

	while(1)
	{
		if (serial.available()) {
			char order[200];
			serial.read(order);

			serial.printfln("_");//Acquittement

			if(!strcmp("?",order))
			{
				serial.printfln("0");
			}
			else if(!strcmp("!",order))
			{
				serial.printfln("Abwabwa.");
			}
			else if(!strcmp("oxy",order))
			{
				serial.printfln("x=%f\r\ny=%f", motionControlSystem->getX(), motionControlSystem->getY());
				serial.printfln("o=%f", motionControlSystem->getAngleRadian());
			}
			else if(!strcmp("?xyo",order))
			{
				serial.printfln("%f", motionControlSystem->getX());
				serial.printfln("%f", motionControlSystem->getY());
				serial.printfln("%f", motionControlSystem->getAngleRadian());
			}
			else if(!strcmp("us_av",order))
			{
				serial.printfln("%d", 3000);//Distance mesurée par l'ultrason avant gauche, en mm
				serial.printfln("%d", 3000);//Distance mesurée par l'ultrason avant droit, en mm
			}
			else if(!strcmp("us_ar",order))
			{
				serial.printfln("%d", 3000);//Distance mesurée par l'ultrason arrière gauche, en mm
				serial.printfln("%d", 3000);//Distance mesurée par l'ultrason arrière droit, en mm
			}
			else if(!strcmp("ct0",order))
			{
				motionControlSystem->enableTranslationControl(false);
			}
			else if(!strcmp("ct1",order))
			{
				motionControlSystem->enableTranslationControl(true);
			}
			else if(!strcmp("cr0",order))
			{
				motionControlSystem->enableRotationControl(false);
			}
			else if(!strcmp("cr1",order))
			{
				motionControlSystem->enableRotationControl(true);
			}
			else if(!strcmp("cx",order))
			{
				float x;
				serial.read(x);
				motionControlSystem->setX(x);
			}
			else if(!strcmp("cy",order))
			{
				float y;
				serial.read(y);
				motionControlSystem->setY(y);
			}
			else if(!strcmp("co",order))
			{
				float o;
				serial.read(o);
				motionControlSystem->setOriginalAngle(o);
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
			else if(!strcmp("d", order))
			{
				int deplacement = 0;
				serial.read(deplacement);
				serial.printfln("On avance de %d mm", deplacement);
				motionControlSystem->orderTranslation(deplacement);
			}
			else if(!strcmp("t", order))
			{
				float angle = motionControlSystem->getAngleRadian();
				serial.read(angle);
				serial.printfln("On tourne a %f radian", angle);
				motionControlSystem->orderRotation(angle);
			}
			else if (!strcmp("broad",order))
			{
				actuatorsMgr.broad();
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
			else if(!strcmp("track",order))
			{
				motionControlSystem->printTracking();
			}
			else if(!strcmp("clear",order))
			{
				motionControlSystem->clearTracking();
				serial.printfln("Tracking array cleared");
			}



			/* ACTIONNEURS */
			else if(!strcmp("obd",order))
			{
				actuatorsMgr.obd();
			}
			else if(!strcmp("fbd",order))
			{
				actuatorsMgr.fbd();
			}
			else if(!strcmp("obg",order))
			{
				actuatorsMgr.obg();
			}
			else if(!strcmp("fbg",order))
			{
				actuatorsMgr.fbg();
			}
			else if(!strcmp("obdl",order))
			{
				actuatorsMgr.obdl();
			}
			else if(!strcmp("fbdl",order))
			{
				actuatorsMgr.fbdl();
			}
			else if(!strcmp("obgl",order))
			{
				actuatorsMgr.obgl();
			}
			else if(!strcmp("fbgl",order))
			{
				actuatorsMgr.fbgl();
			}
			else if(!strcmp("omd",order))
			{
				actuatorsMgr.omd();
			}
			else if(!strcmp("fmd",order))
			{
				actuatorsMgr.fmd();
			}
			else if(!strcmp("omg",order))
			{
				actuatorsMgr.omg();
			}
			else if(!strcmp("fmg",order))
			{
				actuatorsMgr.fmg();
			}
			else if(!strcmp("om",order))
			{
				actuatorsMgr.omg();
				actuatorsMgr.omd();
			}
			else if(!strcmp("fm",order))
			{
				actuatorsMgr.fmg();
				actuatorsMgr.fmd();
			}
			else if(!strcmp("ah",order))
			{
				actuatorsMgr.ah();
			}
			else if(!strcmp("ab",order))
			{
				actuatorsMgr.ab();
			}
			else if(!strcmp("as",order))
			{
				actuatorsMgr.as();
			}
			else if(!strcmp("ae",order))
			{
				actuatorsMgr.ae();
			}
			else if(!strcmp("ogd",order))
			{
				actuatorsMgr.ogd();
			}
			else if(!strcmp("fgd",order))
			{
				actuatorsMgr.fgd();
			}
			else if(!strcmp("gdi",order))
			{
				actuatorsMgr.gdi();
			}
			else if(!strcmp("ogg",order))
			{
				actuatorsMgr.ogg();
			}
			else if(!strcmp("fgg",order))
			{
				actuatorsMgr.fgg();
			}
			else if(!strcmp("ggi",order))
			{
				actuatorsMgr.ggi();
			}
			else if(!strcmp("go",order))
			{
				actuatorsMgr.ogg();
				actuatorsMgr.ogd();
			}
			else if(!strcmp("gf",order))
			{
				actuatorsMgr.fgg();
				actuatorsMgr.fgd();
			}
			else if(!strcmp("gi",order))
			{
				actuatorsMgr.ggi();
				actuatorsMgr.gdi();
			}
		}
	}
}

extern "C" {
//Interruption overflow TIMER4
void TIM4_IRQHandler(void) { //2kHz = 0.0005s = 0.5ms
	__IO static uint32_t i = 0, j = 0;
	static MotionControlSystem* motionControlSystem = &MotionControlSystem::Instance();

	if (TIM_GetITStatus(TIM4, TIM_IT_Update) != RESET) {
		//Remise à 0 manuelle du flag d'interruption nécessaire
		TIM_ClearITPendingBit(TIM4, TIM_IT_Update);

		//Asservissement et mise à jour de la position
		motionControlSystem->control();
		motionControlSystem->updatePosition();

		if (i >= 100) { //50ms
			motionControlSystem->manageStop();
			i = 0;
		}

		if(j >= 200){ //100ms
			motionControlSystem->track();
			j=0;
		}

		i++;
		j++;
	}
}
}
