package robot;

import robot.cardsWrappers.ActuatorCardWrapper;
import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Config;
import utils.Sleep;
import hook.Hook;

import java.util.ArrayList;

import enums.ActuatorOrder;
import enums.HauteurBrasClap;
import enums.PathfindingNodes;
import enums.Side;
import enums.Speed;
import exceptions.FinMatchException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;

/**
 * Effectue le lien entre le code et la réalité (permet de parler aux actionneurs, d'interroger les capteurs, etc.)
 * @author pf, marsu
 *
 */

public class RobotReal extends Robot
{
//	private Table table;
	private Locomotion deplacements;
	private ActuatorCardWrapper actionneurs;

	// Constructeur
	public RobotReal(ActuatorCardWrapper actuator, Locomotion deplacements, Table table, Config config, Log log)
 	{
		super(config, log);
		this.actionneurs = actuator;
		this.deplacements = deplacements;
//		this.table = table;
		updateConfig();
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */
	
	public void updateConfig()
	{
		super.updateConfig();
	}
	
	
	public void desactiver_asservissement_rotation() throws FinMatchException
	{
		try {
			deplacements.getLocomotionCardWrapper().disableRotationnalFeedbackLoop();
		} catch (SerialConnexionException e) {
			// On peut pas faire grand chose...
			e.printStackTrace();
		}
	}

	public void activer_asservissement_rotation() throws FinMatchException
	{
		try {
			deplacements.getLocomotionCardWrapper().enableRotationnalFeedbackLoop();
		} catch (SerialConnexionException e) {
			// On peut pas faire grand chose...
			e.printStackTrace();
		}
	}

	public void recaler() throws FinMatchException
	{
	    set_vitesse(Speed.READJUSTMENT);
	    deplacements.readjust();
	}
	
	/**
	 * Avance d'une certaine distance donnée en mm (méthode bloquante), gestion des hooks
	 * @throws UnableToMoveException 
	 * @throws FinMatchException 
	 */
	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException, FinMatchException
	{
		deplacements.moveLengthwise(distance, hooks, mur);
	}	

	/**
	 * Modifie la vitesse de translation
	 * @param Speed : l'une des vitesses indexées dans enums.
	 * @throws FinMatchException 
	 * 
	 */
	@Override
	public void set_vitesse(Speed vitesse) throws FinMatchException
	{
        deplacements.setTranslationnalSpeed(vitesse.PWMTranslation);
        deplacements.setRotationnalSpeed(vitesse.PWMRotation);
		log.debug("Modification de la vitesse: "+vitesse, this);
	}
	
	/*
	 * ACTIONNEURS
	 */
	
	/* 
	 * GETTERS & SETTERS
	 */
	@Override
	public void setPosition(Vec2 position) throws FinMatchException
	{
	    deplacements.setPosition(position);
	}
	
    @Override
	public Vec2 getPosition() throws FinMatchException
	{
	    return deplacements.getPosition();
	}

	@Override
	public void setOrientation(double orientation) throws FinMatchException
	{
	    deplacements.setOrientation(orientation);
	}

    @Override
    public double getOrientation() throws FinMatchException
    {
        return deplacements.getOrientation();
    }

    /**
	 * Méthode sleep utilisée par les scripts
     * @throws FinMatchException 
	 */
	@Override	
	public void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException
	{
		Sleep.sleep(duree);
		if(hooks != null)
			for(Hook hook: hooks)
				hook.evaluate();
	}

    @Override
    public void stopper() throws FinMatchException
    {
        deplacements.immobilise();
    }

    @Override
    public void tourner(double angle, boolean mur) throws UnableToMoveException, FinMatchException
    {
        deplacements.turn(angle, null, mur);
    }
    
    @Override
    public void suit_chemin(ArrayList<PathfindingNodes> chemin, ArrayList<Hook> hooks) throws UnableToMoveException, FinMatchException
    {
        deplacements.followPath(chemin, hooks);
    }
    
	@Override
    public RobotChrono cloneIntoRobotChrono() throws FinMatchException
    {
    	RobotChrono rc = new RobotChrono(config, log);
    	copy(rc);
    	return rc;
    }
    
    // Cette copie est un peu plus lente que les autres car il y a un appel série
    // Néanmoins, on ne fait cette copie qu'une fois par arbre.
    @Override
    public void copy(RobotChrono rc) throws FinMatchException
    {
        super.copy(rc);
        getPosition().copy(rc.position);
        rc.orientation = getOrientation();
    }

	@Override
	public void setInsiste(boolean insiste) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public long getTempsDepuisDebutMatch()
    {
    	return System.currentTimeMillis() - Config.getDateDebutMatch();
    }
	
	public void leverDeuxTapis(boolean needToSleep) throws FinMatchException
	{
		try {
			actionneurs.useActuator(ActuatorOrder.LEVE_TAPIS_GAUCHE);
			actionneurs.useActuator(ActuatorOrder.LEVE_TAPIS_DROIT);
			if(needToSleep)
				leverDeuxTapisSleep();
		} catch (SerialConnexionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void poserDeuxTapis(boolean needToSleep) throws FinMatchException
	{
		try {
			actionneurs.useActuator(ActuatorOrder.BAISSE_TAPIS_DROIT);
			actionneurs.useActuator(ActuatorOrder.BAISSE_TAPIS_DROIT);
			tapisRougePose(2);
			if(needToSleep)
				poserDeuxTapisSleep();
		} catch (SerialConnexionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void bougeBrasClap(Side cote, HauteurBrasClap hauteur, boolean needToSleep) throws SerialConnexionException, FinMatchException
	{
		ActuatorOrder order = bougeBrasClapOrder(cote, hauteur);
		actionneurs.useActuator(order);
		if(needToSleep)
			bougeBrasClapSleep(order);
	}

}
