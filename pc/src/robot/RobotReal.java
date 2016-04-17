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
import exceptions.FinMatchException;
import exceptions.UnableToMoveException;
import exceptions.UnexpectedObstacleOnPathException;

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
		log.debug("Initialisation de l'odométrie et des constantes d'asservissement");
		int x = config.getInt(ConfigInfo.X_DEPART);
		int y = config.getInt(ConfigInfo.Y_DEPART);
		double o = config.getDouble(ConfigInfo.O_DEPART);
		setPositionOrientationCourbureDirection(new Vec2<ReadOnly>(x, y), o, 0, true);
		stm.initOdoSTM(new Vec2<ReadOnly>(x, y), o);
		/*
		// Envoie des constantes du pid
		stm.setPIDconstVitesseGauche(config.getDouble(ConfigInfo.CONST_KP_VIT_GAUCHE), config.getDouble(ConfigInfo.CONST_KD_VIT_GAUCHE));
		stm.setPIDconstVitesseDroite(config.getDouble(ConfigInfo.CONST_KP_VIT_DROITE), config.getDouble(ConfigInfo.CONST_KD_VIT_DROITE));
		stm.setPIDconstTranslation(config.getDouble(ConfigInfo.CONST_KP_TRANSLATION), config.getDouble(ConfigInfo.CONST_KD_TRANSLATION));
		stm.setPIDconstRotation(config.getDouble(ConfigInfo.CONST_KP_ROTATION), config.getDouble(ConfigInfo.CONST_KD_ROTATION));
		stm.setPIDconstCourbure(config.getDouble(ConfigInfo.CONST_KP_COURBURE), config.getDouble(ConfigInfo.CONST_KD_COURBURE));
		stm.setPIDconstVitesseLineaire(config.getDouble(ConfigInfo.CONST_KP_VIT_LINEAIRE), config.getDouble(ConfigInfo.CONST_KD_VIT_LINEAIRE));
		*/
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
		// TODO gestion haut niveau des problèmes
		try {
			synchronized(requete)
			{
				stm.envoieHooks(hooks);
				RequeteType type;
				stm.avancer(distance);
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

    /**
     * Gère les exceptions, c'est-à-dire les rencontres avec l'ennemi et les câlins avec un mur.
     * @param hooks
     * @param trajectoire_courbe
     * @param marche_arriere
     * @param insiste
     * @throws UnableToMoveException 
     * @throws FinMatchException 
     * @throws ScriptHookException 
     * @throws ChangeDirectionException 
     */
    private void gestionExceptions(Vec2<ReadOnly> consigne, Vec2<ReadOnly> intermediaire, int differenceDistance, boolean marcheAvant, boolean mur) throws UnableToMoveException, FinMatchException
    {
        int nb_iterations_deblocage = 2; // combien de fois on réessaye si on se prend un mur
        boolean recommence;
        do {
            recommence = false;
            try
            {
            	attendStatus();
            } catch (UnableToMoveException e)
            {
                nb_iterations_deblocage--;
                stm.immobilise();
                /*
                 * En cas de blocage, on recule (si on allait tout droit) ou on avance.
                 */
                // Si on s'attendait à un mur, c'est juste normal de se le prendre.
                if(!mur)
                {
                    try
                    {
                        log.warning("On n'arrive plus à avancer. On se dégage");
                        if(marcheAvant)
                            deplacements.moveLengthwise(-distance_degagement_robot);
                        else
                            deplacements.moveLengthwise(distance_degagement_robot);
                        while(!deplacements.isMouvementFini());
                    	recommence = true; // si on est arrivé ici c'est qu'aucune exception n'a été levée
                    	// on peut donc relancer le mouvement
                    } catch (SerialConnexionException e1)
                    {
                        e1.printStackTrace();
                    } catch (BlockedException e1) {
                    	immobilise();
                        log.critical("On n'arrive pas à se dégager.");
					}
                    if(!recommence && nb_iterations_deblocage == 0)
                        throw new UnableToMoveException();
                }
            } catch (UnexpectedObstacleOnPathException e)
            {
            	immobilise();
            	long dateAvant = System.currentTimeMillis();
                log.critical("Détection d'un ennemi! Abandon du mouvement.");
            	while(System.currentTimeMillis() - dateAvant < attente_ennemi_max)
            	{
            		try {
            			detectEnemy(marcheAvant);
            			recommence = true; // si aucune détection
            			break;
            		}
            		catch(UnexpectedObstacleOnPathException e2)
            		{}
            	}
                if(!recommence)
                    throw new UnableToMoveException();
            } catch (WallCollisionDetectedException e) {
            	immobilise();
                if(seulementAngle)
                	throw new UnableToMoveException();
                else
				try {
                	if(marcheAvant)
						deplacements.moveLengthwise(-distance_degagement_robot);
					else
	                    deplacements.moveLengthwise(distance_degagement_robot);
                    try {
						while(!deplacements.isMouvementFini());
					} catch (BlockedException e1) {
						throw new UnableToMoveException();
					}
                	recommence = true;
				} catch (SerialConnexionException e1) {
					e1.printStackTrace();
				}
                	
			}

        } while(recommence); // on recommence tant qu'il le faut

    // Tout s'est bien passé
    }

    /**
     * Bloque jusqu'à ce que la STM donne un status.
     * - Soit le robot a fini le mouvement, et la méthode se termine
     * - Soit le robot est bloqué, et la méthod lève une exception
     * ATTENTION ! Il faut que cette méthode soit appelée dans un synchronized(requete)
     * @throws UnableToMoveException
     */
    void attendStatus() throws UnableToMoveException
    {
		try {
			RequeteType type;
			do {
				if(requete.isEmpty())
					requete.wait();

				type = requete.get();
				if(type == RequeteType.BLOCAGE_MECANIQUE)
					throw new UnableToMoveException();
			} while(type != RequeteType.TRAJET_FINI);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

    }

}
