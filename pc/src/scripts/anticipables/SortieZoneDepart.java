package scripts.anticipables;

import java.util.ArrayList;

import pathfinding.ChronoGameState;
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
import robot.Cinematique;
import robot.Robot;
import robot.RobotChrono;
import robot.Speed;
import scripts.ScriptAnticipable;
import table.Table;
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
	public ArrayList<Integer> getVersions(Table table, Robot robot) {
		ArrayList<Integer> out = new ArrayList<Integer>();
		return out;
	}

	@Override
	protected void execute(int id_version, Table table, Robot robot)
			throws UnableToMoveException, FinMatchException
	{
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Hook hook = new HookDate(log, 2000);
		hook.ajouter_callback(new UtiliseActionneur(ActuatorOrder.TEST));
		hooks.add(hook);
		robot.tourner(Math.PI, Speed.STANDARD);
		robot.avancer(500, hooks, Speed.STANDARD);
	}

	@Override
	protected void termine(Table table, Robot robot) throws FinMatchException
	{}

	@Override
	public int point_sortie(int id)
	{
		// TODO point de sortie
		return 0;
	}

	@Override
	public Cinematique pointEntree(int version)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
