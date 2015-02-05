package hook;

import java.util.ArrayList;

import robot.RobotVrai;
import strategie.GameState;
import utils.Log;
import utils.Read_Ini;

/**
 * Classe-mère abstraite des hooks, utilisés pour la programmation évènementielle
 * @author pf
 *
 */

abstract public class Hook
{

	protected ArrayList<Callback> callbacks = new ArrayList<Callback>();
	
	protected Read_Ini config;
	protected Log log;
	protected GameState<RobotVrai> real_state;

	public Hook(Read_Ini config, Log log, GameState<RobotVrai> real_state)
	{
		this.config = config;
		this.log = log;
		this.real_state = real_state;
	}
	
	/**
	 * On peut ajouter un callback à un hook.
	 * Il n'y a pas de méthode pour en retirer, car il n'y en a a priori pas besoin
	 * @param callback
	 */
	public void ajouter_callback(Callback callback)
	{
		callbacks.add(callback);
	}
	
	/**
	 * Quand un hook est déclenché, tous ses callbacks sont exécutés
	 * @return true si ce hook modifie les déplacements du robot
	 */
	protected boolean declencher()
	{
		boolean retour = false;
		
		for(Callback callback : callbacks)
			retour |= callback.appeler();
		return retour;
	}

	/**
	 * Méthode qui sera surchargée par les classes filles.
	 * Elle contient la condition d'appel du hook
	 * @param robot
	 * @return true si ce hook modifie les déplacements du robot, false sinon
	 */
	public abstract boolean evaluate();
	
	/**
	 * On peut supprimer le hook s'il n'y a plus aucun callback déclenchable.
	 * @return
	 */
	public boolean supprimable()
	{
	    for(Callback c: callbacks)
	        if(!c.supprimable())
	            return false;
	    return true;
	}

}

