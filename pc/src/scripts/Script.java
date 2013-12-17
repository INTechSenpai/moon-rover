package scripts;
import pathfinding.Pathfinding;
import smartMath.Vec2;
import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.HookGenerator;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotVrai;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import hook.methodes.TakeFire;

import java.util.ArrayList;

import exception.MouvementImpossibleException;
/**
 * Classe abstraite dont hériteront les différents scripts. S'occupe le robotvrai et robotchrono de manière à ce que ce soit transparent pour les différents scripts
 * @author pf
 */

public abstract class Script implements Service {

	// Ces services resteront toujours les mêmes, on les factorise avec un static
	protected static HookGenerator hookgenerator;
	protected static Read_Ini config;
	protected static Log log;
	private Pathfinding pathfinding;
	
	protected static ArrayList<Hook> hookfeu;
	
	public Script(Pathfinding pathfinding, HookGenerator hookgenerator, Read_Ini config, Log log, RobotVrai robotvrai)
	{
		this.pathfinding = pathfinding;
		Script.hookgenerator = hookgenerator;
		Script.config = config;
		Script.log = log;

		ArrayList<Hook> hookfeu = new ArrayList<Hook>();
		Executable takefire = new TakeFire(robotvrai);
		Hook hook = hookgenerator.hook_feu();
		hook.ajouter_callback(new Callback(takefire, true));		
		hookfeu.add(hook);

	}
		
	/**
	 * Exécute vraiment un script
	 */
	public void agit(int id_version, RobotVrai robotvrai, Table table)
	{
		Vec2 point_entree = point_entree(id_version, robotvrai, table);

		robotvrai.set_vitesse_translation("entre_scripts");
		robotvrai.set_vitesse_rotation("entre_scripts");

		ArrayList<Vec2> chemin = pathfinding.chemin(robotvrai.getPosition(), point_entree);

		try
		{
			robotvrai.suit_chemin(chemin, hookfeu);
			execute(id_version, robotvrai, table);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
		}
		finally
		{
			termine(robotvrai, table);
		}
		
	}
	
	/**
	 * Calcule le temps d'exécution de ce script (grâce à robotChrono)
	 * @return le temps d'exécution
	 */
	public long calcule(int id_version, RobotChrono robotchrono, Table table, boolean use_cache)
	{
		Vec2 point_entree = point_entree(id_version, robotchrono, table);
		robotchrono.set_vitesse_translation("entre_scripts");
		robotchrono.set_vitesse_rotation("entre_scripts");
		
		pathfinding.setUseCache(use_cache);
		robotchrono.initialiser_compteur(pathfinding.distance(robotchrono.getPosition(), point_entree));
		robotchrono.setPosition(point_entree);

		try {
			execute(id_version, robotchrono, table);
		}
		catch(Exception e)
		{
			log.warning("Exception scripts: "+e.toString(), this);
		}
		return robotchrono.get_compteur();
	}
		
	/**
	 * Renvoie le tableau des versions d'un script
	 * @return le tableau des versions possibles
	 */
	public abstract ArrayList<Integer> version(final Robot robot, final Table table);

	/**
	 * Retourne la position d'entrée associée à la version id
	 * @param id de la version
	 * @return la position du point d'entrée
	 */
	protected abstract Vec2 point_entree(int id, final Robot robot, final Table table);
	
	/**
	 * Renvoie le score que peut fournir un script
	 * @return le score
	 */
	public abstract int score(int id_version, final Robot robot, final Table table);
	
	/**
 	 * Donne le poids du script, utilisé pour calculer sa note
	 * @return le poids
	 */
	public abstract int poids(final Robot robot, final Table table);

	/**
	 * Exécute le script
	 */
	abstract protected void execute(int id_version, Robot robot, Table table) throws MouvementImpossibleException;

	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs.
	 */
	abstract protected void termine(Robot robot, Table table);
	
}
