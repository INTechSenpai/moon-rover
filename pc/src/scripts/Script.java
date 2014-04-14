package scripts;

import pathfinding.Pathfinding;
import smartMath.Vec2;
import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.HookGenerator;
import robot.Cote;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotVrai;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import hook.methodes.DisparitionTorche;
import hook.methodes.TakeFire;

import java.util.ArrayList;

import exception.ConfigException;
import exception.MouvementImpossibleException;
import exception.PathfindingException;
import exception.ScriptException;
import exception.SerialException;
/**
 * Classe abstraite dont hériteront les différents scripts. S'occupe le robotvrai et robotchrono de manière à ce que ce soit transparent pour les différents scripts
 * @author pf
 */

public abstract class Script implements Service {

	// Ces services resteront toujours les mêmes, on les factorise avec un static
	protected static HookGenerator hookgenerator;
	protected static Read_Ini config;
	protected static Log log;
	
	private ArrayList<Hook> hooks_chemin = new ArrayList<Hook>();
	
	protected String couleur; 
	private int rayon_robot;
	
	public Script(HookGenerator hookgenerator, Read_Ini config, Log log, RobotVrai robotvrai)
	{
		Script.hookgenerator = hookgenerator;
		Script.config = config;
		Script.log = log;
		
		try {
			couleur = config.get("couleur");
		} catch (ConfigException e) {
			e.printStackTrace();
		}

		try {
			rayon_robot = Integer.parseInt(config.get("rayon_robot"));
		} catch (NumberFormatException | ConfigException e) {
			e.printStackTrace();
		}
}
		
	/**
	 * Exécute vraiment un script
	 */
	public void agit(int id_version, RobotVrai robotvrai, Table table, Pathfinding pathfinding, boolean retenter_si_blocage) throws ScriptException
	{
		Vec2 point_entree = point_entree(id_version);

		robotvrai.set_vitesse_translation("entre_scripts");
		robotvrai.set_vitesse_rotation("entre_scripts");

		Executable takefire = new TakeFire(robotvrai, Cote.GAUCHE);
		Hook hook = hookgenerator.hook_feu(Cote.GAUCHE);
		hook.ajouter_callback(new Callback(takefire, true));		
		hooks_chemin.add(hook);

		takefire = new TakeFire(robotvrai, Cote.DROIT);
		hook = hookgenerator.hook_feu(Cote.DROIT);
		hook.ajouter_callback(new Callback(takefire, true));
		hooks_chemin.add(hook);

		Executable torche_disparue = new DisparitionTorche(table, Cote.DROIT);
		hook = hookgenerator.hook_position(table.getPositionTorche(Cote.DROIT), table.getRayonTorche(Cote.DROIT)+rayon_robot);
		hook.ajouter_callback(new Callback(torche_disparue, true));
		hooks_chemin.add(hook);
	
		torche_disparue = new DisparitionTorche(table, Cote.GAUCHE);
		hook = hookgenerator.hook_position(table.getPositionTorche(Cote.GAUCHE), table.getRayonTorche(Cote.GAUCHE)+rayon_robot);
		hook.ajouter_callback(new Callback(torche_disparue, true));
		hooks_chemin.add(hook);

		try
		{
			robotvrai.va_au_point_pathfinding(pathfinding, point_entree, hooks_chemin, retenter_si_blocage, false, true, false);
			execute(id_version, robotvrai, table, pathfinding);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			// Si on rencontre un obstacle en allant exécuter un script et qu'il reste d'autres scripts, alors on change de script
			throw new ScriptException();
		}
		finally
		{
			termine(robotvrai, table, pathfinding);
		}
		
	}
	
	/**
	 * Calcule le temps d'exécution de ce script (grâce à robotChrono)
	 * @return le temps d'exécution
	 */
	public long calcule(int id_version, RobotChrono robotchrono, Table table, Pathfinding pathfinding, boolean use_cache)
	{
		Vec2 point_entree = point_entree(id_version);
		robotchrono.set_vitesse_translation("entre_scripts");
		robotchrono.set_vitesse_rotation("entre_scripts");
		
		try {
			robotchrono.initialiser_compteur(pathfinding.distance(robotchrono.getPosition(), point_entree, use_cache));
		} catch (PathfindingException e1) {
			// En cas de problème du pathfinding, on évalue la longueur du chemin
			robotchrono.initialiser_compteur((int)(robotchrono.getPosition().distance(point_entree)*1.5));
			e1.printStackTrace();
		}
		robotchrono.setPosition(point_entree);

		try {
			execute(id_version, robotchrono, table, pathfinding);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return robotchrono.get_compteur();
	}
	/**
	 * Renvoie les versions associées associée à une méta-version
	 * @return le tableau des versions associée
	 */
	public abstract ArrayList<Integer> version_asso(int id_meta);
	
	/**
	 * Renvoie le tableau des méta-verions d'un script
	 * @return le tableau des méta-versions possibles
	 */
	public abstract ArrayList<Integer> meta_version(final Robot robot, final Table table, Pathfinding pathfinding);
		
	/**
	 * Renvoie le tableau des versions d'un script
	 * @return le tableau des versions possibles
	 */
	public abstract ArrayList<Integer> version(final Robot robot, final Table table, Pathfinding pathfinding);

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
	public abstract int score(int id_version, final Robot robot, final Table table);
	
	/**
 	 * Donne le poids du script, utilisé pour calculer sa note
	 * @return le poids
	 */
	public abstract int poids(final Robot robot, final Table table);

	/**
 	 * Donne la probabilité que le script réussisse
	 * @return la proba que la script réussisse, en supposant que l'ennemi n'y soit pas
	 */
	public abstract float proba_reussite();

	/**
	 * Exécute le script
	 * @throws SerialException 
	 */
	abstract protected void execute(int id_version, Robot robot, Table table, Pathfinding pathfinding) throws MouvementImpossibleException, SerialException;

	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs.
	 */
	abstract protected void termine(Robot robot, Table table, Pathfinding pathfinding);
	
	public void maj_config()
	{
	}

	
}
