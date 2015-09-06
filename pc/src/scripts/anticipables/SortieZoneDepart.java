package scripts.anticipables;

import java.util.ArrayList;

import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import permissions.ReadWrite;
import exceptions.FinMatchException;
import exceptions.UnableToMoveException;
import hook.HookFactory;
import robot.RobotChrono;
import scripts.ScriptAnticipable;
import strategie.GameState;
import utils.Log;

/**
 * Faux script. Permet de sortir de la zone de départ.
 * Pas de version. Exécutée dès le début du match.
 * Permet de calculer une stratégie à la sortie de ce "script"
 * @author pf
 *
 */

public class SortieZoneDepart extends ScriptAnticipable {

	public SortieZoneDepart(HookFactory hookgenerator, Log log, GridSpace gridspace)
	{
		super(hookgenerator, log, gridspace);
	}

	@Override
	public ArrayList<Integer> getVersions(GameState<RobotChrono,ReadOnly> state) {
		ArrayList<Integer> out = new ArrayList<Integer>();
		return out;
	}

	@Override
	protected void execute(int id_version, GameState<?,ReadWrite> state)
			throws UnableToMoveException, FinMatchException
	{
		state.robot.tourner(Math.PI);
		state.robot.avancer(500); // TODO (avec règlement)
	}

	@Override
	protected void termine(GameState<?,ReadWrite> state) throws FinMatchException
	{}

	@Override
	public int point_sortie(int id)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
}
