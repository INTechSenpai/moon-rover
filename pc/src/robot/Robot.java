package robot;

import java.util.Hashtable;
import java.util.Map;

import pathfinding.Pathfinding;
import robot.cartes.Actionneurs;
import robot.cartes.Capteur;
import robot.cartes.Deplacements;
import smartMath.Vec2;
import container.Service;
import hook.HookGenerator;
import table.Table;
import utils.Log;
import utils.Read_Ini;

/**
 *  Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author PF
 */

public abstract class Robot implements Service {
	
	public abstract void stopper();
	public abstract void correction_angle(float angle);
	public abstract void tourner();
	public abstract void suit_chemin();
	public abstract void set_vitesse_translation(String vitesse);
	public abstract void set_vitesse_rotation(String vitesse);
	
	protected Pathfinding pathfinding;
	protected Capteur capteur;
	protected Actionneurs actionneurs;
	protected Deplacements deplacements;
	protected HookGenerator hookgenerator;
	protected Table table;
	protected Read_Ini config;
	protected Log log;

	/* Ces attributs sont nécessaires à robotvrai et à robotchrono, donc ils sont ici.
	 * Cela regroupe tous les attributs ayant une conséquence dans la stratégie
	 */
	protected Vec2 position;
	protected float orientation;
	
	public Robot(Service pathfinding, Service capteur, Service actionneurs, Service deplacements, Service hookgenerator, Service table, Service config, Service log)
	{
		this.pathfinding = (Pathfinding) pathfinding;
		this.capteur = (Capteur) capteur;
		this.actionneurs = (Actionneurs) actionneurs;
		this.deplacements = (Deplacements) deplacements;
		this.hookgenerator = (HookGenerator) hookgenerator;
		this.table = (Table) table;
		this.config = (Read_Ini) config;
		this.log = (Log) log;
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
