
/**
 *  Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author PF
 */

abstract class Robot {
	
	abstract void stopper();
	abstract void avancer();
	abstract void correction_angle();
	abstract void tourner();
	abstract void suit_chemin();
	abstract void va_au_point();
	abstract void set_vitesse_translation();
	abstract void set_vitesse_rotation();
	
}
