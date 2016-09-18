#include "MotionControlSystem.h"


MotionControlSystem::MotionControlSystem() : 
motor(), 
leftMotorEncoder(PIN_A_LEFT_MOTOR_ENCODER, PIN_B_LEFT_MOTOR_ENCODER),
rightMotorEncoder(PIN_A_RIGHT_MOTOR_ENCODER, PIN_B_RIGHT_MOTOR_ENCODER),
leftFreeEncoder(PIN_A_LEFT_BACK_ENCODER, PIN_B_LEFT_BACK_ENCODER),
rightFreeEncoder(PIN_A_RIGHT_BACK_ENCODER, PIN_B_RIGHT_BACK_ENCODER),
direction(DirectionController::Instance()),
rightSpeedPID(&currentRightSpeed, &rightPWM, &rightSpeedSetpoint),
leftSpeedPID(&currentLeftSpeed, &leftPWM, &leftSpeedSetpoint),
translationPID(&currentDistance, &movingSpeedSetpoint, &translationSetpoint),
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
{
	updateSpeedAndPosition();
	updateTrajectoryIndex();

	/* Code d'asservissement */

	manageStop();
	manageBlocking();

	
	/*
	// Pour le calcul de la vitesse instantanée :
	static int32_t previousLeftTicks = 0;
	static int32_t previousRightTicks = 0;

	// Récupération des informations des encodeurs (nombre de ticks)
	int32_t rightTicks = -(rightMotorEncoder.read());
	int32_t leftTicks = leftMotorEncoder.read();

	currentLeftSpeed = (leftTicks - previousLeftTicks) * FREQ_ASSERV; // (nb-de-tick-passés)*(freq_asserv) (ticks/sec)
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
		{// Si la trajectoire est terminée ou inexistante : pas de mouvement
			movingSpeedSetpoint = 0;
			leftSpeedSetpoint = 0;
			rightSpeedSetpoint = 0;
		}
		else
		{

			//* Vérification de fin de mouvement élémentaire *//*

			if (currentTrajectory[currentMove].getBendRadiusTicks() == 0)
			{// Cas d'un mouvement purement rotatif
				if ( (currentTrajectory[currentMove].getLengthTicks() >= 0 && currentAngle >= rotationSetpoint)
				  || (currentTrajectory[currentMove].getLengthTicks() <  0 && currentAngle <= rotationSetpoint) )
				{// Rotation terminée
					nextMove();
				}
			}
			else
			{// Cas d'une trajectoire courbe standard
				if ( (currentTrajectory[currentMove].getLengthTicks() >= 0 && currentDistance >= translationSetpoint)
				  || (currentTrajectory[currentMove].getLengthTicks() <  0 && currentDistance <= translationSetpoint) )
				{// Translation terminée
					nextMove();
				}
			}


			// On vérifie de nouveau l'existance du mouvement élémentaire courant
			if (currentMove >= currentTrajectory.size())
			{// Si la trajectoire est terminée ou inexistante : pas de mouvement
				movingSpeedSetpoint = 0;
				leftSpeedSetpoint = 0;
				rightSpeedSetpoint = 0;
			}
			else
			{
				int32_t maxSpeed = currentTrajectory[currentMove].getSpeedTicks_S();


				//  Si il est spécifié explicitement que ce mouvement élémentaire doit se terminer
				//	à l'arrêt ("stopAfterMove" est alors passé à TRUE).
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


				//* Limitation des variations de movingSpeedSetpoint (limitation de l'accélération) *//*
				if (movingSpeedSetpoint - previousMovingSpeed > maxAcceleration / FREQ_ASSERV)
				{
					movingSpeedSetpoint = previousMovingSpeed + maxAcceleration / FREQ_ASSERV;
				}
				else if (previousMovingSpeed - movingSpeedSetpoint > maxAcceleration / FREQ_ASSERV)
				{
					movingSpeedSetpoint = previousMovingSpeed - maxAcceleration / FREQ_ASSERV;
				}
				previousMovingSpeed = movingSpeedSetpoint;





				//* Calcul des vitesses des deux moteurs à partir de movingSpeedSetpoint et de bendRadius *//*
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

void MotionControlSystem::updateSpeedAndPosition() 
{
	// Check errors of encoder to detect external blocking
}

void MotionControlSystem::updateTrajectoryIndex()
{

}

void MotionControlSystem::manageStop()
{
	endOfMoveMgr.compute();
	if (endOfMoveMgr.isStopped())
	{
		movingState = STOPPED;
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
	}
}



/*
	Gestion des déplacements
*/

void MotionControlSystem::addTrajectoryPoint(const TrajectoryPoint & trajPoint, uint8_t index)
{
	// currentTrajectory n'a pas besoin d'être interrupt-safe
	// mettre à jour nextStopPoint : doit être interrupt-safe
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
	// rappel : l'écriture de movingState doit être interrupt-safe 
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

