package scripts.anticipables;

import java.util.ArrayList;

import pathfinding.GameState;
import permissions.ReadOnly;
import permissions.ReadWrite;
import exceptions.FinMatchException;
import exceptions.UnableToMoveException;
import hook.HookFactory;
import robot.RobotChrono;
import scripts.ScriptAnticipable;
import utils.Log;

/**
 * N'est pas vraiment un script à proprement parler. C'est juste une attente.
 * Mis sous la forme d'un script afin d'être utilisé dans la stratégie
 * @author pf
 *
 */

public class ScriptAttente extends ScriptAnticipable
{

	public ScriptAttente(HookFactory hookgenerator, Log log)
	{
		super(hookgenerator, log);
	}

	@Override
	public ArrayList<Integer> getVersions(GameState<RobotChrono,ReadOnly> state)
	{
		ArrayList<Integer> version = new ArrayList<Integer>();
/*		PathfindingNodes entree_sortie = GameState.getPositionPathfinding(state);
		if(entree_sortie != null && GameState.canSleepUntilSomethingChange(state))
			version.add(entree_sortie);*/
		return version;
	}

	@Override
	public int point_sortie(int id) {
		// ce qui signifie: on ne bouge pas
		return id;
	}

	@Override
	protected void execute(int id_version, GameState<?,ReadWrite> state)
			throws UnableToMoveException, FinMatchException
	{
		/**
		 * On attend jusqu'à ce qu'un obstacle ait disparu.
		 */
//		GameState.sleepUntilSomethingChange(state);
		// TODO
	}

	@Override
	protected void termine(GameState<?,ReadWrite> state) throws FinMatchException
	{
	}

}
