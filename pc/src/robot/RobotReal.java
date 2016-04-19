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
	private int distanceDegagement;
	private int tempsAttente;
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
		distanceDegagement = config.getInt(ConfigInfo.DISTANCE_DEGAGEMENT_ROBOT);
		tempsAttente = config.getInt(ConfigInfo.ATTENTE_ENNEMI_PART);
		log.debug("Initialisation de l'odométrie et des constantes d'asservissement");
		int x = config.getInt(ConfigInfo.X_DEPART);
		int y = config.getInt(ConfigInfo.Y_DEPART);
		double o = config.getDouble(ConfigInfo.O_DEPART);
		setPositionOrientationCourbureDirection(new Vec2<ReadOnly>(x, y), o, 0, true);
		stm.initOdoSTM(new Vec2<ReadOnly>(x, y), o);
		/*// TODO envoyer le pid quand les valeurs sont trouvées
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
		try {
			synchronized(requete)
			{
				stm.envoieHooks(hooks);
				stm.avancer(distance);
				gestionExceptions(mur);
			}
		}
		finally
		{
			stm.deleteHooks(hooks);
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

    /**
     * Tourne, quoi. L'angle est absolu
     */
    @Override
    public void tourner(double angle) throws UnableToMoveException
    {
		synchronized(requete)
		{
			stm.turn(angle);
			gestionExceptions(false);
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
	}

    /**
     * Gère les exceptions, c'est-à-dire les rencontres avec l'ennemi et les câlins avec un mur.
     * Cette méthode NE GÈRE PAS les exceptions lors des trajectoires courbes
     */
    private void gestionExceptions(boolean mur) throws UnableToMoveException
    {
        int nb_iterations_deblocage = 2; // combien de fois on réessaye si on se prend un mur
        int nb_iterations_ennemi = 2000 / tempsAttente; // 2 secondes max
        while(true)
        {
            try
            {
            	attendStatus();
            	return; // tout s'est bien passé
            } catch (UnableToMoveException e)
            {
                // Si on s'attendait à un mur, c'est juste normal de se le prendre.
                if(!mur)
                {
                    try
                    {
                        /*
                         * En cas de blocage, on recule (si on allait tout droit) ou on avance.
                         */

                        log.warning("On n'arrive plus à avancer. On se dégage");
                        stm.avancerMemeSens(-distanceDegagement);
                        attendStatus();
                    } catch (UnableToMoveException e1) {
                        log.critical("On n'arrive pas à se dégager.");
					} catch (UnexpectedObstacleOnPathException e1) {
						stm.immobilise();
						e1.printStackTrace();
					}
                    if(nb_iterations_deblocage-- == 0)
                        throw new UnableToMoveException();
                }           
                else
                	return; // on s'est pris un mur, on s'attendait à un mur : tout va bien
            } catch (UnexpectedObstacleOnPathException e)
            {
            	stm.suspendMouvement();
            	Sleep.sleep(tempsAttente);
            	// on ne s'est jamais arrêté à cause d'un problème mécanique, on peut donc relancer le mouvement
            	stm.reprendMouvement();
            	//attendStatus();
                if(nb_iterations_ennemi-- == 0)
                    throw new UnableToMoveException();
            }
            
        }

    // Tout s'est bien passé
    }

    /**
     * Bloque jusqu'à ce que la STM donne un status.
     * - Soit le robot a fini le mouvement, et la méthode se termine
     * - Soit le robot est bloqué, et la méthod lève une exception
     * ATTENTION ! Il faut que cette méthode soit appelée dans un synchronized(requete)
     * @throws UnableToMoveException
     */
    void attendStatus() throws UnableToMoveException, UnexpectedObstacleOnPathException
    {
		try {
			RequeteType type;
			do {
				if(requete.isEmpty())
					requete.wait();

				type = requete.get();
				if(type == RequeteType.BLOCAGE_MECANIQUE)
					throw new UnableToMoveException();
				else if(type == RequeteType.ENNEMI_SUR_CHEMIN)
					throw new UnexpectedObstacleOnPathException();
			} while(type != RequeteType.TRAJET_FINI);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

    }

}
