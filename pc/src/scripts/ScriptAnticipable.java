package scripts;

import robot.RobotChrono;
import utils.ConfigInfo;
import utils.Log;
import utils.Config;
import utils.Vec2;
import hook.HookFactory;

import java.util.ArrayList;

import pathfinding.GameState;
import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import permissions.ReadWrite;
import exceptions.FinMatchException;
import exceptions.PointSortieException;
import exceptions.ScriptException;
import exceptions.UnableToMoveException;
/**
 * Classe abstraite dont héritent les différents scripts.
 * S'occupe de robotvrai et robotchrono de manière à ce que ce soit transparent pour les différents scripts
 * @author pf, marsu
 */

public abstract class ScriptAnticipable
{
	protected int positionTolerancy;
	protected HookFactory hookfactory;
	protected Log log;
	
	private int squared_tolerance_depart_script = 400; // 2cm
	protected volatile boolean symetrie;
	
	/**
	 * Renvoie le tableau des méta-verions d'un script
	 * @return le tableau des méta-versions possibles
	 */
	public abstract ArrayList<Integer> getVersions(GameState<RobotChrono,ReadOnly> state);

	public ScriptAnticipable(HookFactory hookgenerator, Log log)
	{
		this.hookfactory = hookgenerator;
		this.log = log;
	}

	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs, on se décale du mur, ...
	 * A priori sans avoir besoin du numéro de version; si besoin est, à rajouter en paramètre.
	 * @throws ScriptException 
	 * @throws FinMatchException 
	 */
	protected abstract void termine(GameState<?,ReadWrite> gamestate) throws ScriptException, FinMatchException;
	
	/**
	 * Surcouche d'exécute, avec une gestion d'erreur.
	 * Peut être appelé avec un RobotReal ou un RobotChrono
	 * @param id_version
	 * @param state
	 * @throws ScriptException
	 * @throws FinMatchException
	 */
	public void agit(int id_version, GameState<?,ReadWrite> state) throws ScriptException, FinMatchException
	{
//		if(state.robot instanceof RobotReal)
//			log.debug("Agit version "+id_version);
		int pointEntree = id_version;
		
/*		if(state.robot.getPosition().squaredDistance(gridspace.computeVec2(pointEntree)) > squared_tolerance_depart_script)
		{
			log.critical("Appel d'un script à une mauvaise position. Le robot devrait être en "+pointEntree+" et est en "+state.robot.getPosition());
			throw new ScriptException();
		}*/
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
			termine(state);
		}

	}

	/**
	 * Utilisé par robotchrono
	 * @param id
	 * @return
	 */
	public abstract int point_sortie(int id);
	
	/**
	 * Exécute ou calcule le script, avec RobotVrai ou RobotChrono
	 * Attention! Pour les scripts de hook, on ne sait a priori pas où on est, dans quelle orientation, etc.
	 * Il faut donc vérifier qu'il y a bien la place de faire l'action, potentiellement se mettre en position, etc.
	 * @param gamestate
	 * @throws ScriptException
	 * @throws UnableToMoveException 
	 */
	protected abstract void execute(int id_version, GameState<?,ReadWrite>state) throws UnableToMoveException, FinMatchException;

	public void updateConfig(Config config)
	{
		symetrie = config.getSymmetry();
	}

	public void useConfig(Config config)
	{
		positionTolerancy = config.getInt(ConfigInfo.HOOKS_TOLERANCE_MM);		
		squared_tolerance_depart_script = config.getInt(ConfigInfo.TOLERANCE_DEPART_SCRIPT);
		squared_tolerance_depart_script *= squared_tolerance_depart_script; // on en utilise le carré
	}

}
