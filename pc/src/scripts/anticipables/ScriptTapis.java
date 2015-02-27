package scripts.anticipables;

import java.util.ArrayList;

import astar.arc.PathfindingNodes;
import exceptions.FinMatchException;
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
 * Script des tapis.
 * @author pf
 *
 */

public class ScriptTapis extends Script {

	public ScriptTapis(HookFactory hookgenerator, Config config, Log log)
	{
		super(hookgenerator, config, log);
	}

	@Override
	public ArrayList<PathfindingNodes> getVersions(GameState<RobotChrono> state)
	{
		// Si les tapis sont posés, pas de métaversion possible
		ArrayList<PathfindingNodes> out = new ArrayList<PathfindingNodes>();
		if(state.robot.areTapisPoses())
			return out;
		
		// Sinon, on renvoie uniquement la méta_version 0
		out.add(PathfindingNodes.NODE_TAPIS);
		return out;
	}

	@Override
	public void execute(PathfindingNodes id_version, GameState<?> state) throws UnableToMoveException, SerialConnexionException, FinMatchException, ScriptHookException
	{
		state.robot.tourner(-Math.PI/2);
		state.robot.avancer_dans_mur(-260);
		state.robot.poserDeuxTapis();
	}

	@Override
	protected void termine(GameState<?> state) throws SerialConnexionException, FinMatchException, ScriptHookException
	{
		// on relève les tapis
		state.robot.leverDeuxTapis();
		try {
			// et on se dégage
			state.robot.avancer(260); // TODO (avec règlement)
		} catch (UnableToMoveException e) {
			e.printStackTrace();
		}
	}
}
