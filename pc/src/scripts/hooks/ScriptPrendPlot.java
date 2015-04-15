package scripts.hooks;

import exceptions.FinMatchException;
import exceptions.ScriptException;
import exceptions.SerialConnexionException;
import exceptions.UnableToMoveException;
import hook.HookFactory;
import robot.RobotReal;
import scripts.ScriptHook;
import strategie.GameState;
import table.GameElementNames;
import utils.Config;
import utils.Log;
import vec2.ReadOnly;
import vec2.Vec2;

/**
 * Script hook de prise de plot.
 * C'est le script appelé lorsqu'on a détecté un plot.
 * @author pf
 *
 */

public class ScriptPrendPlot extends ScriptHook
{

	private int distance_optimale;
	
	public ScriptPrendPlot(HookFactory hookgenerator, Config config, Log log)
	{
		super(hookgenerator, config, log);
	}

	@Override
	protected void termine(GameState<RobotReal> gamestate) throws ScriptException,
			FinMatchException, SerialConnexionException
	{
		// TODO (avec règlement)
	}

	@Override
	protected void execute(GameElementNames id_version, GameState<RobotReal> state)
			throws UnableToMoveException, SerialConnexionException,
			FinMatchException
	{
		state.robot.stopper();
		Vec2<ReadOnly> position = state.robot.getPosition();
		Vec2<ReadOnly> position_obstacle = id_version.getObstacle().getPosition();
		double orientation_cible = Math.atan2(position_obstacle.y-position.y, position_obstacle.x-position.x);
		state.robot.tourner(orientation_cible);
		// TODO (avec règlement)
		int distance = (int) position.distance(position_obstacle);
		state.robot.avancer(distance-distance_optimale);
	}

}
