package scripts;

import strategie.GameState;
import robot.RobotChrono;
import robot.RobotReal;
import utils.Log;
import utils.Config;
import container.Service;
import hook.types.HookFactory;

import java.util.ArrayList;

import enums.PathfindingNodes;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import exceptions.strategie.ScriptException;
/**
 * Classe abstraite dont héritent les différents scripts.
 * S'occupe de robotvrai et robotchrono de manière à ce que ce soit transparent pour les différents scripts
 * @author pf, marsu
 */

public abstract class Script implements Service 
{

	// Ces services resteront toujours les mêmes, on les factorise avec un static
	protected static HookFactory hookgenerator;
	protected static Config config;
	protected static Log log;

	/*
	 * versions.get(meta_id) donne la liste des versions associées aux meta_id
	 */
	protected ArrayList<ArrayList<Integer>> versions = new ArrayList<ArrayList<Integer>>();	
	
	public Script(HookFactory hookgenerator, Config config, Log log)
	{
		Script.hookgenerator = hookgenerator;
		Script.config = config;
		Script.log = log;
	}

	public void agit(int id_version, GameState<RobotReal> state, boolean retenter_si_blocage) throws ScriptException
	{
		try
		{
		    state.robot.setInsiste(retenter_si_blocage);
			execute(id_version, state);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			// le script a échoué, on prévient le haut niveau
			throw new ScriptException();
		}
		finally
		{
			termine(state);
		}

	}
	
	/**
	 * Calcule le temps d'exécution de ce script (grâce à robotChrono)
	 * @return le temps d'exécution
	 * @throws PathfindingException 
	 */
	public long calcule(int meta_id_version, GameState<RobotChrono> state)
	{
		try {
			// on prend la première version de la méta-version
			execute(versions.get(meta_id_version).get(0), state);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return state.robot.get_compteur();
	}	

	/**
	 * Retourne la position d'entrée associée à la version id
	 * @param id de la version
	 * @return la position du point d'entrée
	 */
	public abstract PathfindingNodes point_entree(int id);
   
	/**
	 * Exécute ou calcule le script, avec RobotVrai ou RobotChrono
	 * @throws SerialConnexionException 
	 */
	protected abstract void execute(int id_version, GameState<?>state) throws UnableToMoveException, SerialConnexionException;

	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs, on se décale du mur, ...
	 */
	abstract protected void termine(GameState<?> state);
	
	public void updateConfig()
	{
	}

}
