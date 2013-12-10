package robot;

import smartMath.Vec2;
import container.Service;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf
 *
 */

public class RobotChrono extends Robot {

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
	public void correction_angle()
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
	public void set_vitesse_translation()
	{
		
	}
	public void set_vitesse_rotation()
	{
		
	}
	
}
