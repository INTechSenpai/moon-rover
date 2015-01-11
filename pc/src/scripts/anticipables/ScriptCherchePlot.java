package scripts.anticipables;

import java.util.ArrayList;

import astar.arc.PathfindingNodes;
import exceptions.FinMatchException;
import exceptions.ScriptException;
import exceptions.ScriptHookException;
import exceptions.SerialConnexionException;
import exceptions.UnableToMoveException;
import hook.HookFactory;
import robot.RobotChrono;
import scripts.Script;
import strategie.GameState;
import utils.Config;
import utils.Log;

/**
 * Script de recherche de plot
 * @author pf
 *
 */

public class ScriptCherchePlot extends Script
{

	public ScriptCherchePlot(HookFactory hookgenerator, Config config, Log log)
	{
		super(hookgenerator, config, log);
	}

	@Override
	public ArrayList<Integer> getVersions(GameState<RobotChrono> state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void termine(GameState<?> gamestate) throws ScriptException,
			FinMatchException, SerialConnexionException, ScriptHookException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PathfindingNodes point_entree(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PathfindingNodes point_sortie(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void execute(int id_version, GameState<?> state)
			throws UnableToMoveException, SerialConnexionException,
			FinMatchException, ScriptHookException {
		// TODO Auto-generated method stub
		
	}

}
