package scripts;

import strategie.GameState;
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
	
	/**
	 * Renvoie le tableau des méta-verions d'un script
	 * @return le tableau des méta-versions possibles
	 */
	public abstract ArrayList<Integer> meta_version(final GameState<?> state);

    /**
     * Renvoie le score que peut fournir une méta-version d'un script
     * @return le score
     */
	public int meta_score(int id_metaversion, GameState<?> state)
	{
	    ArrayList<Integer> versions = this.versions.get(id_metaversion);
        if(versions == null)
            return -1;
        int max = -1;
	    for(Integer v: versions)
	      if(max < 0 || score(v, state) > score(max, state))
	          max = v;
		return score(max, state);
	}

	/**
	 * Renvoie le score que peut fournir une version d'un script
	 * @return le score
	 */
	public abstract int score(int id_version, final GameState<?> state);
	
	/**
	 * Renvoie le tableau des versions associées à une métaversion
	 * @param meta_version
	 * @return
	 */
	public ArrayList<Integer> version_asso(int meta_version)
	{
	    return versions.get(meta_version);
	}

	
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
	 * Retourne la position d'entrée associée à la version id
	 * @param id de la version
	 * @return la position du point d'entrée
	 */
	public abstract PathfindingNodes point_entree(int id);
   
	/**
	 * Exécute ou calcule le script, avec RobotVrai ou RobotChrono
	 * @throws SerialConnexionException 
	 */
	public abstract void execute(int id_version, GameState<?>state) throws UnableToMoveException, SerialConnexionException;

	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs, on se décale du mur, ...
	 */
	abstract protected void termine(GameState<?> state);
	
	public void updateConfig()
	{
	}

}
