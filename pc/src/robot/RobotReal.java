package robot;

import utils.ConfigInfo;
import utils.Log;
import utils.Config;
import utils.Sleep;
import utils.Vec2;
import hook.Hook;

import java.util.ArrayList;

import buffer.DataForSerialOutput;
import permissions.ReadOnly;
import requete.RequeteSTM;
import requete.RequeteType;
import exceptions.UnableToMoveException;

/**
 * Effectue le lien entre le code et la réalité (permet de parler à la stm, d'interroger les capteurs, etc.)
 * @author pf
 *
 */

public class RobotReal extends Robot
{
	private DataForSerialOutput stm;
	private RequeteSTM requete;
	private boolean matchDemarre = false;
	
	// Constructeur
	public RobotReal(DataForSerialOutput stm, Log log, RequeteSTM requete)
 	{
		super(log);
		this.stm = stm;
		this.requete = requete;
		// On envoie à la STM la vitesse par défaut
		setVitesse(vitesse);
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */
	
	@Override
	public void updateConfig(Config config)
	{
		super.updateConfig(config);
		matchDemarre = config.getBoolean(ConfigInfo.MATCH_DEMARRE);
	}

	@Override
	public void useConfig(Config config)
	{
		super.useConfig(config);
	}
	
/*	@Override
	public void desactiveAsservissement()
	{
		stm.desactiveAsservissement();
	}

	@Override
	public void activeAsservissement()
	{
		stm.activeAsservissement();
	}*/

	/**
	 * Avance d'une certaine distance donnée en mm (méthode bloquante), gestion des hooks
	 * @throws UnableToMoveException 
	 */
	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException
	{
		// Il est nécessaire d'ajouter le hookFinMatch avant chaque appel de stm qui prenne un peu de temps (avancer, tourner, ...)
//		hooks.add(hookFinMatch);
		try {
			synchronized(requete)
			{
				RequeteType type;
				stm.avancer(distance, hooks, mur);
				do {
					requete.wait();
					type = requete.get();
					if(type == RequeteType.BLOCAGE_MECANIQUE)
						throw new UnableToMoveException();
				} while(type != RequeteType.TRAJET_FINI);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	/**
	 * Modifie la vitesse de translation
	 * @param Speed : l'une des vitesses indexées dans enums.
	 */
	@Override
	public void setVitesse(Speed vitesse)
	{
		stm.setSpeed(vitesse);
		log.debug("Modification de la vitesse: "+vitesse);
	}
	
	public void setPositionOrientationSTM(Vec2<ReadOnly> position, double orientation)
	{
		stm.setPositionOrientation(position, orientation);
	}

	public void setPositionOrientationJava(Vec2<ReadOnly> position, double orientation)
	{
		Vec2.copy(position, this.position);
		this.orientation = orientation;
	}

	public void updatePositionOrientation()
	{
	    stm.getPositionOrientation();
	}
    
    /**
	 * Méthode sleep utilisée par les scripts
	 */
	@Override	
	public void sleep(long duree, ArrayList<Hook> hooks)
	{
		Sleep.sleep(duree);
	}

    @Override
    public void stopper()
    {
        try {
			stm.immobilise();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Tourne, quoi. L'angle est absolu
     */
    @Override
    public void tourner(double angle) throws UnableToMoveException
    {
		try {
			synchronized(requete)
			{
				RequeteType type;
				stm.turn(angle);
				do {
					requete.wait();
					type = requete.get();
					if(type == RequeteType.BLOCAGE_MECANIQUE)
						throw new UnableToMoveException();
				} while(type != RequeteType.TRAJET_FINI);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	@Override
    public RobotChrono cloneIntoRobotChrono()
    {
    	RobotChrono rc = new RobotChrono(log);
    	copy(rc);
    	return rc;
    }
    
    // Cette copie est un peu plus lente que les autres car il y a un appel série
    // Néanmoins, on ne fait cette copie qu'une fois par arbre.
    @Override
    public void copy(RobotChrono rc)
    {
        super.copy(rc);
		Vec2.copy(getPosition(), rc.position);
		rc.orientation = getOrientation();
    }

	@Override
    public long getTempsDepuisDebutMatch()
    {
		if(!matchDemarre)
			return 0;
		else
			return System.currentTimeMillis() - dateDebutMatch;
    }
	
	/**
	 * Envoie un ordre à la série. Le protocole est défini dans l'enum ActuatorOrder
	 * @param order l'ordre à envoyer
	 */
	public void useActuator(ActuatorOrder order)
	{
		stm.utiliseActionneurs(order);
/*		try {
			synchronized(requete)
			{
				if(symetrie)
					order = order.getSymmetry();
				stm.utiliseActionneurs(order);
				do {
					requete.wait();
					// TODO gérer le cas du problème d'actionneurs
				} while(requete.type != RequeteType.ACTIONNEURS_FINI);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	@Override
	public Vec2<ReadOnly> getPosition() {
		return position.getReadOnly();
	}

	@Override
	public double getOrientation() {
		return orientation;
	}

}
