package robot;

import smartMath.Vec2;
import container.Service;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf
 *
 */

public class RobotChrono extends Robot {

	private float vitesse_mmps;
	private float vitesse_rps;
	
	public RobotChrono(Service pathfinding, Service capteur, Service actionneurs, Service deplacements, Service hookgenerator, Service table, Service config, Service log)
	{
		super(pathfinding, capteur, actionneurs, deplacements, hookgenerator, table, config, log);
	}
	
	// La plupart de ces méthodes resteront vides
	
	public void stopper()
	{
		
	}
	
	@Override
	public void avancer(int distance, int nbTentatives, boolean retenterSiBlocage,
			boolean sansLeverException)
	{
		
	}
	public void correction_angle(float angle)
	{
		
	}
	public void tourner()
	{
		
	}
	public void suit_chemin()
	{
		
	}
	
	@Override
	public void va_au_point(Vec2 point)
	{
		
	}
	public void set_vitesse_translation(String vitesse)
	{
        int pwm_max = conventions_vitesse_translation(vitesse);
        vitesse_mmps = ((float)2500)/((float)613.52 * (float)(Math.pow((double)pwm_max,(double)(-1.034))));
	}
	public void set_vitesse_rotation(String vitesse)
	{
        int pwm_max = conventions_vitesse_rotation(vitesse);
        vitesse_rps = ((float)Math.PI)/((float)277.85 * (float)Math.pow(pwm_max,(-1.222)));
        vitesse_mmps = ((float)2500)/((float)613.52 * (float)(Math.pow((double)pwm_max,(double)(-1.034))));
	}
	
}
