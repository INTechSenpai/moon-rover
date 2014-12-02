package robot;

import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Config;
import utils.Sleep;
import hook.Hook;

import java.util.ArrayList;

import enums.Speed;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;

/**
 * Effectue le lien entre le code et la réalité (permet de parler aux actionneurs, d'interroger les capteurs, etc.)
 * @author pf, marsu
 *
 */

public class RobotReal extends Robot
{

	@SuppressWarnings("unused")
	private Table table;
	private Locomotion deplacements;

	// Constructeur
	public RobotReal( Locomotion deplacements, Table table, Config config, Log log)
 	{
		super(config, log);
		this.deplacements = deplacements;
		this.table = table;
		updateConfig();
		vitesse = Speed.BETWEEN_SCRIPTS;		
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
		try {
			deplacements.getLocomotionCardWrapper().disableRotationnalFeedbackLoop();
		} catch (SerialConnexionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void activer_asservissement_rotation()
	{
		try {
			deplacements.getLocomotionCardWrapper().enableRotationnalFeedbackLoop();
		} catch (SerialConnexionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void recaler()
	{
	    set_vitesse(Speed.READJUSTMENT);
	    deplacements.readjust();
	}
	
	/**
	 * Avance d'une certaine distance donnée en mm (méthode bloquante), gestion des hooks
	 * @throws UnableToMoveException 
	 */
	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException
	{
		deplacements.moveForward(distance, hooks, mur);
	}	

	/**
	 * Modifie la vitesse de translation
	 * @param Speed : l'une des vitesses indexées dans enums.
	 * 
	 */
	@Override
	public void set_vitesse(Speed vitesse)
	{
        deplacements.setTranslationnalSpeed(vitesse.PWMTranslation);
        deplacements.setRotationnalSpeed(vitesse.PWMTotation);
		log.debug("Modification de la vitesse: "+vitesse, this);
	}
	
	/*
	 * ACTIONNEURS
	 */
	
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
        deplacements.immobilise();
    }

    @Override
    public void tourner(double angle, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException
    {
        deplacements.turn(angle, hooks, mur);
    }
    
    @Override
    public void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks) throws UnableToMoveException
    {
        deplacements.followPath(chemin, hooks);
    }
    @Override
    public void copy(RobotChrono rc)
    {
        super.copy(rc);
        getPositionFast().copy(rc.position);
        rc.orientation = getOrientationFast();
    }

    // TODO utilité ?
	@Override
	public Vec2 getPositionFast()
	{
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
