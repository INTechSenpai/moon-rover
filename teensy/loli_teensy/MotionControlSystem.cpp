#include "MotionControlSystem.h"


MotionControlSystem::MotionControlSystem() : 
motor(), 
leftMotorEncoder(PIN_A_LEFT_MOTOR_ENCODER, PIN_B_LEFT_MOTOR_ENCODER),
rightMotorEncoder(PIN_A_RIGHT_MOTOR_ENCODER, PIN_B_RIGHT_MOTOR_ENCODER),
leftFreeEncoder(PIN_A_LEFT_BACK_ENCODER, PIN_B_LEFT_BACK_ENCODER),
rightFreeEncoder(PIN_A_RIGHT_BACK_ENCODER, PIN_B_RIGHT_BACK_ENCODER),
rightSpeedPID(&currentRightSpeed, &rightPWM, &rightSpeedSetpoint),
leftSpeedPID(&currentLeftSpeed, &leftPWM, &leftSpeedSetpoint),
translationPID(&currentDistance, &movingSpeedSetpoint, &translationSetpoint),
direction(DirectionController::Instance()),
averageLeftSpeed(), averageRightSpeed(),
leftMotorBlockingMgr(leftSpeedSetpoint, currentLeftSpeed),
rightMotorBlockingMgr(rightSpeedSetpoint, currentRightSpeed),
endOfMoveMgr(currentMovingSpeed)
{
	positionControlled = true;
	leftSpeedControlled = true;
	rightSpeedControlled = true;
	pwmControlled = true;

	currentDistance = 0;
	currentLeftSpeed = 0;
	currentRightSpeed = 0;

	leftSpeedPID.setOutputLimits(-1023, 1023);
	rightSpeedPID.setOutputLimits(-1023, 1023);

	leftMotorBlockingMgr.setTunings(0.5, 100);
	rightMotorBlockingMgr.setTunings(0.5, 100);
	endOfMoveMgr.setTunings(5000, 100);


	maxAcceleration = 13000;

	translationPID.setTunings(6.5, 0, 250);
	leftSpeedPID.setTunings(2, 0.01, 100);
	rightSpeedPID.setTunings(2, 0.01, 50);
	resetPosition();
	stop();
}

void MotionControlSystem::enablePositionControl(bool enabled)
{
	positionControlled = enabled;
}

void MotionControlSystem::enableLeftSpeedControl(bool enable)
{
	leftSpeedControlled = enable;
}

void MotionControlSystem::enableRightSpeedControl(bool enable)
{
	rightSpeedControlled = enable;
}

void MotionControlSystem::enablePwmControl(bool enable)
{
	pwmControlled = enable;
}



/*
	Boucle d'asservissement
*/

