package robot;

import smartMath.Vec2;

/**
 *  Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author PF
 */

public abstract class Robot {
	
	public abstract void stopper();
	public abstract void correction_angle();
	public abstract void tourner();
	public abstract void suit_chemin();
	public abstract void set_vitesse_translation();
	public abstract void set_vitesse_rotation();
	
	public void avancer(int distance, int nbTentatives,
			boolean retenterSiBlocage, boolean sansLeverException) {
		// TODO Auto-generated method stub
		
	}
	
	public void va_au_point(Vec2 point) {
		// TODO Auto-generated method stub
		
	}
	
}
