package scripts;
import pathfinding.Pathfinding;
import smartMath.Vec2;
import hook.HookGenerator;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotVrai;
import table.Table;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;
import container.Service;

import java.util.ArrayList;

import exception.MouvementImpossibleException;
/**
 * Classe abstraite dont hériteront les différents scripts. S'occupe le robotvrai et robotchrono de manière à ce que ce soit transparent pour les différents scripts
 * @author pf
 */

public abstract class Script implements Service {

	// Ces services resteront toujours les mêmes, ont les factorise avec un static
	protected static ThreadTimer threadtimer;
	protected static HookGenerator hookgenerator;
	protected static Read_Ini config;
	protected static Log log;

	// Pathfinding, robot et table peuvent changer d'un script à l'autre, donc pas de static
	protected Pathfinding pathfinding;
	
	public Script(Pathfinding pathfinding, ThreadTimer threadtimer, HookGenerator hookgenerator, Read_Ini config, Log log) {
		this.pathfinding = pathfinding;
		Script.threadtimer = threadtimer;
		Script.hookgenerator = hookgenerator;
		Script.config = config;
		Script.log = log;
	}
		
	/**
	 * Exécute vraiment un script
	 */
	public void agit(int id_version, RobotVrai robotvrai, Table table)
	{
		try
		{
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
	public long calcule(int id_version, RobotChrono robotchrono, Table table)
	{
		robotchrono.reset_compteur();
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
	public abstract ArrayList<Integer> version(Robot robot, Table table);

	/**
	 * Retourne la position d'entrée associée à la version id
	 * @param id de la version
	 * @return la position du point d'entrée
	 */
	public abstract Vec2 point_entree(int id, Robot robot, Table table);
	
	/**
	 * Renvoie le score que peut fournir un script
	 * @return le score
	 */
	public abstract int score(int id_version, Robot robot, Table table);
	
	/**
 	 * Donne le poids du script, utilisé pour calculer sa note
	 * @return le poids
	 */
	public abstract int poids(Robot robot, Table table);

	/**
	 * Exécute le script
	 */
	abstract protected void execute(int id_version, Robot robot, Table table) throws MouvementImpossibleException;

	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs.
	 */
	abstract protected void termine(Robot robot, Table table);
	
}