void MotionControlSystem::control()
{/*
	// Pour le calcul de la vitesse instantan�e :
	static int32_t previousLeftTicks = 0;
	static int32_t previousRightTicks = 0;

	// R�cup�ration des informations des encodeurs (nombre de ticks)
	int32_t rightTicks = -(rightMotorEncoder.read());
	int32_t leftTicks = leftMotorEncoder.read();

	currentLeftSpeed = (leftTicks - previousLeftTicks) * FREQ_ASSERV; // (nb-de-tick-pass�s)*(freq_asserv) (ticks/sec)
	currentRightSpeed = (rightTicks - previousRightTicks) * FREQ_ASSERV;

	previousLeftTicks = leftTicks;
	previousRightTicks = rightTicks;

	averageLeftSpeed.add(currentLeftSpeed);
	averageRightSpeed.add(currentRightSpeed);

	// On effectue un lissage des valeurs de current[..]Speed en utilisant une moyenne glissante
	currentLeftSpeed = averageLeftSpeed.value();
	currentRightSpeed = averageRightSpeed.value();

	currentDistance = (leftTicks + rightTicks) / 2;
	currentAngle = (rightTicks - leftTicks) / 2;


	if (positionControlled)
	{
		if (currentMove >= currentTrajectory.size())
		{// Si la trajectoire est termin�e ou inexistante : pas de mouvement
			movingSpeedSetpoint = 0;
			leftSpeedSetpoint = 0;
			rightSpeedSetpoint = 0;
		}
		else
		{

			//* V�rification de fin de mouvement �l�mentaire *//*

			if (currentTrajectory[currentMove].getBendRadiusTicks() == 0)
			{// Cas d'un mouvement purement rotatif
				if ( (currentTrajectory[currentMove].getLengthTicks() >= 0 && currentAngle >= rotationSetpoint)
				  || (currentTrajectory[currentMove].getLengthTicks() <  0 && currentAngle <= rotationSetpoint) )
				{// Rotation termin�e
					nextMove();
				}
			}
			else
			{// Cas d'une trajectoire courbe standard
				if ( (currentTrajectory[currentMove].getLengthTicks() >= 0 && currentDistance >= translationSetpoint)
				  || (currentTrajectory[currentMove].getLengthTicks() <  0 && currentDistance <= translationSetpoint) )
				{// Translation termin�e
					nextMove();
				}
			}


			// On v�rifie de nouveau l'existance du mouvement �l�mentaire courant
			if (currentMove >= currentTrajectory.size())
			{// Si la trajectoire est termin�e ou inexistante : pas de mouvement
				movingSpeedSetpoint = 0;
				leftSpeedSetpoint = 0;
				rightSpeedSetpoint = 0;
			}
			else
			{
				int32_t maxSpeed = currentTrajectory[currentMove].getSpeedTicks_S();


				//  Si il est sp�cifi� explicitement que ce mouvement �l�mentaire doit se terminer
				//	� l'arr�t ("stopAfterMove" est alors pass� � TRUE).
				if (currentTrajectory[currentMove].stopAfterMove)
				{
					if (currentTrajectory[currentMove].getBendRadiusTicks() == 0)
					{// Cas d'un mouvement purement rotatif
						rotationPID.compute();		// Actualise la valeur de 'movingSpeedSetpoint'
						
						// Limitation de la consigne de vitesse en rotation
						if (movingSpeedSetpoint > maxSpeed)
							movingSpeedSetpoint = maxSpeed;
						else if (movingSpeedSetpoint < -maxSpeed)
							movingSpeedSetpoint = -maxSpeed;
					}
					else
					{// Cas d'une trajectoire courbe standard
						translationPID.compute();	// Actualise la valeur de 'movingSpeedSetpoint'

						// Limitation de la consigne de vitesse en translation
						if (movingSpeedSetpoint > maxSpeed)
							movingSpeedSetpoint = maxSpeed;
						else if (movingSpeedSetpoint < -maxSpeed)
							movingSpeedSetpoint = -maxSpeed;
					}
				}
				else
				{
					if (currentTrajectory[currentMove].getLengthTicks() > 0)
					{
						movingSpeedSetpoint = maxSpeed;
					}
					else
					{
						movingSpeedSetpoint = -maxSpeed;
					}
				}


				//* Gestion du mode "PAUSED" *//*
				if (paused)
				{
					movingSpeedSetpoint = 0;
				}


				//* Limitation des variations de movingSpeedSetpoint (limitation de l'acc�l�ration) *//*
				if (movingSpeedSetpoint - previousMovingSpeed > maxAcceleration / FREQ_ASSERV)
				{
					movingSpeedSetpoint = previousMovingSpeed + maxAcceleration / FREQ_ASSERV;
				}
				else if (previousMovingSpeed - movingSpeedSetpoint > maxAcceleration / FREQ_ASSERV)
				{
					movingSpeedSetpoint = previousMovingSpeed - maxAcceleration / FREQ_ASSERV;
				}
				previousMovingSpeed = movingSpeedSetpoint;





				//* Calcul des vitesses des deux moteurs � partir de movingSpeedSetpoint et de bendRadius *//*
				static int32_t radius;
				radius = currentTrajectory[currentMove].getBendRadiusTicks();
				if (radius == 0)
				{
					leftSpeedSetpoint = -movingSpeedSetpoint;
					rightSpeedSetpoint = movingSpeedSetpoint;
				}
				else if (radius == INFINITE_RADIUS)
				{
					leftSpeedSetpoint = movingSpeedSetpoint;
					rightSpeedSetpoint = movingSpeedSetpoint;
				}
				else
				{
					leftSpeedSetpoint = (int32_t)(((double)radius - ROBOT_RADIUS / TICK_TO_MM)*(double)movingSpeedSetpoint / (double)radius);
					rightSpeedSetpoint = (int32_t)(((double)radius + ROBOT_RADIUS / TICK_TO_MM)*(double)movingSpeedSetpoint / (double)radius);
				}
			}
		}

	}



	if (leftSpeedControlled)
		leftSpeedPID.compute();		// Actualise la valeur de 'leftPWM'
	if (rightSpeedControlled)
		rightSpeedPID.compute();	// Actualise la valeur de 'rightPWM'


	if (pwmControlled)
	{
		motor.runLeft(leftPWM);
		motor.runRight(rightPWM);
	}
	//*/
}


void MotionControlSystem::manageBlocking()
{
	// d�tecter les blocages physiques (3 BlockingMgr � tester)

	/*

	static uint32_t time = 0;
	static uint32_t previousMove = 0;

	// Au changement de UnitMove, on r�initialise le timer
	if (currentMove != previousMove)
	{
		previousMove = currentMove;
		time = 0;
	}

	if ((isPhysicallyBlocked() && moving) && !paused)
	{

		if (time == 0)
		{ //D�but du timer
			time = millis();
		}
		else
		{
			if ((millis() - time) >= delayToStop)
			{ //Si arr�t� plus de 'delayToStop' ms
				if (currentMove >= currentTrajectory.size())
				{// Si la trajectoire est termin�e
					blocked = false;
					stop();
				}
				else if (
							(currentTrajectory[currentMove].getBendRadiusTicks() != 0 && ABS(currentDistance - translationSetpoint) <= toleranceTranslation) || 
							(currentTrajectory[currentMove].getBendRadiusTicks() == 0 && ABS(currentAngle - rotationSetpoint) <= toleranceRotation)
						)
				{// Si on est suffisament proche de la fin du mouvement �l�mentaire
					blocked = false;
					if (currentMove == currentTrajectory.size() - 1)
					{// Il s'agit du dernier mouvement �l�mentaire de la trajectoire
						stop();
					}
					else
					{
						nextMove();
					}
				}
				else
				{// Sinon : il d'agit d'un blocage physique
					blocked = true;
					stop();
				}
			}
		}
	}
	else
	{
		time = 0;
		if (moving)
			blocked = false;
	}
	//*/
}

