package pathfinding.thetastar;

import container.Service;
import utils.Config;
import utils.Log;

/**
 * Classe qui fournit des objets LocomotionArc à AStar.
 * AStar a besoin de beaucoup de LocomotionArc, et l'instanciation d'un objet est long.
 * Du coup on réutilise les mêmes objets sans devoir en créer tout le temps de nouveaux.
 * @author pf
 *
 */

public class MemoryManager implements Service {

	private static final int nb_instances = 50000;

	private final LocomotionArc[] arcArray = new LocomotionArc[nb_instances];
	protected Log log;
	
	// gamestates_list est triés: avant firstAvailable, les gamestate sont indisponibles, après, ils sont disponibles
	private int firstAvailable;
	
	@Override
	public void updateConfig(Config config)
	{
//		for(int j = 0; j < nb_instances; j++)
//			gamestates_list[j].updateConfig(config);
	}

	@Override
	public void useConfig(Config config)
	{}

	public MemoryManager(Log log)
	{	
		this.log = log;

		firstAvailable = 0;
		// on prépare déjà des arcs
		log.debug("Instanciation de "+nb_instances+" LocomotionArc");

		for(int i = 0; i < nb_instances; i++)
			arcArray[i] = new LocomotionArc();

		log.debug("Instanciation finie");
	}
	
	/**
	 * Donne un arc disponible
	 * @param id_astar
	 * @return
	 */
	public LocomotionArc getNewArc()
	{
		// lève une exception s'il n'y a plus de place
		LocomotionArc out;
		out = arcArray[firstAvailable];
		firstAvailable++;
		return out;
	}
	
	/**
	 * Signale qu'un arc est de nouveau disponible
	 * @param state
	 * @param id_astar
	 */
	private void destroyArc(LocomotionArc arc)
	{
		int indice_state = arc.getIndiceMemoryManager();
		/**
		 * S'il est déjà détruit, on lève une exception
		 */
		if(indice_state >= firstAvailable)
		{
			log.critical("MemoryManager veut détruire un objet déjà détruit !");
		}

		// On inverse dans le Vector les deux gamestates,
		// de manière à avoir toujours un Vector trié.
		firstAvailable--;
		
		LocomotionArc tmp1 = arcArray[indice_state];
		LocomotionArc tmp2 = arcArray[firstAvailable];

		tmp1.setIndiceMemoryManager(firstAvailable);
		tmp2.setIndiceMemoryManager(indice_state);

		arcArray[firstAvailable] = tmp1;
		arcArray[indice_state] = tmp2;
	}
	
	/**
	 * Signale que tous les arcs sont disponibles. Très rapide.
	 * @param id_astar
	 */
	public void empty()
	{
		firstAvailable = 0;
	}
	
	/**
	 * Utilisé par les tests
	 */
	public boolean isMemoryManagerEmpty()
	{
		return firstAvailable == 0;
	}
	
}
