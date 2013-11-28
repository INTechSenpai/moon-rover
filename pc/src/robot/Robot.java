package robot;
/**
 *  Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author PF
 */

public abstract class Robot {
	
	public abstract void stopper();
	public abstract void avancer();
	public abstract void correction_angle();
	public abstract void tourner();
	public abstract void suit_chemin();
	public abstract void va_au_point();
	public abstract void set_vitesse_translation();
	public abstract void set_vitesse_rotation();
	
}