void MotionControlSystem::updatePosition() 
{
	/*
	static int32_t lastDistance = 0;
	static int32_t lastAngle = 0;

	static float deltaDistanceMm;
	static float deltaAngleRadian;

	deltaDistanceMm = (currentDistance - lastDistance) * TICK_TO_MM;
	lastDistance = currentDistance;

	deltaAngleRadian = (currentAngle - lastAngle) * TICK_TO_RADIAN;
	lastAngle = currentAngle;

	currentPosition.orientation += deltaAngleRadian;

	currentPosition.x += (deltaDistanceMm * cos(currentPosition.orientation));
	currentPosition.y += (deltaDistanceMm * sin(currentPosition.orientation));
	//*/
}

void MotionControlSystem::manageStop()
{
	// D�tecter l'arr�t du robot via le StoppingMgr
}



/*
	Gestion des d�placements
*/

void MotionControlSystem::addTrajectoryPoint(const TrajectoryPoint & trajPoint, uint8_t index)
{
	// currentTrajectory n'a pas besoin d'�tre interrupt-safe
	// mettre � jour nextStopPoint : doit �tre interrupt-safe
}

MotionControlSystem::MovingState MotionControlSystem::getMovingState() const
{
	noInterrupts();
	MovingState movingStateCpy = movingState;
	interrupts();
	return movingStateCpy;
}

void MotionControlSystem::gotoNextStopPoint()
{
	// rappel : l'�criture de movingState doit �tre interrupt-safe 
}

void MotionControlSystem::stop() 
{
	noInterrupts();
	translationSetpoint = currentDistance;
	leftSpeedSetpoint = 0;
	rightSpeedSetpoint = 0;
	leftPWM = 0;
	rightPWM = 0;
	movingSpeedSetpoint = 0;
	previousMovingSpeed = 0;
	motor.runLeft(0);
	motor.runRight(0);
	movingState = STOPPED;
	translationPID.resetErrors();
	leftSpeedPID.resetErrors();
	rightSpeedPID.resetErrors();
	interrupts();
}

void MotionControlSystem::setMaxMovingSpeed(int32_t newMaxMovingSpeed)
{
	noInterrupts();
	maxMovingSpeed = newMaxMovingSpeed;
	interrupts();
}

int32_t MotionControlSystem::getMaxMovingSpeed() const
{
	return maxMovingSpeed;
}



/**
* Getters/Setters des constantes d'asservissement en translation/rotation/vitesse
*/

void MotionControlSystem::getTranslationTunings(float &kp, float &ki, float &kd) const {
	kp = translationPID.getKp();
	ki = translationPID.getKi();
	kd = translationPID.getKd();
}
void MotionControlSystem::getLeftSpeedTunings(float &kp, float &ki, float &kd) const {
	kp = leftSpeedPID.getKp();
	ki = leftSpeedPID.getKi();
	kd = leftSpeedPID.getKd();
}
void MotionControlSystem::getRightSpeedTunings(float &kp, float &ki, float &kd) const {
	kp = rightSpeedPID.getKp();
	ki = rightSpeedPID.getKi();
	kd = rightSpeedPID.getKd();
}
void MotionControlSystem::getTrajectoryTunings(float &k1, float &k2) const {
	k1 = curvatureCorrectorK1;
	k2 = curvatureCorrectorK2;
}
void MotionControlSystem::setTranslationTunings(float kp, float ki, float kd) {
	translationPID.setTunings(kp, ki, kd);
}
void MotionControlSystem::setLeftSpeedTunings(float kp, float ki, float kd) {
	leftSpeedPID.setTunings(kp, ki, kd);
}
void MotionControlSystem::setRightSpeedTunings(float kp, float ki, float kd) {
	rightSpeedPID.setTunings(kp, ki, kd);
}
void MotionControlSystem::setTrajectoryTunings(float k1, float k2) {
	curvatureCorrectorK1 = k1;
	curvatureCorrectorK2 = k2;
}


/*
* Getters/Setters des variables de position haut niveau
*/
void MotionControlSystem::setPosition(const Position & newPosition)
{
	noInterrupts();
	position = newPosition;
	interrupts();
}

void MotionControlSystem::getPosition(Position & returnPos) const
{
	noInterrupts();
	returnPos = position;
	interrupts();
}

uint8_t MotionControlSystem::getTrajectoryIndex() const
{
	noInterrupts();
	uint8_t indexCpy = trajectoryIndex;
	interrupts();
	return indexCpy;
}

void MotionControlSystem::resetPosition()
{
	noInterrupts();
	position.x = 0;
	position.y = 0;
	position.orientation = 0;
	interrupts();
	stop();
}

