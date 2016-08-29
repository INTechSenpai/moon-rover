package robot;

import utils.Log;
import utils.Config;
import pathfinding.dstarlite.GridSpace;
import serie.BufferOutgoingOrder;
import serie.Ticket;
import exceptions.UnableToMoveException;
import exceptions.UnexpectedObstacleOnPathException;

/**
 * Effectue le lien entre le code et la réalité (permet de parler à la carte bas niveau, d'interroger les capteurs, etc.)
 * @author pf
 *
 */

public class RobotReal extends Robot
{
	private BufferOutgoingOrder serialOutput;
//	private int distanceDegagement;
//	private int tempsAttente;
	
	// Constructeur
	public RobotReal(BufferOutgoingOrder serialOutput, Log log)
 	{
		super(log);
		this.serialOutput = serialOutput;
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */

	@Override
	public void useConfig(Config config)
	{
		super.useConfig(config);
		// TODO
		cinematique = new Cinematique(0, 0, 0, true, 0, 0, 0, Speed.STANDARD);
	}
		
	public void setEnMarcheAvance(boolean enMarcheAvant)
	{
		cinematique.enMarcheAvant = enMarcheAvant;
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
     * Gère les exceptions, c'est-à-dire les rencontres avec l'ennemi et les câlins avec un mur.
     * Cette méthode NE GÈRE PAS les exceptions lors des trajectoires courbes
     * @throws UnexpectedObstacleOnPathException 
     */
    private void gestionExceptions(boolean mur, Ticket t) throws UnableToMoveException, UnexpectedObstacleOnPathException
    {
//        int nb_iterations_deblocage = 1; // combien de fois on réessaye si on se prend un mur
//        int nb_iterations_ennemi = 2000 / tempsAttente; // 2 secondes max

        while(true)
        {
            try
            {
            	attendStatus(t);
            	return; // tout s'est bien passé
            } catch (UnableToMoveException e)
            {
                // Si on s'attendait à un mur, c'est juste normal de se le prendre.
                if(!mur)
                {
//                    try
//                    {
                        /*
                         * En cas de blocage, on recule (si on allait tout droit) ou on avance.
                         */
/*                    	Sleep.sleep(500);
                        log.warning("On n'arrive plus à avancer. On se dégage");
//                        serialOutput.avancerMemeSens(-distanceDegagement, Speed.STANDARD);
                        attendStatus();
                    } catch (UnableToMoveException e1) {
                        log.critical("On n'arrive pas à se dégager.");
					} catch (UnexpectedObstacleOnPathException e1) {
						serialOutput.immobilise();
						e1.printStackTrace();
					}
//                    if(nb_iterations_deblocage-- == 0)*/
                        throw new UnableToMoveException();
                }           
                else
                	return; // on s'est pris un mur, on s'attendait à un mur : tout va bien
            }/* catch (UnexpectedObstacleOnPathException e)
            {
            	serialOutput.suspendMouvement();

            	Sleep.sleep(tempsAttente);
            	// on ne s'est jamais arrêté à cause d'un problème mécanique, on peut donc relancer le mouvement

            	serialOutput.reprendMouvement();
            	//attendStatus();
                if(nb_iterations_ennemi-- == 0)
                    throw new UnableToMoveException();
            }*/
            
        }

    // Tout s'est bien passé
    }

    /**
     * Bloque jusqu'à ce que la carte donne un status.
     * - Soit le robot a fini le mouvement, et la méthode se termine
     * - Soit le robot est bloqué, et la méthod lève une exception
     * ATTENTION ! Il faut que cette méthode soit appelée dans un synchronized(requete)
     * @throws UnableToMoveException
     */
    private void attendStatus(Ticket t) throws UnableToMoveException, UnexpectedObstacleOnPathException
    {
		try {
			Ticket.State o;
			do {
				if(t.isEmpty())
				{
					// Si au bout de 3s le robot n'a toujours rien répondu,
					// on suppose un blocage mécanique
					t.set(Ticket.State.KO);
					t.wait(15000);
				}

				o = t.getAndClear();
				if(o == Ticket.State.KO)
					throw new UnableToMoveException();
/*				else if(o == SerialProtocol.ENNEMI_SUR_CHEMIN)
				{
					log.critical("Ennemi sur le chemin !");
					serialOutput.immobilise();
					Sleep.sleep(4000);
					throw new UnexpectedObstacleOnPathException();
				}*/
			} while(o != Ticket.State.OK);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

    }

	public void setCinematique(Cinematique cinematique)
	{
		cinematique.copy(this.cinematique);
	}

/*	// TODO à virer (utilisé uniquement pour les tests)
	public Cinematique getCinematique()
	{
		return cinematique;
	}
*/
}
