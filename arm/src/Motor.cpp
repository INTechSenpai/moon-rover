/**
 * Moteur.cpp
 *
 * Classe de gestion d'un moteur (PWM, direction...)
 *
 * Récapitulatif pins utilisées pour contrôler les deux moteurs :
 *
 * Gauche :
 * 	-pins de sens : PD10
 * 	-pin de pwm : PC6
 * Droit :
 * 	-pins de sens : PD12
 * 	-pin de pwm : PC7
 *
 */

#include "Motor.h"

Motor::Motor(Side s) : side(s)
{
	setDirection(MOTOR_FORWARD);
	initPWM();
}

void Motor::initPWM(){
}

void Motor::run(int16_t pwm){
}

void Motor::setDirection(Direction dir) {
}
