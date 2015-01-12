package scripts.hooks;

import java.util.ArrayList;

import astar.arc.PathfindingNodes;
import exceptions.FinMatchException;
import exceptions.ScriptException;
import exceptions.ScriptHookException;
import exceptions.SerialConnexionException;
import exceptions.UnableToMoveException;
import hook.HookFactory;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.Script;
import strategie.GameState;
import utils.Config;
import utils.Log;

/**
 * La funny action est un script de hook qui dépend de la date de jeu
 */

public class ScriptFunnyAction extends Script {

	public ScriptFunnyAction(HookFactory hookgenerator, Config config, Log log)
	{
		super(hookgenerator, config, log);
	}

	@Override
	public ArrayList<Integer> getVersions(GameState<RobotChrono> state) {
		// Jamais appelée a priori
		return null;
	}

	@Override
	protected void termine(GameState<?> gamestate) throws ScriptException,
			FinMatchException, SerialConnexionException, ScriptHookException {
		// Fin du match
		gamestate.robot.stopper();
		gamestate.robot.desactiver_asservissement_rotation();
		gamestate.robot.desactiver_asservissement_translation();
		((RobotReal)(gamestate.robot)).closeSerialConnections();
	}

	@Override
	public PathfindingNodes point_entree(int id) {
		return null; // peut survenir n'importe quand
	}

	@Override
	public PathfindingNodes point_sortie(int id) {
		return null; // peut survenir n'importe quand
	}

	@Override
	protected void execute(int id_version, GameState<?> state)
			throws UnableToMoveException, SerialConnexionException,
			FinMatchException, ScriptHookException {
		// DEPENDS_ON_RULES
	}

}
