package robot;

import utils.Log;
import utils.Config;
import utils.ConfigInfo;
import utils.Sleep;
import utils.Vec2;
import hook.Hook;

import java.util.ArrayList;

import buffer.DataForSerialOutput;
import pathfinding.dstarlite.GridSpace;
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
		
	// Constructeur
	public RobotReal(DataForSerialOutput stm, Log log, RequeteSTM requete)
 	{
		super(log);
		this.stm = stm;
		this.requete = requete;
		// On envoie à la STM la vitesse par défaut
		setVitesse(vitesse);
//		stm.envoieActionneurs();
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */

	@Override
	public void useConfig(Config config)
	{
		super.useConfig(config);
		int x = config.getInt(ConfigInfo.X_DEPART);
		int y = config.getInt(ConfigInfo.Y_DEPART);
		double o = config.getDouble(ConfigInfo.O_DEPART);
		setPositionOrientationCourbureDirection(new Vec2<ReadOnly>(x, y), o, 0, true);
		stm.initOdoSTM(new Vec2<ReadOnly>(x, y), o);
		
		// Envoie des constantes du pid
		stm.setPIDconstVitesseGauche(config.getDouble(ConfigInfo.CONST_KP_VIT_GAUCHE), config.getDouble(ConfigInfo.CONST_KD_VIT_GAUCHE));
		stm.setPIDconstVitesseDroite(config.getDouble(ConfigInfo.CONST_KP_VIT_DROITE), config.getDouble(ConfigInfo.CONST_KD_VIT_DROITE));
		stm.setPIDconstTranslation(config.getDouble(ConfigInfo.CONST_KP_TRANSLATION), config.getDouble(ConfigInfo.CONST_KD_TRANSLATION));
		stm.setPIDconstRotation(config.getDouble(ConfigInfo.CONST_KP_ROTATION), config.getDouble(ConfigInfo.CONST_KD_ROTATION));
		stm.setPIDconstCourbure(config.getDouble(ConfigInfo.CONST_KP_COURBURE), config.getDouble(ConfigInfo.CONST_KD_COURBURE));
		stm.setPIDconstVitesseLineaire(config.getDouble(ConfigInfo.CONST_KP_VIT_LINEAIRE), config.getDouble(ConfigInfo.CONST_KD_VIT_LINEAIRE));
		
	}
		
	public void setEnMarcheAvance(boolean enMarcheAvant)
	{
		this.enMarcheAvant = enMarcheAvant;
	}

	/**
	 * Avance d'une certaine distance donnée en mm (méthode bloquante), gestion des hooks
	 * @throws UnableToMoveException 
	 */
	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException
	{
		try {
			synchronized(requete)
			{
				stm.envoieHooks(hooks);
				RequeteType type;
				stm.avancer(distance, mur);
				do {
					if(requete.isEmpty())
						requete.wait();
					type = requete.get();
					if(type == RequeteType.BLOCAGE_MECANIQUE)
					{
						stm.deleteHooks(hooks);
						throw new UnableToMoveException();
					}
				} while(type != RequeteType.TRAJET_FINI);
				stm.deleteHooks(hooks);
			}
		} catch (InterruptedException e) {
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

	public void setPositionOrientationCourbureDirection(Vec2<ReadOnly> position, double orientation, double courbure, boolean enMarcheAvant)
	{
		Vec2.copy(position, this.position);
		this.orientation = orientation;
		this.courbure = courbure;
		this.enMarcheAvant = enMarcheAvant;
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
		stm.envoieHooks(hooks);
		Sleep.sleep(duree);
		stm.deleteHooks(hooks);
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
					if(requete.isEmpty())
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
	public int getPositionGridSpace()
	{
		return GridSpace.computeGridPoint(position.getReadOnly());
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

}
