package scripts.hooks;

import permissions.ReadWrite;
import exceptions.FinMatchException;
import exceptions.ScriptException;
import exceptions.UnableToMoveException;
import hook.HookFactory;
import robot.RobotReal;
import scripts.ScriptHook;
import strategie.GameState;
import table.GameElementNames;
import utils.Log;

/**
 * Exemple
 * @author pf
 *
 */

public class ScriptHookExemple extends ScriptHook
{

	public ScriptHookExemple(HookFactory hookgenerator, Log log)
	{
		super(hookgenerator, log);
	}

	@Override
	protected void termine(GameState<RobotReal,ReadWrite> gamestate) throws ScriptException,
			FinMatchException
	{
		// TODO (avec règlement)
	}

	@Override
	protected void execute(GameElementNames id_version, GameState<RobotReal,ReadWrite> state)
			throws UnableToMoveException, FinMatchException
	{
		// TODO (avec règlement)
	}

}
