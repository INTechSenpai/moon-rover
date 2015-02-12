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
	motionControlSystem->init();
	ActuatorsMgr actuatorsMgr;

	float
	kp_trans = 0.2,
	kd_trans = 35,
	kp_rot = 0.2,
	kd_rot = 50;
	bool translation = true;//permet de basculer entre les réglages Kp de translation et de rotation

	while(1)
	{
		if (serial.available()) {
			char order[200];
			serial.read(order);

			if(!strcmp("?",order))
			{
				serial.printfln("complete");
			}
			else if(!strcmp("!",order))
			{
				serial.printfln("%lf", 3.15402151024021654);
			}
			else if(!strcmp("oxy",order))
			{
				serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
				serial.printfln("%f", motionControlSystem->getAngleRadian());
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
				serial.printfln("kp?");
				if (translation)
				{
					serial.read(kp_trans);
					serial.printfln("kp_trans = %f", kp_trans);
					motionControlSystem->setTranslationTunings(kp_trans,0,kd_trans);
					motionControlSystem->setRotationTunings(kp_rot,0,kd_rot);
					motionControlSystem->orderTranslation(100);
					Delay(3000);
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
					motionControlSystem->orderTranslation(-100);
					Delay(3000);
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
				}
				else
				{
					serial.read(kp_rot);
					serial.printfln("kp_rot = %f", kp_rot);
					motionControlSystem->setTranslationTunings(kp_trans,0,kd_trans);
					motionControlSystem->setRotationTunings(kp_rot,0,kd_rot);
					motionControlSystem->orderRotation(PI/2);
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
				serial.printfln("kd ?");
				if (translation)
				{
					serial.read(kd_trans);
					serial.printfln("kd_trans = %f", kd_trans);
					motionControlSystem->setTranslationTunings(kp_trans,0,kd_trans);
					motionControlSystem->setRotationTunings(kp_rot,0,kd_rot);
					motionControlSystem->orderTranslation(100);
					Delay(3000);
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
					motionControlSystem->orderTranslation(-100);
					Delay(3000);
					serial.printfln("%f\r\n%f", motionControlSystem->getX(), motionControlSystem->getY());
					serial.printfln("%f", motionControlSystem->getAngleRadian());
				}
				else
				{
					serial.read(kd_rot);
					serial.printfln("kd_rot = %f", kd_rot);
					motionControlSystem->setTranslationTunings(kp_trans,0,kd_trans);
					motionControlSystem->setRotationTunings(kp_rot,0,kd_rot);
					motionControlSystem->orderRotation(PI/2);
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
				motionControlSystem->setTranslationTunings(kp_trans,0,kd_trans);
				motionControlSystem->setRotationTunings(kp_rot,0,kd_rot);
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
				serial.printfln("kp_trans ?");
				serial.read(kp_trans);
				motionControlSystem->setTranslationTunings(kp_trans,0,kd_trans);
				serial.printfln("kp_trans = %f", kp_trans);
			}
			else if(!strcmp("kdt",order))
			{
				serial.printfln("kd_trans ?");
				serial.read(kd_trans);
				motionControlSystem->setTranslationTunings(kp_trans,0,kd_trans);
				serial.printfln("kd_trans = %f", kd_trans);
			}
			else if(!strcmp("kpr",order))
			{
				serial.printfln("kp_rot ?");
				serial.read(kp_rot);
				motionControlSystem->setRotationTunings(kp_rot,0,kd_rot);
				serial.printfln("kp_rot = %f", kp_rot);
			}
			else if(!strcmp("kdr",order))
			{
				serial.printfln("kd_rot ?");
				serial.read(kd_rot);
				motionControlSystem->setRotationTunings(kp_rot,0,kd_rot);
				serial.printfln("kd_rot = %f", kd_rot);
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
	__IO static uint32_t i = 0;
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

		i++;
	}
}
}
