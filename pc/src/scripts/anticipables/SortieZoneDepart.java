package scripts.anticipables;

import java.util.ArrayList;

import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.astar.arc.PathfindingNodes;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.UnableToMoveException;
import hook.HookFactory;
import robot.RobotChrono;
import scripts.Script;
import strategie.GameState;
import utils.Log;

/**
 * Faux script. Permet de sortir de la zone de départ.
 * Pas de version. Exécutée dès le début du match.
 * Permet de calculer une stratégie à la sortie de ce "script"
 * @author pf
 *
 */

public class SortieZoneDepart extends Script {

	public SortieZoneDepart(HookFactory hookgenerator, Log log)
	{
		super(hookgenerator, log);
	}

	@Override
	public ArrayList<PathfindingNodes> getVersions(GameState<RobotChrono,ReadOnly> state) {
		ArrayList<PathfindingNodes> out = new ArrayList<PathfindingNodes>();
		out.add(PathfindingNodes.POINT_DEPART);
		return out;
	}

	@Override
	protected void execute(PathfindingNodes id_version, GameState<?,ReadWrite> state)
			throws UnableToMoveException, FinMatchException, ScriptHookException
	{
		GameState.tourner(state, Math.PI);
		GameState.avancer(state, 500); // TODO (avec règlement)
	}

	@Override
	protected void termine(GameState<?,ReadWrite> state) throws FinMatchException, ScriptHookException
	{}
	
}
