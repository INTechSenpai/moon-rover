#include "MotionControlSystem.h"


MotionControlSystem::MotionControlSystem() :
		leftMotor(Side::LEFT), rightMotor(Side::RIGHT), translationControlled(
				true), rotationControlled(true), translationPID(
				&currentDistance, &pwmTranslation, &translationSetpoint), rotationPID(
				&currentAngle, &pwmRotation, &rotationSetpoint),  leftEncoderFake(0), rightEncoderFake(0),
				originalAngle(0.0), rotationSetpoint(0), translationSetpoint(0), x(0.0), y(0.0), moving(
				false), movingForPositioningServo(false) {

}

void MotionControlSystem::init() {
	/**
	 * Initialisation moteurs et encodeurs
	 */

	Motor::initPWM();
	Counter();

	/**
	 * R�glage des PID
	 */

	translationPID.setControllerDirection(PidDirection::DIRECT);
	translationPID.setTunings(1.5, 0, 0.);
	rotationPID.setControllerDirection(PidDirection::DIRECT);
	rotationPID.setTunings(1, 0, 0.);


	/**
	 * Initialisation de la boucle d'asservissement (TIMER 4)
	 */

	NVIC_InitTypeDef NVIC_InitStructure;
	//Configuration et activation de l'interruption
	NVIC_InitStructure.NVIC_IRQChannel = TIM4_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
	NVIC_Init(&NVIC_InitStructure);

	//Activation de l'horloge du TIMER 4
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM4, ENABLE);
	//Configuration du timer
	//TIM4CLK = HCLK / 2 = SystemCoreClock /2 = 168MHz/2 = 84MHz
	TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
	TIM_TimeBaseStructure.TIM_Prescaler = 84 - 1; // 84 MHz Clock down to 1 MHz
	TIM_TimeBaseStructure.TIM_Period = 500 - 1; // 1 MHz down to 2 KHz : fr�quence d'asservissement de 2kHz
	TIM_TimeBaseStructure.TIM_ClockDivision = 0;
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;
	TIM_TimeBaseInit(TIM4, &TIM_TimeBaseStructure);

	TIM_ITConfig(TIM4, TIM_IT_Update, ENABLE);

	//Active l'asservissement
	enable(true);
}

int MotionControlSystem::getPWMTranslation() {
	return pwmTranslation;
}

int MotionControlSystem::getPWMRotation() {
	return pwmRotation;
}

int MotionControlSystem::getTranslationGoal() {
	return translationSetpoint;
}

int MotionControlSystem::getRotationGoal() {
	return rotationSetpoint;
}

int MotionControlSystem::getLeftEncoder() {
	return Counter::getLeftValue();
}

int MotionControlSystem::getRightEncoder() {
	return Counter::getRightValue();
}

void MotionControlSystem::moveLeftEncoder(int32_t value)
{
	leftEncoderFake += value;
}
void MotionControlSystem::moveRightEncoder(int32_t value)
{
	rightEncoderFake += value;
}

void MotionControlSystem::enable(bool enable) {
	if (enable) {
		TIM_Cmd(TIM4, ENABLE); //Active la boucle d'asservissement
	} else {
		TIM_Cmd(TIM4, DISABLE); //D�sactive la boucle d'asservissement
		stop();
	}
}

void MotionControlSystem::enableTranslationControl(bool enabled) {
	translationControlled = enabled;
}
void MotionControlSystem::enableRotationControl(bool enabled) {
	rotationControlled = enabled;
}

void MotionControlSystem::control() {

	int32_t leftTicks = Counter::getLeftValue();
	int32_t rightTicks = Counter::getRightValue();
//	int32_t leftTicks = leftEncoderFake;
//	int32_t rightTicks = rightEncoderFake;

	// currentDistance = leftTicks + rightTicks; // Il manque bel et bien un /2
	currentDistance = (leftTicks + rightTicks) / 2;
	currentAngle = leftTicks - rightTicks;

	if (translationControlled) {
		translationPID.compute();
	} else
		pwmTranslation = 0;

	if (rotationControlled) {
		rotationPID.compute();
	} else
		pwmRotation = 0;

	applyControl();
}

