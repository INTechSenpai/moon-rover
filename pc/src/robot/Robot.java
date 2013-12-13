package robot;

import hook.Hook;
import smartMath.Vec2;
import container.Service;
import utils.Log;
import utils.Read_Ini;

/**
 *  Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author PF
 */

public abstract class Robot implements Service {
	
	public abstract void stopper();
	public abstract void correction_angle(float angle);
	public abstract void tourner(float angle, Hook[] hooks, int nombre_tentatives, boolean sans_lever_exception);
	public abstract void tourner(float angle, int nombre_tentatives, boolean sans_lever_exception);
	public abstract void tourner(float angle, Hook[] hooks, boolean sans_lever_exception);
	public abstract void tourner(float angle, Hook[] hooks, int nombre_tentatives);
	public abstract void tourner(float angle, boolean sans_lever_exception);
	public abstract void tourner(float angle, int nombre_tentatives);
	public abstract void tourner(float angle, Hook[] hooks);
	public abstract void tourner(float angle);
	public abstract void suit_chemin();
	public abstract void set_vitesse_translation(String vitesse);
	public abstract void set_vitesse_rotation(String vitesse);
	public abstract void setPosition(Vec2 position);
	public abstract void setOrientation(float orientation);

	public abstract void tirerBalles(boolean rightSide);
	
	protected Read_Ini config;
	protected Log log;

	/* Ces attributs sont nécessaires à robotvrai et à robotchrono, donc ils sont ici.
	 * Cela regroupe tous les attributs ayant une conséquence dans la stratégie
	 */
	protected Vec2 position = new Vec2(0, 0);
	protected float orientation = 0;
	
	public Robot(Read_Ini config, Log log)
	{
		this.config = config;
		this.log = log;
	}

	public void avancer(int distance, int nbTentatives,
			boolean retenterSiBlocage, boolean sansLeverException) {
		// TODO Auto-generated method stub
		
	}
	
	public void va_au_point(Vec2 point) {
		// TODO Auto-generated method stub
		
	}
	
	protected int conventions_vitesse_translation(String vitesse)
	{
        if(vitesse == "entre_scripts")
        	return 150;
        else if(vitesse == "recal_faible")
            return 90;
        else if(vitesse == "recal_forte")
            return 120;
        else
        {
        	log.warning("Erreur vitesse translation: "+vitesse, this);
        	return 150;
        }
	}

	protected int conventions_vitesse_rotation(String vitesse)
	{
        if(vitesse == "entre_scripts")
        	return 160;
        else if(vitesse == "recal_faible")
            return 120;
        else if(vitesse == "recal_forte")
            return 130;
        else
        {
        	log.warning("Erreur vitesse rotation: "+vitesse, this);
        	return 160;
        }
	}
	
	public Vec2 getPosition() {
		return position;
	}

	public double getOrientation() {
		return orientation;
	}

}
