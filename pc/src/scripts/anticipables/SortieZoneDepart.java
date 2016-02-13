package scripts.anticipables;

import java.util.ArrayList;

import pathfinding.GameState;
import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import permissions.ReadWrite;
import exceptions.FinMatchException;
import exceptions.UnableToMoveException;
import hook.Hook;
import hook.HookFactory;
import hook.methods.UtiliseActionneur;
import hook.types.HookDate;
import robot.ActuatorOrder;
import robot.RobotChrono;
import scripts.ScriptAnticipable;
import utils.Log;

/**
 * Test
 * @author pf
 *
 */

public class SortieZoneDepart extends ScriptAnticipable {

	public SortieZoneDepart(HookFactory hookgenerator, Log log)
	{
		super(hookgenerator, log);
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
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Hook hook = new HookDate(log, 2000);
		hook.ajouter_callback(new UtiliseActionneur(ActuatorOrder.BAISSE_TAPIS_DROIT));
		hooks.add(hook);
		state.robot.tourner(Math.PI);
		state.robot.avancer(500, hooks);
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
