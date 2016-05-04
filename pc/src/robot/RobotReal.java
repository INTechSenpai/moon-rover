package robot;

import utils.Log;
import utils.Config;
import utils.ConfigInfo;
import utils.Sleep;
import utils.Vec2;
import utils.permissions.ReadOnly;
import hook.Hook;

import java.util.ArrayList;

import pathfinding.dstarlite.GridSpace;
import robot.actuator.ActuatorOrder;
import robot.requete.RequeteSTM;
import robot.requete.RequeteType;
import serie.DataForSerialOutput;
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
	
	private boolean sableDevant = false;
	private boolean sableDerriere = false;
	
	// Constructeur
	public RobotReal(DataForSerialOutput stm, Log log, RequeteSTM requete)
 	{
		super(log);
		this.stm = stm;
		this.requete = requete;
		// On envoie à la STM la vitesse par défaut
//		setVitesse(cinematique.vitesse);
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
		cinematique = new Cinematique(x, y, o, true, 0, 0, 0, Speed.STANDARD);
/*		stm.initOdoSTM(new Vec2<ReadOnly>(x, y), o);
		stm.initOdoSTM(new Vec2<ReadOnly>(x, y), o);
		stm.initOdoSTM(new Vec2<ReadOnly>(x, y), o);*/
		
		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_GAUCHE_VERR2);
		stm.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_DROIT_VERR2);
		stm.utiliseActionneurs(ActuatorOrder.AX12_AVANT_GAUCHE_FERME);
		stm.utiliseActionneurs(ActuatorOrder.AX12_AVANT_DROIT_FERME);
		stm.utiliseActionneurs(ActuatorOrder.AX12_POISSON_HAUT);
		stm.utiliseActionneurs(ActuatorOrder.AX12_POISSON_FERME);
		// Envoie des constantes du pid
		stm.setPIDconstVitesseGauche(config.getDouble(ConfigInfo.CONST_KP_VIT_GAUCHE), config.getDouble(ConfigInfo.CONST_KI_VIT_GAUCHE), config.getDouble(ConfigInfo.CONST_KD_VIT_GAUCHE));
		stm.setPIDconstVitesseDroite(config.getDouble(ConfigInfo.CONST_KP_VIT_DROITE), config.getDouble(ConfigInfo.CONST_KI_VIT_DROITE), config.getDouble(ConfigInfo.CONST_KD_VIT_DROITE));
		stm.setPIDconstTranslation(config.getDouble(ConfigInfo.CONST_KP_TRANSLATION), config.getDouble(ConfigInfo.CONST_KI_TRANSLATION), config.getDouble(ConfigInfo.CONST_KD_TRANSLATION));
		stm.setPIDconstRotation(config.getDouble(ConfigInfo.CONST_KP_ROTATION), config.getDouble(ConfigInfo.CONST_KI_ROTATION), config.getDouble(ConfigInfo.CONST_KD_ROTATION));
		stm.setPIDconstCourbure(config.getDouble(ConfigInfo.CONST_KP_COURBURE), config.getDouble(ConfigInfo.CONST_KI_COURBURE), config.getDouble(ConfigInfo.CONST_KD_COURBURE));
		stm.setPIDconstVitesseLineaire(config.getDouble(ConfigInfo.CONST_KP_VIT_LINEAIRE), config.getDouble(ConfigInfo.CONST_KI_VIT_LINEAIRE), config.getDouble(ConfigInfo.CONST_KD_VIT_LINEAIRE));
	}
		
	public void setEnMarcheAvance(boolean enMarcheAvant)
	{
		cinematique.enMarcheAvant = enMarcheAvant;
	}

    public void avancerB(int distance, boolean mur, Speed vitesse)
    {
    	try {
    		avancer(distance, new ArrayList<Hook>(), mur, vitesse);
		} catch (UnableToMoveException e) {}	
    }

    public void avancerB(int distance, ArrayList<Hook> hooks, Speed vitesse)
    {
    	try {
    		avancer(distance, hooks, false, vitesse);
		} catch (UnableToMoveException e) {}	
    }

	/**
	 * Avance d'une certaine distance donnée en mm (méthode bloquante), gestion des hooks
	 * @throws UnableToMoveException 
	 */
	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur, Speed vitesse) throws UnableToMoveException
	{
		try {
			synchronized(requete)
			{
				stm.envoieHooks(hooks);
				stm.avancer(distance, mur ? Speed.INTO_WALL : vitesse);
				gestionExceptions(mur);
			}
		}
		finally
		{
			stm.deleteHooks(hooks);
		}
	}	

    public void vaAuPointBasNiveau(Vec2<ReadOnly> point, ArrayList<Hook> hooks, Speed vitesse) throws UnableToMoveException
	{
		try {
			synchronized(requete)
			{
				stm.envoieHooks(hooks);
				stm.vaAuPoint(point, vitesse);
				gestionExceptions(true);
			}
		}
		finally
		{
			stm.deleteHooks(hooks);
		}
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

	public void vaAuPointB(Vec2<ReadOnly> point, ArrayList<Hook> hooks, Speed vitesse, boolean marcheAvant)
	{
    	try {
    		vaAuPoint(point, hooks, vitesse, marcheAvant);
		} catch (UnableToMoveException e) {}	
	}

	public void vaAuPointB(Vec2<ReadOnly> point, Speed vitesse, boolean marcheAvant)
	{
    	try {
    		vaAuPoint(point, new ArrayList<Hook>(), vitesse, marcheAvant);
		} catch (UnableToMoveException e) {}	
	}

	public void vaAuPoint(Vec2<ReadOnly> point, ArrayList<Hook> hooks, Speed vitesse, boolean marcheAvant) throws UnableToMoveException
	{
		int distance;
		double angle;
		synchronized(cinematique)
		{
			if(symetrie)
				point.x = -point.x;
			log.debug("position actuelle : "+cinematique.position);
			log.debug("position cible : "+point);

			distance = (int)cinematique.position.distance(point);
			if(!marcheAvant)
				distance = -distance;
			
			angle = Math.atan2(point.y-cinematique.position.y, 
					point.x-cinematique.position.x);
			if(!marcheAvant)
				angle += Math.PI;
		}
		if(Math.abs(distance) > 20)
			tournerSansSym(angle, vitesse);
		Sleep.sleep(200);
		vaAuPointBasNiveau(point, hooks, vitesse);
		
	}
	
    public void tournerB(double angle, Speed vitesse)
    {
    	try {
			tourner(angle, vitesse);
		} catch (UnableToMoveException e) {}
    }

	
    /**
     * Tourne, quoi. L'angle est absolu
     */
    @Override
    public void tourner(double angle, Speed vitesse) throws UnableToMoveException
    {
		synchronized(requete)
		{
			if(symetrie)
				angle = Math.PI - angle;
			stm.turn(angle, vitesse);
			gestionExceptions(false);
		}
    }

    private void tournerSansSym(double angle, Speed vitesse) throws UnableToMoveException
    {
		synchronized(requete)
		{
//			if(symetrie)
//				angle = 2*cinematique.orientation - angle;
			stm.turn(angle, vitesse);
			gestionExceptions(false);
		}
    }

	@Override
	public int getPositionGridSpace()
	{
		return GridSpace.computeGridPoint(cinematique.getPosition());
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
//        int nb_iterations_deblocage = 1; // combien de fois on réessaye si on se prend un mur
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
                    	Sleep.sleep(500);
                        log.warning("On n'arrive plus à avancer. On se dégage");
                        stm.avancerMemeSens(-distanceDegagement, Speed.STANDARD);
                        attendStatus();
                    } catch (UnableToMoveException e1) {
                        log.critical("On n'arrive pas à se dégager.");
					} catch (UnexpectedObstacleOnPathException e1) {
						stm.immobilise();
						e1.printStackTrace();
					}
//                    if(nb_iterations_deblocage-- == 0)
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
				{
					// Si au bout de 3s le robot n'a toujours rien répondu,
					// on suppose un blocage mécanique
					requete.set(RequeteType.BLOCAGE_MECANIQUE_VITESSE);
					requete.wait(7000);
				}

				type = requete.getAndClear();
				if(type == RequeteType.BLOCAGE_MECANIQUE_VITESSE)
					throw new UnableToMoveException();
				else if(type == RequeteType.BLOCAGE_MECANIQUE_ACCELERATION)
				{
					Sleep.sleep(1000);
					throw new UnableToMoveException();
				}
				else if(type == RequeteType.ENNEMI_SUR_CHEMIN)
					throw new UnexpectedObstacleOnPathException();
			} while(type != RequeteType.TRAJET_FINI);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

    }

	public void setCinematique(Cinematique cinematique)
	{
		cinematique.copy(this.cinematique);
	}

	// TODO à virer (utilisé uniquement pour les tests)
	public Cinematique getCinematique()
	{
		return cinematique;
	}
	
	public void setSable(boolean devant, boolean arriere)
	{
		sableDevant = devant;
		sableDevant = arriere;
	}

	public boolean getSableDevant()
	{
		return sableDevant;
	}

	public boolean getSableDerriere()
	{
		return sableDerriere;
	}

}
