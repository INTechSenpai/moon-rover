package scripts.hooks;

import pathfinding.GameState;
import permissions.ReadWrite;
import exceptions.FinMatchException;
import exceptions.ScriptException;
import exceptions.UnableToMoveException;
import hook.HookFactory;
import scripts.ScriptHook;
import table.GameElementNames;
import utils.Log;

/**
 * Exemple de script lançable par hook
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
	protected void termine(GameState<?,ReadWrite> gamestate) throws ScriptException,
			FinMatchException
	{
		// TODO (avec règlement)
	}

	@Override
	protected void execute(GameElementNames id_version, GameState<?,ReadWrite> state)
			throws UnableToMoveException, FinMatchException
	{
		// TODO (avec règlement)
	}

	@Override
	protected boolean isPossible(GameState<?, ReadWrite> gamestate)
	{
		// TODO (avec règlement)
		return false;
	}

}
