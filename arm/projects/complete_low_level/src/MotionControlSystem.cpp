#include "MotionControlSystem.h"

MotionControlSystem::MotionControlSystem(): leftMotor(Side::LEFT), rightMotor(Side::RIGHT),
	translationPID(&currentDistance, &pwmTranslation, &translationSetpoint),
	rotationPID(&currentAngle, &pwmRotation, &rotationSetpoint)
{
	translationControlled = true;
	rotationControlled = true;
	originalAngle = 0.0;
	rotationSetpoint = 0;
	translationSetpoint = 0;
	x = 0;
	y = 0;
	moving = false;
	moveAbnormal = false;

	delayToStop = 500;
	toleranceInTick = 100;
	pwmMinToMove = 0;//60
	minSpeed = 1;
}

MotionControlSystem::MotionControlSystem(const MotionControlSystem&): leftMotor(Side::LEFT), rightMotor(Side::RIGHT),
	translationPID(&currentDistance, &pwmTranslation, &translationSetpoint),
	rotationPID(&currentAngle, &pwmRotation, &rotationSetpoint)
{
	translationControlled = true;
	rotationControlled = true;
	originalAngle = 0.0;
	rotationSetpoint = 0;
	translationSetpoint = 0;
	x = 0;
	y = 0;
	moving = false;
	moveAbnormal = false;
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
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 16;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
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

int MotionControlSystem::getPWMTranslation() const{
	return pwmTranslation;
}

int MotionControlSystem::getPWMRotation() const{
	return pwmRotation;
}

int MotionControlSystem::getTranslationGoal() const{
	return translationSetpoint;
}

int MotionControlSystem::getRotationGoal() const{
	return rotationSetpoint;
}

int MotionControlSystem::getLeftEncoder() const{
	return Counter::getLeftValue();
}

int MotionControlSystem::getRightEncoder() const{
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

	/*
	 * Comptage des ticks de la roue droite
	 * Cette codeuse est connectée à un timer 16bit
	 * on subit donc un overflow/underflow de la valeur des ticks tous les 7 mètres environ
	 * ceci est corrigé de manière à pouvoir parcourir des distances grandes sans devenir fou en chemin (^_^)
	 */
	static int32_t lastRawRightTicks = 0;	//On garde en mémoire le nombre de ticks obtenu au précédent appel
	static int rightOverflow = 0;			//On garde en mémoire le nombre de fois que l'on a overflow (négatif pour les underflow)

	int32_t rawRightTicks = Counter::getRightValue();	//Nombre de ticks avant tout traitement

	if (lastRawRightTicks - rawRightTicks > 32768)		//Détection d'un overflow
		rightOverflow++;
	else if(lastRawRightTicks - rawRightTicks < -32768)	//Détection d'un underflow
		rightOverflow--;

	lastRawRightTicks = rawRightTicks;

	int32_t rightTicks = rawRightTicks + rightOverflow*65535;	//On calcul le nombre réel de ticks

	/*
	 * Comptage des ticks de la roue gauche
	 * ici on est sur un timer 32bit, pas de problème d'overflow sauf si on tente de parcourir plus de 446km...
	 */
	int32_t leftTicks = Counter::getLeftValue();


	currentDistance = (leftTicks + rightTicks) / 2;
	translationPID.compute();

	if (translationControlled)
	{
		if(pwmTranslation > maxPWMtranslation)
			pwmTranslation = maxPWMtranslation;
		if(pwmTranslation < -maxPWMtranslation)
			pwmTranslation = -maxPWMtranslation;
	}
	else
		pwmTranslation = 0;

	currentAngle = (rightTicks - leftTicks) / 2;
	rotationPID.compute();

	if (rotationControlled)
	{
		if(pwmRotation > maxPWMrotation)
			pwmRotation = maxPWMrotation;
		if(pwmRotation < -maxPWMrotation)
			pwmRotation = -maxPWMrotation;
	}
	else
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

	if (isPhysicallyStopped(minSpeed) && moving)
	{

		if (time == 0)
		{ //Début du timer
			time = Millis();
		}
		else
		{
			if ((Millis() - time) >= delayToStop)
			{ //Si arrêté plus de 500ms
				if (ABS(translationPID.getError()) <= toleranceInTick && ABS(rotationPID.getError()) <= toleranceInTick)
				{ //Stopé pour cause de fin de mouvement
					//serial.printfln("fin de mouvement, err = %d", translationPID.getError());
					stop();
					moveAbnormal = false;
				}
				else if (ABS(pwmRotation) >= pwmMinToMove || ABS(pwmTranslation) >= pwmMinToMove)
				{ //Stoppé pour blocage
					//serial.printfln("bloque !");
					stop();
					moveAbnormal = true;
				}
				else
				{//Stoppé par les frottements du robot sur la table
					//serial.printfln("frottements");
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

	this->trackArray[i].translationCourante = currentDistance;
	this->trackArray[i].rotationCourante = currentAngle;
	this->trackArray[i].pwmTranslation = pwmTranslation;
	this->trackArray[i].pwmRotation = pwmRotation;
	this->trackArray[i].consigneTranslation = translationSetpoint;
	this->trackArray[i].consigneRotation = rotationSetpoint;
	this->trackArray[i].x = x;
	this->trackArray[i].y = y;
	this->trackArray[i].angle = getAngleRadian();
	this->trackArray[i].asservTranslation = translationControlled;
	this->trackArray[i].asservRotation = rotationControlled;
	this->trackArray[i].tailleBufferReception = serial.available();

	i = (i+1)%(TRACKER_SIZE);
}

void MotionControlSystem::printTrackingOXY()
{
	for(int i=0; i<TRACKER_SIZE; i++)
	{
		serial.printfln("x=%f | y=%f | o=%f", this->trackArray[i].x, this->trackArray[i].y, this->trackArray[i].angle);
	}
}

void MotionControlSystem::printTrackingAll()
{
	for(int i=0; i<TRACKER_SIZE; i++)
	{
		serial.printfln("x=%f | y=%f | o=%f | cons_T=%d | cons_R=%d | curr_T=%d | curr_R=%d | asservT=%d | asservR=%d | pwmT=%d | pwmR=%d | tBuff=%d",
				trackArray[i].x, trackArray[i].y, trackArray[i].angle, trackArray[i].consigneTranslation, trackArray[i].consigneRotation
				, trackArray[i].translationCourante, trackArray[i].rotationCourante, trackArray[i].asservTranslation, trackArray[i].asservRotation
				, trackArray[i].pwmTranslation, trackArray[i].pwmRotation, trackArray[i].tailleBufferReception);
	}
}

void MotionControlSystem::printTrackingLocomotion()
{
	for(int i=0; i<TRACKER_SIZE; i++)
	{
		serial.printfln("cons_T=%d | cons_R=%d | curr_T=%d | curr_R=%d | asservT=%d | asservR=%d | pwmT=%d | pwmR=%d",
					trackArray[i].consigneTranslation, trackArray[i].consigneRotation,
					trackArray[i].translationCourante, trackArray[i].rotationCourante,
					trackArray[i].asservTranslation, trackArray[i].asservRotation
					, trackArray[i].pwmTranslation, trackArray[i].pwmRotation);
	}
}

void MotionControlSystem::printTrackingSerie()
{
	for(int i=0; i<TRACKER_SIZE; i++)
	{
		serial.printfln("tBuff=%d", trackArray[i].tailleBufferReception);
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

float MotionControlSystem::getX() const{
	return x;
}

float MotionControlSystem::getY() const{
	return y;
}

void MotionControlSystem::setX(float newX){
	this->x = newX;
}

void MotionControlSystem::setY(float newY){
	this->y = newY;
}

float MotionControlSystem::getBalance() const{
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

int16_t MotionControlSystem::getMaxPWMtranslation() const{
	return this->maxPWMtranslation;
}

int16_t MotionControlSystem::getMaxPWMrotation() const{
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

int MotionControlSystem::getBestTuningsInDatabase(int16_t pwm, float database[NB_SPEED][NB_CTE_ASSERV]) const
{
	float ecartMin = 255, indice = 0;
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

bool MotionControlSystem::isMoving() const{
	return moving;
}

bool MotionControlSystem::isMoveAbnormal() const{
	return moveAbnormal;
}
