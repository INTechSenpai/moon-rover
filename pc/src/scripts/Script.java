package scripts;

import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.ConfigInfo;
import utils.Log;
import utils.Config;
import vec2.ReadOnly;
import vec2.Vec2;
import astar.arc.PathfindingNodes;
import hook.HookFactory;

import java.util.ArrayList;

import exceptions.FinMatchException;
import exceptions.PointSortieException;
import exceptions.ScriptException;
import exceptions.ScriptHookException;
import exceptions.SerialConnexionException;
import exceptions.UnableToMoveException;
/**
 * Classe abstraite dont héritent les différents scripts.
 * S'occupe de robotvrai et robotchrono de manière à ce que ce soit transparent pour les différents scripts
 * @author pf, marsu
 */

public abstract class Script
{
	protected int positionTolerancy;
	protected HookFactory hookfactory;
	protected Config config;
	protected Log log;
	
	private int squared_tolerance_depart_script = 400; // 2cm
	
	/**
	 * Renvoie le tableau des méta-verions d'un script
	 * @return le tableau des méta-versions possibles
	 */
	public abstract ArrayList<PathfindingNodes> getVersions(GameState<RobotChrono> state);

	public Script(HookFactory hookgenerator, Config config, Log log)
	{
		this.hookfactory = hookgenerator;
		this.config = config;
		this.log = log;
		updateConfig();
	}

	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs, on se décale du mur, ...
	 * A priori sans avoir besoin du numéro de version; si besoin est, à rajouter en paramètre.
	 * @throws ScriptHookException 
	 */
	protected abstract void termine(GameState<?> gamestate) throws ScriptException, FinMatchException, SerialConnexionException, ScriptHookException;
	
	/**
	 * Surcouche d'exécute, avec une gestion d'erreur.
	 * Peut être appelé avec un RobotReal ou un RobotChrono
	 * @param id_version
	 * @param state
	 * @throws ScriptException
	 * @throws FinMatchException
	 * @throws ScriptHookException
	 */
	public void agit(PathfindingNodes id_version, GameState<?> state) throws ScriptException, FinMatchException, ScriptHookException
	{
		if(state.robot instanceof RobotReal)
			log.debug("Agit version "+id_version);
		PathfindingNodes pointEntree = id_version;
		
		if(state.robot.getPosition().squaredDistance(pointEntree.getCoordonnees()) > squared_tolerance_depart_script)
		{
			log.critical("Appel d'un script à une mauvaise position. Le robot devrait être en "+pointEntree+" "+pointEntree.getCoordonnees()+" et est en "+state.robot.getPosition());
			throw new ScriptException();
		}
		try
		{
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
			try {
				termine(state);
			} catch (SerialConnexionException e) {
				try {
					state.robot.sleep(100); // on attends un petit peu...
					termine(state);  // on réessaye encore une fois
				} catch (SerialConnexionException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}

	}

	/**
	 * Utilisé par robotchrono
	 * @param id
	 * @return
	 */
	public PathfindingNodes point_sortie(PathfindingNodes id)
	{
		return id.getSortie();
	}

	/**
	 * Vérifie la position de sortie en simulation.
	 * @param id
	 * @param position
	 * @throws PointSortieException
	 */
	public final void checkPointSortie(PathfindingNodes id, Vec2<ReadOnly> position) throws PointSortieException
	{
		PathfindingNodes sortie = point_sortie(id);
		if(!position.equals(sortie.getCoordonnees()))
		{
			log.critical("Position de "+sortie+" incorrecte! Sa bonne position est: "+position);
			throw new PointSortieException();
		}

	}
	
	/**
	 * Exécute ou calcule le script, avec RobotVrai ou RobotChrono
	 * Attention! Pour les scripts de hook, on ne sait a priori pas où on est, dans quelle orientation, etc.
	 * Il faut donc vérifier qu'il y a bien la place de faire l'action, potentiellement se mettre en position, etc.
	 * @param gamestate
	 * @throws ScriptException
	 * @throws FinMatchException
	 *
	 * @throws SerialConnexionException 
	 * @throws ScriptHookException 
	 */
	protected abstract void execute(PathfindingNodes id_version, GameState<?>state) throws UnableToMoveException, SerialConnexionException, FinMatchException, ScriptHookException;

	public void updateConfig()
	{
		positionTolerancy = config.getInt(ConfigInfo.HOOKS_TOLERANCE_MM);		
		squared_tolerance_depart_script = config.getInt(ConfigInfo.TOLERANCE_DEPART_SCRIPT);
		squared_tolerance_depart_script *= squared_tolerance_depart_script; // on en utilise le carré
	}

}
