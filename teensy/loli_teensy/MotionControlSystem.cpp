#include "MotionControlSystem.h"


MotionControlSystem::MotionControlSystem() :
leftMotorEncoder(PIN_A_LEFT_MOTOR_ENCODER, PIN_B_LEFT_MOTOR_ENCODER),
rightMotorEncoder(PIN_A_RIGHT_MOTOR_ENCODER, PIN_B_RIGHT_MOTOR_ENCODER),
leftFreeEncoder(PIN_A_LEFT_BACK_ENCODER, PIN_B_LEFT_BACK_ENCODER),
rightFreeEncoder(PIN_A_RIGHT_BACK_ENCODER, PIN_B_RIGHT_BACK_ENCODER),
direction(DirectionController::Instance()),
rightSpeedPID(&currentRightSpeed, &rightPWM, &rightSpeedSetpoint),
rightMotorBlockingMgr(rightSpeedSetpoint, currentRightSpeed),
leftSpeedPID(&currentLeftSpeed, &leftPWM, &leftSpeedSetpoint),
leftMotorBlockingMgr(leftSpeedSetpoint, currentLeftSpeed),
translationPID(&currentTranslation, &movingSpeedSetpoint, &translationSetpoint),
endOfMoveMgr(currentMovingSpeed)
{
	currentTranslation = 0;
	currentLeftSpeed = 0;
	currentRightSpeed = 0;

	maxMovingSpeed = 0;

	movingState = STOPPED;
	trajectoryIndex = 0;
	updateNextStopPoint();
	updateSideDistanceFactors();

	leftSpeedPID.setOutputLimits(-1023, 1023);
	rightSpeedPID.setOutputLimits(-1023, 1023);

	loadParameters();

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

void MotionControlSystem::getEnableStates(bool &cp, bool &cvg, bool &cvd, bool &cpwm)
{
	cp = positionControlled;
	cvg = leftSpeedControlled;
	cvd = rightSpeedControlled;
	cpwm = pwmControlled;
}



/*
	Boucle d'asservissement
*/

void MotionControlSystem::control()
{
	updateSpeedAndPosition();
	manageStop();
	manageBlocking();
	checkTrajectory();
	updateTrajectoryIndex();

	if (positionControlled)
	{
		if (movingState == MOVE_INIT || movingState == MOVING)
		{
			/* Asservissement sur trajectoire */
			static float posError;
			static float orientationError;
			TrajectoryPoint currentTrajPoint = currentTrajectory[trajectoryIndex];
			Position posConsigne = currentTrajPoint.getPosition();
			posError = sqrtf(square(position.x - posConsigne.x) + square(position.y - posConsigne.y));
			orientationError = position.orientation - posConsigne.orientation;
			curvatureOrder = currentTrajPoint.getCurvature() - curvatureCorrectorK1 * posError - curvatureCorrectorK2 * orientationError;
			direction.setAimCurvature(curvatureOrder);

			if (movingState == MOVE_INIT)
			{
				if (ABS(direction.getRealCurvature() - currentTrajPoint.getCurvature()) < CURVATURE_TOLERANCE)
				{
					movingState = MOVING;
				}
				leftSpeedSetpoint = 0;
				rightSpeedSetpoint = 0;
			}
			else
			{
				translationPID.compute(); // MAJ movingSpeedSetpoint

				// Limitation de l'accélération
				if (movingSpeedSetpoint - previousMovingSpeedSetpoint > maxAcceleration)
				{
					movingSpeedSetpoint = previousMovingSpeedSetpoint + maxAcceleration;
				}
				else if (movingSpeedSetpoint - previousMovingSpeedSetpoint < -maxAcceleration)
				{
					movingSpeedSetpoint = previousMovingSpeedSetpoint - maxAcceleration;
				}

				// Limitation de la vitesse
				if (movingSpeedSetpoint > ABS(maxMovingSpeed))
				{
					movingSpeedSetpoint = ABS(maxMovingSpeed);
				}

				// Calcul des vitesses gauche et droite en fonction de la vitesse globale
				leftSpeedSetpoint = movingSpeedSetpoint * leftSideDistanceFactor;
				rightSpeedSetpoint = movingSpeedSetpoint * rightSideDistanceFactor;

				// Gestion du sens de déplacement
				if (maxMovingSpeed < 0)
				{
					leftSpeedSetpoint = -leftSpeedSetpoint;
					rightSpeedSetpoint = -rightSpeedSetpoint;
				}
			}
		}
		else
		{
			leftSpeedSetpoint = 0;
			rightSpeedSetpoint = 0;
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
}

void MotionControlSystem::updateSpeedAndPosition() 
{
	static int32_t
		leftMotorTicks = 0,
		rightMotorTicks = 0,
		leftTicks = 0,
		rightTicks = 0;
	static int32_t
		previousLeftMotorTicks = 0,
		previousRightMotorTicks = 0,
		previousLeftTicks = 0,
		previousRightTicks = 0;
	static int32_t
		deltaLeftMotorTicks = 0,
		deltaRightMotorTicks = 0,
		deltaLeftTicks = 0,
		deltaRightTicks = 0,
		deltaTranslation = 0;
	static float
		deltaTranslation_mm = 0,
		half_deltaRotation_rad = 0,
		currentAngle = 0,
		corrector = 1;

	// Récupération des données des encodeurs
	leftMotorTicks = leftMotorEncoder.read() * FRONT_TICK_TO_TICK;
	rightMotorTicks = rightMotorEncoder.read() * FRONT_TICK_TO_TICK;
	leftTicks = leftFreeEncoder.read();
	rightTicks = rightFreeEncoder.read();

	// Calcul du mouvement de chaque roue depuis le dernier asservissement
	deltaLeftMotorTicks = leftMotorTicks - previousLeftMotorTicks;
	deltaRightMotorTicks = rightMotorTicks - previousRightMotorTicks;
	deltaLeftTicks = leftTicks - previousLeftTicks;
	deltaRightTicks = rightTicks - previousRightTicks;

	previousLeftMotorTicks = leftMotorTicks;
	previousRightMotorTicks = rightMotorTicks;
	previousLeftTicks = leftTicks;
	previousRightTicks = rightTicks;

	// Mise à jour de la vitesse des moteurs
	currentLeftSpeed = deltaLeftMotorTicks * FREQ_ASSERV;
	currentRightSpeed = deltaRightMotorTicks * FREQ_ASSERV;
	averageLeftSpeed.add(currentLeftSpeed);
	averageRightSpeed.add(currentRightSpeed);
	currentLeftSpeed = averageLeftSpeed.value();
	currentRightSpeed = averageRightSpeed.value();

	// Mise à jour de la position et de l'orientattion
	deltaTranslation = ((deltaLeftTicks + deltaRightTicks) / 2);
	deltaTranslation_mm = deltaTranslation * TICK_TO_MM;
	half_deltaRotation_rad = ((deltaRightTicks - deltaLeftTicks) / 4) * TICK_TO_RADIANS;
	currentAngle = position.orientation + half_deltaRotation_rad;
	position.setOrientation(position.orientation + half_deltaRotation_rad * 2);
	corrector = 1 - square(half_deltaRotation_rad) / 6;
	position.x += corrector * deltaTranslation_mm * cosf(currentAngle);
	position.y += corrector * deltaTranslation_mm * sinf(currentAngle);

	// Mise à jour de currentTranslation
	currentTranslation += deltaTranslation;

	// Mise à jour de la vitesse de translation
	currentMovingSpeed = deltaTranslation * FREQ_ASSERV;
	averageTranslationSpeed.add(currentMovingSpeed);
	currentMovingSpeed = averageTranslationSpeed.value();

	// Mise à jour des erreurs cumulatives des encodeurs des moteurs
	leftMotorError += deltaLeftMotorTicks - (int)((float)deltaTranslation * leftSideDistanceFactor);
	rightMotorError += deltaRightMotorTicks - (int)((float)deltaTranslation * rightSideDistanceFactor);

	// En cas d'erreur excessive au niveau des moteurs de propulsion, le robot est considéré bloqué.
	if (ABS(leftMotorError) > MOTOR_SLIP_TOLERANCE || ABS(rightMotorError) > MOTOR_SLIP_TOLERANCE)
	{
		movingState = EXT_BLOCKED;
		stop();
		Log::critical(34, "Derapage d'un moteur de propulsion");
	}
}

void MotionControlSystem::updateTrajectoryIndex()
{
	if (movingState == MOVING && !currentTrajectory[trajectoryIndex].isStopPoint())
	{
		uint8_t nextPoint = trajectoryIndex + 1;
		while 
			(
			currentTrajectory[nextPoint].isUpToDate() && 
			position.isCloserToAThanB(currentTrajectory[nextPoint].getPosition(), currentTrajectory[trajectoryIndex].getPosition())
			)
		{
			nextPoint++;
		}
		if (nextPoint > trajectoryIndex + 1)
		{
			trajectoryIndex = nextPoint - 1;
			updateTranslationSetpoint();
			updateSideDistanceFactors();
		}
	}
	else if (movingState == STOPPED && currentTrajectory[trajectoryIndex].isStopPoint())
	{
		if (currentTrajectory[(uint8_t)(trajectoryIndex + 1)].isUpToDate())
		{
			currentTrajectory[trajectoryIndex].makeObsolete();
			trajectoryIndex++;
			updateNextStopPoint();
			updateSideDistanceFactors();
		}
	}
}

void MotionControlSystem::updateNextStopPoint()
{
	if (currentTrajectory[trajectoryIndex].isUpToDate() && currentTrajectory[trajectoryIndex].isStopPoint())
	{
		nextStopPoint = trajectoryIndex;
	}
	else
	{
		uint16_t infiniteLoopCheck = 0;
		bool found = false;
		nextStopPoint = trajectoryIndex + 1;
		while (currentTrajectory[nextStopPoint].isUpToDate() && infiniteLoopCheck < UINT8_MAX + 1)
		{
			if (currentTrajectory[nextStopPoint].isStopPoint())
			{
				found = true;
				break;
			}
			nextStopPoint++;
			infiniteLoopCheck++;
		}
		if (!found)
		{
			nextStopPoint = UINT16_MAX;
		}
	}
	updateTranslationSetpoint();
}

void MotionControlSystem::checkTrajectory()
{
	if (movingState == MOVE_INIT || movingState == MOVING)
	{
		bool valid = true;
		if (!currentTrajectory[trajectoryIndex].isUpToDate())
		{
			valid = false;
		}
		else if (movingState == MOVING && !currentTrajectory[trajectoryIndex].isStopPoint())
		{
			if (!currentTrajectory[(uint8_t)(trajectoryIndex + 1)].isUpToDate())
			{
				valid = false;
			}
		}
		if (!valid)
		{
			movingState = EMPTY_TRAJ;
			stop();
			Log::critical(32, "Empty trajectory");
		}
	}
}

void MotionControlSystem::updateTranslationSetpoint()
{
	if (nextStopPoint == UINT16_MAX)
	{
		translationSetpoint = currentTranslation + UINT8_MAX * TRAJECTORY_STEP;
	}
	else
	{
		uint8_t nbPointsToTravel = nextStopPoint - trajectoryIndex;
		translationSetpoint = currentTranslation + nbPointsToTravel * TRAJECTORY_STEP;
		translationSetpoint += TRAJECTORY_STEP / 2;
	}
}

void MotionControlSystem::updateSideDistanceFactors()
{
	static float half_width = LEFT_RIGHT_WHEELS_DISTANCE / 2;
	static float squared_length = square(FRONT_BACK_WHEELS_DISTANCE);

	float c = currentTrajectory[trajectoryIndex].getCurvature();
	if (c == 0 || !currentTrajectory[trajectoryIndex].isUpToDate())
	{
		leftSideDistanceFactor = 1;
		rightSideDistanceFactor = 1;
	}
	else
	{
		float r = 1 / c;
		leftSideDistanceFactor = sqrtf(square(r - half_width) + squared_length) / r;
		rightSideDistanceFactor = sqrtf(square(r + half_width) + squared_length) / r;
	}
}

void MotionControlSystem::manageStop()
{
	endOfMoveMgr.compute();
	if (endOfMoveMgr.isStopped() && movingState == MOVING)
	{
		if (currentTrajectory[trajectoryIndex].isStopPoint())
		{
			movingState = STOPPED;
		}
		else
		{
			movingState = EXT_BLOCKED;
			Log::critical(33, "Erreur d'asservissement en translation");
		}
		stop();
	}
}

void MotionControlSystem::manageBlocking()
{
	leftMotorBlockingMgr.compute();
	rightMotorBlockingMgr.compute();
	if (leftMotorBlockingMgr.isBlocked() || rightMotorBlockingMgr.isBlocked())
	{
		movingState = INT_BLOCKED;
		stop();
		Log::critical(31, "Blocage physique d'un moteur");
	}
}



/*
	Gestion des déplacements
*/

void MotionControlSystem::addTrajectoryPoint(const TrajectoryPoint & trajPoint, uint8_t index)
{
	bool updateIsNeeded = false;
	if (trajPoint.isStopPoint() || (currentTrajectory[index].isUpToDate() && currentTrajectory[index].isStopPoint()))
	{
		updateIsNeeded = true;
	}
	currentTrajectory[index] = trajPoint;
	if (updateIsNeeded)
	{
		noInterrupts();
		updateNextStopPoint();
		interrupts();
	}
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
	noInterrupts();
	if (movingState != MOVING)
	{
		movingState = MOVE_INIT;
	}
	interrupts();
}

void MotionControlSystem::stop() 
{
	noInterrupts();
	currentTranslation = 0;
	translationSetpoint = 0;
	leftSpeedSetpoint = 0;
	rightSpeedSetpoint = 0;
	leftPWM = 0;
	rightPWM = 0;
	movingSpeedSetpoint = 0;
	previousMovingSpeedSetpoint = 0;
	motor.runLeft(0);
	motor.runRight(0);
	translationPID.resetErrors();
	leftSpeedPID.resetErrors();
	rightSpeedPID.resetErrors();
	leftMotorError = 0;
	rightMotorError = 0;
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

void MotionControlSystem::setMaxAcceleration(int32_t newMaxAcceleration)
{
	noInterrupts();
	maxAcceleration = newMaxAcceleration;
	interrupts();
}

int32_t MotionControlSystem::getMaxAcceleration() const
{
	return maxAcceleration;
}



/**
* Getters/Setters des constantes d'asservissement en translation/rotation/vitesse
*/

void MotionControlSystem::setCurrentPIDTunings(float kp, float ki, float kd)
{
	switch (pidToSet)
	{
	case MotionControlSystem::LEFT_SPEED:
		setLeftSpeedTunings(kp, ki, kd);
		break;
	case MotionControlSystem::RIGHT_SPEED:
		setRightSpeedTunings(kp, ki, kd);
		break;
	case MotionControlSystem::SPEED:
		setLeftSpeedTunings(kp, ki, kd);
		setRightSpeedTunings(kp, ki, kd);
		break;
	case MotionControlSystem::TRANSLATION:
		setTranslationTunings(kp, ki, kd);
		break;
	default:
		break;
	}
}
void MotionControlSystem::getCurrentPIDTunings(float &kp, float &ki, float &kd) const
{
	switch (pidToSet)
	{
	case MotionControlSystem::LEFT_SPEED:
		getLeftSpeedTunings(kp, ki, kd);
		break;
	case MotionControlSystem::RIGHT_SPEED:
		getRightSpeedTunings(kp, ki, kd);
		break;
	case MotionControlSystem::SPEED:
		float kpl, kil, kdl, kpr, kir, kdr;
		getLeftSpeedTunings(kpl, kil, kdl);
		getRightSpeedTunings(kpr, kir, kdr);
		if ((kpl != kpr || kil != kir) || kdl != kdr)
		{
			Log::warning("Left/Right speed PID tunings are different, left tunings are returned");
		}
		kp = kpl;
		ki = kil;
		kd = kdl;
		break;
	case MotionControlSystem::TRANSLATION:
		getTranslationTunings(kp, ki, kd);
		break;
	default:
		break;
	}
}
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
void MotionControlSystem::setPIDtoSet(PIDtoSet newPIDtoSet)
{
	pidToSet = newPIDtoSet;
}
MotionControlSystem::PIDtoSet MotionControlSystem::getPIDtoSet() const
{
	return pidToSet;
}
void MotionControlSystem::getPIDtoSet_str(char * str, size_t size) const
{
	char leftSpeedStr[] = "LEFT_SPEED";
	char rightSpeedStr[] = "RIGHT_SPEED";
	char speedStr[] = "SPEED";
	char translationStr[] = "TRANSLATION";

	if (size == 0)
	{
		return;
	}
	else if (size < 12)
	{
		str[0] = '\0';
	}
	else
	{
		switch (pidToSet)
		{
		case MotionControlSystem::LEFT_SPEED:
			strcpy(str, leftSpeedStr);
			break;
		case MotionControlSystem::RIGHT_SPEED:
			strcpy(str, rightSpeedStr);
			break;
		case MotionControlSystem::SPEED:
			strcpy(str, speedStr);
			break;
		case MotionControlSystem::TRANSLATION:
			strcpy(str, translationStr);
			break;
		default:
			str[0] = '\0';
			break;
		}
	}
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


/*
*	Réglage des blockingMgr et stoppingMgr
*/

void MotionControlSystem::setLeftMotorBmgrTunings(float sensibility, uint32_t responseTime)
{
	noInterrupts();
	leftMotorBlockingMgr.setTunings(sensibility, responseTime);
	interrupts();
}

void MotionControlSystem::setRightMotorBmgrTunings(float sensibility, uint32_t responseTime)
{
	noInterrupts();
	rightMotorBlockingMgr.setTunings(sensibility, responseTime);
	interrupts();
}

void MotionControlSystem::setEndOfMoveMgrTunings(uint32_t epsilon, uint32_t responseTime)
{
	noInterrupts();
	endOfMoveMgr.setTunings(epsilon, responseTime);
	interrupts();
}

void MotionControlSystem::getLeftMotorBmgrTunings(float & sensibility, uint32_t & responseTime) const
{
	leftMotorBlockingMgr.getTunings(sensibility, responseTime);
}

void MotionControlSystem::getRightMotorBmgrTunings(float & sensibility, uint32_t & responseTime) const
{
	rightMotorBlockingMgr.getTunings(sensibility, responseTime);
}

void MotionControlSystem::getEndOfMoveMgrTunings(uint32_t & epsilon, uint32_t & responseTime) const
{
	endOfMoveMgr.getTunings(epsilon, responseTime);
}


/*
*	Getters/Setters de débug
*/

void MotionControlSystem::getTicks(int32_t & leftFront, int32_t & rightFront, int32_t & leftBack, int32_t & rightBack)
{
	leftFront = leftMotorEncoder.read();
	rightFront = rightMotorEncoder.read();
	leftBack = leftFreeEncoder.read();
	rightBack = rightFreeEncoder.read();
}

void MotionControlSystem::saveParameters()
{
	int a = 0; // Adresse mémoire dans l'EEPROM
	float kp, ki, kd, s;
	uint32_t e, t;

	EEPROM.put(a, positionControlled);
	a += sizeof(positionControlled);
	EEPROM.put(a, leftSpeedControlled);
	a += sizeof(leftSpeedControlled);
	EEPROM.put(a, rightSpeedControlled);
	a += sizeof(rightSpeedControlled);
	EEPROM.put(a, pwmControlled);
	a += sizeof(pwmControlled);

	leftMotorBlockingMgr.getTunings(s, t);
	EEPROM.put(a, s);
	a += sizeof(s);
	EEPROM.put(a, t);
	a += sizeof(t);

	rightMotorBlockingMgr.getTunings(s, t);
	EEPROM.put(a, s);
	a += sizeof(s);
	EEPROM.put(a, t);
	a += sizeof(t);

	endOfMoveMgr.getTunings(e, t);
	EEPROM.put(a, e);
	a += sizeof(e);
	EEPROM.put(a, t);
	a += sizeof(t);

	EEPROM.put(a, maxAcceleration);
	a += sizeof(maxAcceleration);

	kp = translationPID.getKp();
	EEPROM.put(a, kp);
	a += sizeof(kp);
	ki = translationPID.getKi();
	EEPROM.put(a, ki);
	a += sizeof(ki);
	kd = translationPID.getKd();
	EEPROM.put(a, kd);
	a += sizeof(kd);

	kp = leftSpeedPID.getKp();
	EEPROM.put(a, kp);
	a += sizeof(kp);
	ki = leftSpeedPID.getKi();
	EEPROM.put(a, ki);
	a += sizeof(ki);
	kd = leftSpeedPID.getKd();
	EEPROM.put(a, kd);
	a += sizeof(kd);

	kp = rightSpeedPID.getKp();
	EEPROM.put(a, kp);
	a += sizeof(kp);
	ki = rightSpeedPID.getKi();
	EEPROM.put(a, ki);
	a += sizeof(ki);
	kd = rightSpeedPID.getKd();
	EEPROM.put(a, kd);
	a += sizeof(kd);

	EEPROM.put(a, curvatureCorrectorK1);
	a += sizeof(curvatureCorrectorK1);
	EEPROM.put(a, curvatureCorrectorK2);
	a += sizeof(curvatureCorrectorK2);

	EEPROM.put(a, pidToSet);
	a += sizeof(pidToSet);
}

void MotionControlSystem::loadParameters()
{
	noInterrupts();

	int a = 0; // Adresse mémoire dans l'EEPROM
	float kp, ki, kd, s;
	uint32_t e, t;

	EEPROM.get(a, positionControlled);
	a += sizeof(positionControlled);
	EEPROM.get(a, leftSpeedControlled);
	a += sizeof(leftSpeedControlled);
	EEPROM.get(a, rightSpeedControlled);
	a += sizeof(rightSpeedControlled);
	EEPROM.get(a, pwmControlled);
	a += sizeof(pwmControlled);

	EEPROM.get(a, s);
	a += sizeof(s);
	EEPROM.get(a, t);
	a += sizeof(t);
	leftMotorBlockingMgr.setTunings(s, t);

	EEPROM.get(a, s);
	a += sizeof(s);
	EEPROM.get(a, t);
	a += sizeof(t);
	rightMotorBlockingMgr.setTunings(s, t);

	EEPROM.get(a, e);
	a += sizeof(e);
	EEPROM.get(a, t);
	a += sizeof(t);
	endOfMoveMgr.setTunings(e, t);

	EEPROM.get(a, maxAcceleration);
	a += sizeof(maxAcceleration);

	EEPROM.get(a, kp);
	a += sizeof(kp);
	EEPROM.get(a, ki);
	a += sizeof(ki);
	EEPROM.get(a, kd);
	a += sizeof(kd);
	translationPID.setTunings(kp, ki, kd);

	EEPROM.get(a, kp);
	a += sizeof(kp);
	EEPROM.get(a, ki);
	a += sizeof(ki);
	EEPROM.get(a, kd);
	a += sizeof(kd);
	leftSpeedPID.setTunings(kp, ki, kd);

	EEPROM.get(a, kp);
	a += sizeof(kp);
	EEPROM.get(a, ki);
	a += sizeof(ki);
	EEPROM.get(a, kd);
	a += sizeof(kd);
	rightSpeedPID.setTunings(kp, ki, kd);

	EEPROM.get(a, curvatureCorrectorK1);
	a += sizeof(curvatureCorrectorK1);
	EEPROM.get(a, curvatureCorrectorK2);
	a += sizeof(curvatureCorrectorK2);

	EEPROM.get(a, pidToSet);
	a += sizeof(pidToSet);

	interrupts();
}

void MotionControlSystem::loadDefaultParameters()
{
	noInterrupts();

	positionControlled = true;
	leftSpeedControlled = true;
	rightSpeedControlled = true;
	pwmControlled = true;

	leftMotorBlockingMgr.setTunings(0.5, 100);
	rightMotorBlockingMgr.setTunings(0.5, 100);
	endOfMoveMgr.setTunings(5000, 100);

	maxAcceleration = 13000;

	translationPID.setTunings(6.5, 0, 250);
	leftSpeedPID.setTunings(2, 0.01, 100);
	rightSpeedPID.setTunings(2, 0.01, 100);
	curvatureCorrectorK1 = 1;
	curvatureCorrectorK2 = 1;

	pidToSet = SPEED;

	interrupts();
}

