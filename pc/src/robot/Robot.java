package robot;

import java.util.ArrayList;

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
	
	/*
	 * DÉPLACEMENT HAUT NIVEAU
	 */
	
	public abstract void stopper();
	public abstract void correction_angle(float angle);

	public abstract void tourner(float angle, Hook[] hooks, int nombre_tentatives, boolean sans_lever_exception);
	
	public abstract void avancer(int distance, int nbTentatives, boolean retenterSiBlocage, boolean sansLeverException);
	
	public abstract void suit_chemin(ArrayList<Vec2> chemin, Hook[] hooks, boolean marche_arriere_auto, boolean symetrie_effectuee);
	
	public abstract void set_vitesse_translation(String vitesse);
	public abstract void set_vitesse_rotation(String vitesse);
	
	// Utilisé uniquement à l'initialisation
	public abstract void setPosition(Vec2 position);
	public abstract void setOrientation(float orientation);

	public abstract void va_au_point(Vec2 point, Hook[] hooks, boolean trajectoire_courbe, int nombre_tentatives, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean sans_lever_exception);

	/*
	 * ACTIONNEURS
	 */
	
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

	public void va_au_point(Vec2 point)
	{
		va_au_point(point, null, false, 2, true, false, false);
	}

	public void suit_chemin(ArrayList<Vec2> chemin, boolean marche_arriere_auto, boolean symetrie_effectuee)
	{
		suit_chemin(chemin, null, marche_arriere_auto, symetrie_effectuee);
	}

	public void suit_chemin(ArrayList<Vec2> chemin, Hook[] hooks)
	{
		suit_chemin(chemin, hooks, true, false);
	}

	public void suit_chemin(ArrayList<Vec2> chemin, Hook[] hooks, boolean marche_arriere_auto)
	{
		suit_chemin(chemin, hooks, marche_arriere_auto, false);
	}

	public void suit_chemin(ArrayList<Vec2> chemin, boolean marche_arriere_auto)
	{
		suit_chemin(chemin, null, marche_arriere_auto, false);
	}

	public void suit_chemin(ArrayList<Vec2> chemin)
	{
		suit_chemin(chemin, null, true, false);		
	}

	public void tourner(float angle, int nombre_tentatives, boolean sans_lever_exception)
	{
		tourner(angle, null, nombre_tentatives, sans_lever_exception);
	}

	public void tourner(float angle, Hook[] hooks, boolean sans_lever_exception)
	{
		tourner(angle, null, 2, sans_lever_exception);
	}

	public void tourner(float angle, Hook[] hooks, int nombre_tentatives)
	{
		tourner(angle, hooks, nombre_tentatives, false);				
	}

	public void tourner(float angle, boolean sans_lever_exception)
	{
		tourner(angle, null, 2, sans_lever_exception);
	}

	public void tourner(float angle, int nombre_tentatives)
	{
		tourner(angle, null, nombre_tentatives, false);		
	}

	public void tourner(float angle, Hook[] hooks)
	{
		tourner(angle, hooks, 2, false);
	}

	public void tourner(float angle)
	{
		tourner(angle, null, 2, false);
	}

	public void avancer(int distance, int nbTentatives, boolean retenterSiBlocage)
	{
		this.avancer(distance, nbTentatives, retenterSiBlocage, false);
	}
	
	public void avancer(int distance, int nbTentatives)
	{
		this.avancer(distance, nbTentatives, true, false);
	}

	public void avancer(int distance)
	{
		this.avancer(distance, 2, true, false);
	}

}
