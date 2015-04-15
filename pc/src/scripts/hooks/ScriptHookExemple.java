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
import vec2.ReadWrite;

/**
 * Exemple
 * @author pf
 *
 */

public class ScriptHookExemple extends ScriptHook
{

	public ScriptHookExemple(HookFactory hookgenerator, Config config, Log log)
	{
		super(hookgenerator, config, log);
	}

	@Override
	protected void termine(GameState<RobotReal,ReadWrite> gamestate) throws ScriptException,
			FinMatchException, SerialConnexionException
	{
		// TODO (avec règlement)
	}

	@Override
	protected void execute(GameElementNames id_version, GameState<RobotReal,ReadWrite> state)
			throws UnableToMoveException, SerialConnexionException,
			FinMatchException
	{
		// TODO (avec règlement)
	}

}
