#include "MotionControlSystem.h"

MotionControlSystem::MotionControlSystem() :
		leftMotor(Side::LEFT), rightMotor(Side::RIGHT), translationControlled(
				true), rotationControlled(true), translationPID(
				&currentDistance, &pwmTranslation, &translationSetpoint), rotationPID(
				&currentAngle, &pwmRotation, &rotationSetpoint), originalAngle(
				0.0), rotationSetpoint(0), translationSetpoint(0), x(0.0), y(0.0), moving(
				false), moveAbnormal(false) {
}

void MotionControlSystem::init(int16_t maxPWMtranslation, int16_t maxPWMrotation) {
	/**
	 * Initialisation moteurs et encodeurs
	 */

	Motor::initPWM();
	Counter();

	/**
	 * Renseignement de la base de données des constantes d'asservissement
	 */
	float database[][NB_CTE_ASSERV]=
	{//		 PWM  Kp   Ki   Kd
			{120, 0.08, 0., 20.},//Translation
			{120,   2., 0., 700},//Rotation
			{100, 0.2, 0. , 90.},//Translation
			{150, 2.5, 0. , 600},//Rotation
			{  0,  0 , 0. ,   0},//Translation
			{100,  3 , 0. ,1200},//Rotation
			{ 0., 0. , 0. ,  0.},//Translation
			{ 0., 0. , 0. ,  0.} //Rotation
	};

	for(int i=0; i<NB_SPEED; i++)
	{
		for(int j=0; j<NB_CTE_ASSERV; j++)
		{
			translationTunings[i][j] = database[2*i][j];
			rotationTunings[i][j] = database[2*i+1][j];
		}
	}


	/**
	 * Réglage des PID et des PWMmax en fonction des PWMmax donnés
	 */

	this->maxPWMtranslation = maxPWMtranslation;
	this->maxPWMrotation = maxPWMrotation;

	translationPID.setControllerDirection(PidDirection::DIRECT);
	MotionControlSystem::setSmartTranslationTunings();
	rotationPID.setControllerDirection(PidDirection::DIRECT);
	MotionControlSystem::setSmartRotationTunings();

	/**
	 * Réglage de la balance des moteurs
	 * balance = PWM_moteur_droit/PWM_moteur_gauche
	 */
	setBalance(1);


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
	TIM_TimeBaseStructure.TIM_Period = 500 - 1; // 1 MHz down to 2 KHz : fréquence d'asservissement de 2kHz
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

void MotionControlSystem::enable(bool enable) {
	if (enable) {
		TIM_Cmd(TIM4, ENABLE); //Active la boucle d'asservissement
	} else {
		TIM_Cmd(TIM4, DISABLE); //Désactive la boucle d'asservissement
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

	if (translationControlled) {
		currentDistance = (leftTicks + rightTicks) / 2;
		translationPID.compute();

		if(pwmTranslation > maxPWMtranslation)
			pwmTranslation = maxPWMtranslation;
		if(pwmTranslation < -maxPWMtranslation)
			pwmTranslation = -maxPWMtranslation;

	} else
		pwmTranslation = 0;

	if (rotationControlled) {
		currentAngle = (rightTicks - leftTicks) / 2;
		rotationPID.compute();

		if(pwmRotation > maxPWMrotation)
			pwmRotation = maxPWMrotation;
		if(pwmRotation < -maxPWMrotation)
			pwmRotation = -maxPWMrotation;

	} else
		pwmRotation = 0;
	applyControl();
}

bool MotionControlSystem::isPhysicallyStopped(int seuil) {
	return (ABS(translationPID.getDerivativeError()) <= seuil)
			&& (ABS(rotationPID.getDerivativeError()) <= seuil);
}

void MotionControlSystem::manageStop()
{
	static uint32_t time = 0;

	if (isPhysicallyStopped(1) && moving)
	{

		if (time == 0)
		{ //Début du timer
			time = Millis();
		}
		else
		{
			if ((Millis() - time) >= 500)
			{ //Si arrêté plus de 500ms
				if (ABS(translationPID.getError()) <= 100 && ABS(rotationPID.getError()) <= 100)
				{ //Stopé pour cause de fin de mouvement
					serial.printfln("fin de mouvement, err = %d", translationPID.getError());
					stop();
					moveAbnormal = false;
				}
				else if (ABS(pwmRotation) >= 60 || ABS(pwmTranslation) >= 60)
				{ //Stoppé pour blocage
					serial.printfln("bloque !");
					stop();
					moveAbnormal = true;
				}
				else
				{//Stoppé par les frottements du robot sur la table
					serial.printfln("frottements");
					stop();
				}
			}
		}
	}
	else
	{
		time = 0;
		if(moving)
			moveAbnormal = false;
	}
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
}

void MotionControlSystem::track(){
	static int i = 0;//Curseur du tableau
	this->trackArray[i][0] = x;
	this->trackArray[i][1] = y;
	this->trackArray[i][2] = getAngleRadian();
	this->trackArray[i][3] = pwmTranslation;
	this->trackArray[i][4] = pwmRotation;
	i = (i+1)%(TRACKER_SIZE);
}

void MotionControlSystem::printTracking(){
	for(int i=0; i<TRACKER_SIZE; i++)
	{
		if(this->trackArray[i][3] != 0 || this->trackArray[i][4] != 0)
		{
			serial.printfln("x=%f | y=%f | o=%f", this->trackArray[i][0], this->trackArray[i][1], this->trackArray[i][2]);
			serial.printfln("pwmT=%f | pwmR=%f", this->trackArray[i][3], this->trackArray[i][4]);
		}
	}
}

void MotionControlSystem::clearTracking(){
	for(int i=0; i<TRACKER_SIZE; i++)
	{
		this->trackArray[i][0] = 0;
		this->trackArray[i][1] = 0;
		this->trackArray[i][2] = 0;
		this->trackArray[i][3] = 0;
		this->trackArray[i][4] = 0;
	}
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

	leftMotor.run(0);
	rightMotor.run(0);
	moving = false;
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

void MotionControlSystem::setX(float newX){
	this->x = newX;
}

void MotionControlSystem::setY(float newY){
	this->y = newY;
}

float MotionControlSystem::getBalance(){
	return balance;
}

void MotionControlSystem::setBalance(float newBalance){
	balance = newBalance;
}

void MotionControlSystem::setMaxPWMtranslation(int16_t PWM){
	this->maxPWMtranslation = PWM;
}

void MotionControlSystem::setMaxPWMrotation(int16_t PWM){
	this->maxPWMrotation = PWM;
}

int16_t MotionControlSystem::getMaxPWMtranslation(){
	return this->maxPWMtranslation;
}

int16_t MotionControlSystem::getMaxPWMrotation(){
	return this->maxPWMrotation;
}

void MotionControlSystem::setSmartTranslationTunings()
{
	int i = getBestTuningsInDatabase(this->maxPWMtranslation, this->translationTunings);
	translationPID.setTunings(translationTunings[i][1], translationTunings[i][2], translationTunings[i][3]);
}

void MotionControlSystem::setSmartRotationTunings()
{
	int i = getBestTuningsInDatabase(this->maxPWMrotation, this->rotationTunings);
	rotationPID.setTunings(rotationTunings[i][1], rotationTunings[i][2], rotationTunings[i][3]);
}

int MotionControlSystem::getBestTuningsInDatabase(int16_t pwm, float database[NB_SPEED][NB_CTE_ASSERV])
{
	float ecartMin = 255, indice;
	for(int i=0; i<NB_CTE_ASSERV; i++)
	{
		float ecart = ABS(database[i][0] - float(pwm));
		if(ecart < ecartMin)
		{
			ecartMin = ecart;
			indice = i;
		}
	}
	return indice;
}

bool MotionControlSystem::isMoving(){
	return moving;
}

bool MotionControlSystem::isMoveAbnormal(){
	return moveAbnormal;
}
