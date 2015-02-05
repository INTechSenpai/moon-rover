#include "Uart.hpp"
#include "MotionControlSystem.h"
#include "delay.h"
#include "Motor.h"

	Uart<1> serial;

int main(void)
{
	Delay_Init();

	serial.init(115200);
	MotionControlSystem* motionControlSystem = &MotionControlSystem::Instance();
	motionControlSystem->init();

	while(1)
	{
		if (serial.available()) {
			char order[200];
			serial.read(order);

			if(!strcmp("?",order))
			{
				serial.printfln("asservissement");
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
