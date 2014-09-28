package scripts;

import smartMath.Vec2;
import strategie.GameState;
import robot.RobotVrai;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import hook.sortes.HookGenerator;

import java.util.ArrayList;

import exceptions.deplacements.MouvementImpossibleException;
import exceptions.serial.SerialException;
import exceptions.ScriptException;
/**
 * Classe abstraite dont héritent les différents scripts.
 * S'occupe le robotvrai et robotchrono de manière à ce que ce soit transparent pour les différents scripts
 * @author pf, marsu
 */

public abstract class Script implements Service 
{

	// Ces services resteront toujours les mêmes, on les factorise avec un static
	protected static HookGenerator hookgenerator;
	protected static Read_Ini config;
	protected static Log log;

	/*
	 * versions.get(meta_id) donne la liste des versions associées aux meta_id
	 */
	protected ArrayList<ArrayList<Integer>> versions = new ArrayList<ArrayList<Integer>>();	
	
	public Script(HookGenerator hookgenerator, Read_Ini config, Log log)
	{
		Script.hookgenerator = hookgenerator;
		Script.config = config;
		Script.log = log;
	}
		
	/**
	 * Exécute vraiment un script
	 */
	public void agit(int id_version, GameState<RobotVrai> state, boolean retenter_si_blocage) throws ScriptException
	{
	}
	
	/**
	 * Calcule le temps d'exécution de ce script (grâce à robotChrono)
	 * @return le temps d'exécution
	 * @throws PathfindingException 
	 */
	public long calcule()
	{
		return 42;
	}	

	/**
	 * Retourne la position d'entrée associée à la version id
	 * @param id de la version
	 * @return la position du point d'entrée
	 */
	public abstract Vec2 point_entree(int id);
   
	/**
	 * Renvoie le score que peut fournir une version d'un script
	 * @return le score
	 */
	public abstract int score(int id_version, final GameState<?> state);
	
	/**
	 * Exécute le script, avec RobotVrai ou RobotChrono
	 * @throws SerialException 
	 */
	protected void execute() throws MouvementImpossibleException, SerialException
	{
	}

	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs.
	 */
	abstract protected void termine(GameState<?> state);
	
	public void updateConfig()
	{
	}

}
