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

/**
 * La funny action est un script de hook qui dépend de la date de jeu
 */

public class ScriptFunnyAction extends ScriptHook
{

	public ScriptFunnyAction(HookFactory hookgenerator, Config config, Log log)
	{
		super(hookgenerator, config, log);
	}

	@Override
	protected void termine(GameState<RobotReal> gamestate) throws ScriptException,
			FinMatchException, SerialConnexionException {
		// Fin du match
		gamestate.robot.stopper();
		gamestate.robot.desactiver_asservissement_rotation();
		gamestate.robot.desactiver_asservissement_translation();
		gamestate.robot.closeSerialConnections();
	}

	@Override
	protected void execute(GameElementNames id_version, GameState<RobotReal> state)
			throws UnableToMoveException, SerialConnexionException,
			FinMatchException
	{
		// DEPENDS_ON_RULES
	}

}
