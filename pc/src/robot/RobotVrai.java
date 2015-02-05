package robot;

import robot.hautniveau.ActionneursHautNiveau;
import robot.hautniveau.CapteurSimulation;
import robot.hautniveau.DeplacementsHautNiveau;
import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import utils.Sleep;
import hook.Hook;

import java.util.ArrayList;


import enums.Vitesse;
import exceptions.deplacements.MouvementImpossibleException;

/**
 * Classe qui fournit des déplacements haut niveau
 * @author pf, marsu
 *
 */

public class RobotVrai extends Robot {

	private ActionneursHautNiveau actionneurs;
	@SuppressWarnings("unused")
	private Table table;
	private DeplacementsHautNiveau deplacements;

	// Constructeur
	public RobotVrai(CapteurSimulation capteur_simulation, ActionneursHautNiveau actionneurs, DeplacementsHautNiveau deplacements, Table table, Read_Ini config, Log log)
 	{
		super(config, log);
		this.actionneurs = actionneurs;
		this.deplacements = deplacements;
		this.table = table;
		updateConfig();
		vitesse = Vitesse.ENTRE_SCRIPTS;		
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */
	
	public void updateConfig()
	{
		super.updateConfig();
	}
	
	public void desactiver_asservissement_rotation()
	{
		deplacements.desactiver_asservissement_rotation();
	}

	public void activer_asservissement_rotation()
	{
		deplacements.activer_asservissement_rotation();
	}

	public void recaler()
	{
	    set_vitesse(Vitesse.RECALER);
	    deplacements.recaler();
	}
	
	/**
	 * Avance d'une certaine distance (méthode bloquante), gestion des hooks
	 * @throws MouvementImpossibleException 
	 */
	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws MouvementImpossibleException
	{
		deplacements.avancer(distance, hooks, mur);
	}	

	/**
	 * Modifie la vitesse de translation
	 */
	@Override
	public void set_vitesse(Vitesse vitesse)
	{
        deplacements.set_vitesse_translation(vitesse.PWM_translation);
        deplacements.set_vitesse_rotation(vitesse.PWM_rotation);
		log.debug("Modification de la vitesse: "+vitesse, this);
	}
	
	/*
	 * ACTIONNEURS
	 */

	public void initialiser_actionneurs_deplacements()
	{
	    actionneurs.initialiser_actionneurs();
	    deplacements.initialiser_deplacements();
	}
	/* 
	 * GETTERS & SETTERS
	 */
	@Override
	public void setPosition(Vec2 position)
	{
	    deplacements.setPosition(position);
	}
	
    @Override
	public Vec2 getPosition()
	{
	    return deplacements.getPosition();
	}

	@Override
	public void setOrientation(double orientation)
	{
	    deplacements.setOrientation(orientation);
	}

    @Override
    public double getOrientation()
    {
        return deplacements.getOrientation();
    }

    /**
	 * Méthode sleep utilisée par les scripts
	 */
	@Override	
	public void sleep(long duree)
	{
		Sleep.sleep(duree);
	}

    @Override
    public void stopper()
    {
        deplacements.stopper();
    }

    @Override
    public void tourner(double angle, ArrayList<Hook> hooks, boolean mur) throws MouvementImpossibleException
    {
        deplacements.tourner(angle, hooks, mur);
    }
    
    @Override
    public void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks) throws MouvementImpossibleException
    {
        deplacements.suit_chemin(chemin, hooks);
    }
    @Override
    public void copy(RobotChrono rc)
    {
        super.copy(rc);
        getPositionFast().copy(rc.position);
        rc.orientation = getOrientationFast();
    }

	@Override
	public Vec2 getPositionFast() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getOrientationFast() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInsiste(boolean insiste) {
		// TODO Auto-generated method stub
		
	}

}
