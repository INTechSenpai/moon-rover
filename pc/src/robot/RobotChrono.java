package robot;

import smartMath.Vec2;
import strategie.MemoryManagerProduct;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf
 *
 */

public class RobotChrono extends Robot implements MemoryManagerProduct {

	private float vitesse_mmps;
	private float vitesse_rps;
	
	// Durée en millisecondes
	private long duree = 0;
	
	public RobotChrono(Read_Ini config, Log log)
	{
		super(config, log);
	}
	
	public void setPosition(Vec2 position) {
		this.position = position;
	}
	
	public void setOrientation(float orientation) {
		this.orientation = orientation;
	}
	
	public void setVitesse_rps(float vitesse_rps) {
		this.vitesse_rps = vitesse_rps;
	}

	public void setVitesse_mmps(float vitesse_mmps) {
		this.vitesse_mmps = vitesse_mmps;
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

	// Méthodes propres à RobotChrono
	
	public void reset_compteur()
	{
		duree = 0;
	}
	public long get_compteur()
	{
		return duree;
	}

	@Override
	public MemoryManagerProduct clone(MemoryManagerProduct cloned_robotchrono) {
		((RobotChrono)cloned_robotchrono).setPosition(position);
		((RobotChrono)cloned_robotchrono).setOrientation(orientation);
		((RobotChrono)cloned_robotchrono).setVitesse_rps(vitesse_rps);
		((RobotChrono)cloned_robotchrono).setVitesse_mmps(vitesse_mmps);
		return cloned_robotchrono;
	}

	public MemoryManagerProduct clone()
	{
		RobotChrono cloned_robotchrono = new RobotChrono(config, log);
		return clone(cloned_robotchrono);
	}

	@Override
	public String getNom() {
		return "RobotChrono";
	}
	
}
