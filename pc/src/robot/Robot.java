package robot;

import hook.HookGenerator;
import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;


/**
 *  Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author PF
 */

public abstract class Robot implements Service {
	
	public abstract void stopper();
	public abstract void correction_angle();
	public abstract void tourner();
	public abstract void suit_chemin();
	public abstract void set_vitesse_translation();
	public abstract void set_vitesse_rotation();

	protected Capteur capteur;
	protected Actionneurs actionneurs;
	protected Deplacements deplacements;
	protected HookGenerator hookgenerator;
	protected Table table;
	protected Read_Ini config;
	protected Log log;
	
	public Robot(Service capteur, Service actionneurs, Service deplacements, Service hookgenerator, Service table, Service config, Service log)
	{
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
	
}
