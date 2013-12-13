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
	protected Robot robot;
	protected Table table;

	// Les scripts n'auront pas directement accès à robotvrai et à robotchrono
	private RobotVrai robotvrai;
	private RobotChrono robotchrono;
	
	public Script(Service pathfinding, Service threadtimer, Service robotvrai, Service robotchrono, Service hookgenerator, Service table, Service config, Service log) {
		this.pathfinding = (Pathfinding) pathfinding;
		Script.threadtimer = (ThreadTimer) threadtimer;
		this.robotvrai = (RobotVrai) robotvrai;
		this.robotchrono = (RobotChrono) robotchrono;
		Script.hookgenerator = (HookGenerator) hookgenerator;
		this.table = (Table) table;
		Script.config = (Read_Ini) config;
		Script.log = (Log) log;
	}
		
	/**
	 * Exécute vraiment un script
	 */
	public void agit(int id_version)
	{
		robot = robotvrai;
		try
		{
			execute(id_version);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
		}
		finally
		{
			termine();
		}
		
	}
	
	/**
	 * Calcule le temps d'exécution de ce script (grâce à robotChrono)
	 * @return le temps d'exécution
	 */
	public long calcule(int id_version)
	{
		robot = robotchrono;
		robotchrono.reset_compteur();
		try {
			execute(id_version);
		}
		catch(Exception e)
		{
			log.warning("Exception scripts: "+e.toString(), this);
		}
		return robotchrono.get_compteur();
	}
	
	/**
	 * Retourne la table du script. A appeler après avoir utilisé calcule
	 * @return la table
	 */
	public Table getTable()
	{
		return table;
	}
	
	/**
	 * Retourne le robotchrono du script. A appeler après avoir utilisé calcule
	 * @return robotchrono
	 */
	public Robot getRobotChrono()
	{
		return robotchrono;
	}
	
	/**
	 * Renvoie le tableau des versions d'un script
	 * @return le tableau des versions possibles
	 */
	public abstract ArrayList<Integer> version();

	/**
	 * Retourne la position d'entrée associée à la version id
	 * @param id de la version
	 * @return la position du point d'entrée
	 */
	public abstract Vec2 point_entree(int id);
	
	/**
	 * Renvoie le score que peut fournir un script
	 * @return le score
	 */
	public abstract int score(int id_version);
	
	/**
 	 * Donne le poids du script, utilisé pour calculer sa note
	 * @return le poids
	 */
	public abstract int poids();

	/**
	 * Exécute le script
	 */
	abstract protected void execute(int id_version);

	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs.
	 */
	abstract protected void termine();

}