bool MotionControlSystem::isPhysicallyStopped() {
	return (translationPID.getDerivativeError() == 0)
			&& (rotationPID.getDerivativeError() == 0);
}

int MotionControlSystem::manageStop() {
	static uint32_t time = 0;

	if (isPhysicallyStopped() && (moving || movingForPositioningServo)) {

		if (time == 0) { //D�but du timer
			time = Millis();
		} else {
			if ((Millis() - time) >= 500) { //Si arr�t� plus de 500ms
				if (ABS(translationPID.getError()) <= 50	&& ABS(rotationPID.getError()) <= 50) { //Stopp� pour cause de fin de mouvement, donc �a normalement c'est pas trop mal
					time = 0;
					if(moving) {
						stop();
						return 1;
					} else {
						stop();
						return 3;
					}
				}else if (pwmRotation >= 60 || pwmTranslation >= 60) { //Stopp� pour blocage
					stop();
					time = 0;
					return 2;
				}

				stop(); //Arr�t
				time = 0;
			}
		}
	} else {
		time = 0;
	}
	return 0;
}

void MotionControlSystem::updatePosition() {
	static __IO int32_t lastDistance = 0;

	float deltaDistanceMm = (currentDistance - lastDistance) * TICK_TO_MM;
	lastDistance = currentDistance;

	x += (deltaDistanceMm * cos(getAngleRadian()));
	y += (deltaDistanceMm * sin(getAngleRadian()));
}

int32_t MotionControlSystem::optimumAngle(int32_t fromAngle, int32_t toAngle) {
	while (toAngle > fromAngle + PI_TIC)
		toAngle -= 2 * PI_TIC;
	while (toAngle <= fromAngle - PI_TIC)
		toAngle += 2 * PI_TIC;
	return toAngle;
}

void MotionControlSystem::applyControl() {
	leftMotor.run(pwmTranslation - pwmRotation);
	rightMotor.run(pwmTranslation + pwmRotation);
	movingForPositioningServo = true;
}

/**
 * Ordres
 */

void MotionControlSystem::orderTranslation(int32_t mmDistance) {
	translationSetpoint += (int32_t) mmDistance / TICK_TO_MM;
	moving = true;
}

void MotionControlSystem::orderRotation(float angleRadian) {
	int32_t angleTick = (angleRadian - originalAngle) / TICK_TO_RADIAN;
	rotationSetpoint = MotionControlSystem::optimumAngle(currentAngle,
			angleTick);
	moving = true;
}

void MotionControlSystem::orderRawPwm(Side side, int16_t pwm) {
	if (side == Side::LEFT)
		leftMotor.run(pwm);
	else
		rightMotor.run(pwm);
}

void MotionControlSystem::stop() {
	translationSetpoint = currentDistance;
	rotationSetpoint = currentAngle;

	pwmRotation = 0;
	pwmTranslation = 0;
	leftMotor.run(0);
	rightMotor.run(0);
	moving = false;
	movingForPositioningServo = false;
}

/**
 * Getters/Setters des constantes d'asservissement en translation/rotation
 */

void MotionControlSystem::getTranslationTunings(float &kp, float &ki,
		float &kd) const {
	kp = translationPID.getKp();
	ki = translationPID.getKi();
	kd = translationPID.getKd();
}
void MotionControlSystem::getRotationTunings(float &kp, float &ki,
		float &kd) const {
	kp = rotationPID.getKp();
	ki = rotationPID.getKi();
	kd = rotationPID.getKd();
}
void MotionControlSystem::setTranslationTunings(float kp, float ki, float kd) {
	translationPID.setTunings(kp, ki, kd);
}
void MotionControlSystem::setRotationTunings(float kp, float ki, float kd) {
	rotationPID.setTunings(kp, ki, kd);
}

float MotionControlSystem::getAngleRadian() const {
	return (currentAngle * TICK_TO_RADIAN + originalAngle);
}

void MotionControlSystem::setOriginalAngle(float angle) {
	originalAngle = angle - (getAngleRadian() - originalAngle);
}

float MotionControlSystem::getX(){
	return x;
}

float MotionControlSystem::getY(){
	return y;
}


