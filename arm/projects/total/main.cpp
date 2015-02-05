#include "Uart.hpp"
#include "MotionControlSystem.h"
#include "delay.h"
#include "Motor.h"
#include "include/ActuatorsMgr.hpp"

	Uart<1> serial;
	Uart<2> serial_ax;

int main(void)
{
	Delay_Init();

	serial.init(115200);
	MotionControlSystem* motionControlSystem = &MotionControlSystem::Instance();
	motionControlSystem->init();

	serial_ax.init(9600);
	ActuatorsMgr actuatorsMgr;

	while(1)
	{
		if (serial.available()) {
			char order[200];
			serial.read(order);

			if(!strcmp("?",order))
			{
				serial.printfln("3");
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
			else if(!strcmp("!",order))
			{
				//actuatorsMgr.monterBras();
				//serial.printfln("%lf", 3.15402151024021654);
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
				serial.printfln("On tourne a %d radian", angle);
				motionControlSystem->orderRotation(angle);
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
			else if(!strcmp("ml",order))
			{
				serial.printfln("On bouge la codeuse gauche de 100 ticks");
				motionControlSystem->moveLeftEncoder(100);
			}
			else if(!strcmp("mr",order))
			{
				serial.printfln("On bouge la codeuse droite de 100 ticks");
				motionControlSystem->moveRightEncoder(100);
			}
			else if(!strcmp("m",order))
			{
				serial.printfln("On bouge les deux codeuses de 100 ticks");
				motionControlSystem->moveLeftEncoder(100);
				motionControlSystem->moveRightEncoder(100);
			}
			else if(!strcmp("mle",order))
			{
				int32_t value;
				serial.read(value);
				serial.printfln("On bouge la codeuse gauche de %d ticks", value);
				motionControlSystem->moveLeftEncoder(value);
			}
			else if(!strcmp("mre",order))
			{
				int32_t value;
				serial.read(value);
				serial.printfln("On bouge la codeuse droite de %d ticks", value);
				motionControlSystem->moveRightEncoder(value);
			}
			else if(!strcmp("me",order))
			{
				int32_t value;
				serial.read(value);
				serial.printfln("On bouge les deux codeuses de %d ticks", value);
				motionControlSystem->moveLeftEncoder(value);
				motionControlSystem->moveRightEncoder(value);
			}
			else if(!strcmp("g",order))
			{
				serial.printfln("Objectif en translation: %d   actuel : %d", motionControlSystem->getTranslationGoal(), motionControlSystem->currentDistance);
				serial.printfln("Objectif en rotation : %d    actuel : %d", motionControlSystem->getRotationGoal(), motionControlSystem->currentAngle);

			}
			else if(!strcmp("kp",order))
			{
				float kp;
				serial.printfln("kp?");
				serial.read(kp);
				serial.printfln("kp = %f", kp);
				bool translation = false;
				if (translation)
				{
					motionControlSystem->setTranslationTunings(kp,0,0);
					motionControlSystem->orderTranslation(-1000);
				}
				else
				{
					motionControlSystem->setRotationTunings(kp,0,0);
					motionControlSystem->orderRotation(PI);
				}
			}
			else if(!strcmp("kd",order))
			{
				float kd;
				serial.printfln("kd?");
				serial.read(kd);
				serial.printfln("kd = %f", kd);
				bool translation = true;
				if (translation)
				{
					motionControlSystem->setTranslationTunings(1.5,0,kd);
					motionControlSystem->orderTranslation(-1000);
				}
				else
				{
					motionControlSystem->setRotationTunings(1,0,kd);
					motionControlSystem->orderRotation(PI);
				}
			}
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
			else if(!strcmp("ptd",order))
			{
				actuatorsMgr.ptd();
			}
			else if(!strcmp("rtd",order))
			{
				actuatorsMgr.rtd();
			}
			else if(!strcmp("ptg",order))
			{
				actuatorsMgr.ptg();
			}
			else if(!strcmp("rtg",order))
			{
				actuatorsMgr.rtg();
			}
			else if(!strcmp("cdh",order))
			{
				actuatorsMgr.cdh();
			}
			else if(!strcmp("cdm",order))
			{
				actuatorsMgr.cdm();
			}
			else if(!strcmp("cdb",order))
			{
				actuatorsMgr.cdb();
			}
			else if(!strcmp("cgh",order))
			{
				actuatorsMgr.cgh();
			}
			else if(!strcmp("cgm",order))
			{
				actuatorsMgr.cgm();
			}
			else if(!strcmp("cgb",order))
			{
				actuatorsMgr.cgb();
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

		if (i >= 60000) { //30s, pour le debug
			if (int err = motionControlSystem->manageStop() )
			{
				if (err != 3)
				{
					serial.printfln("cas d'arret n° %d", err);
				}
			}
			i = 0;
		}

		i++;
	}
}
}
