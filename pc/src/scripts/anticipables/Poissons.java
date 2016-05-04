package scripts.anticipables;

import java.util.ArrayList;

import pathfinding.ChronoGameState;
import pathfinding.GameState;
import pathfinding.dstarlite.GridSpace;
import exceptions.FinMatchException;
import exceptions.UnableToMoveException;
import hook.Hook;
import hook.HookFactory;
import hook.methods.UtiliseActionneur;
import hook.types.HookDate;
import hook.types.HookDemiPlan;
import robot.Cinematique;
import robot.Robot;
import robot.RobotChrono;
import robot.Speed;
import robot.actuator.ActuatorOrder;
import scripts.ScriptAnticipable;
import table.Table;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

/**
 * Test
 * @author pf
 *
 */

public class Poissons extends ScriptAnticipable {

	public Poissons(HookFactory hookgenerator, Log log)
	{
		super(hookgenerator, log);
	}

	@Override
	public ArrayList<Integer> getVersions(Table table, Robot robot) {
		ArrayList<Integer> out = new ArrayList<Integer>();
		out.add(0);
		return out;
	}

	@Override
	protected void execute(int id_version, Table table, Robot robot)
			throws UnableToMoveException, FinMatchException
	{
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
		return new Cinematique(1100, 120, Math.PI, true, 0, 0, 0, Speed.SLOW);
	}
	
}
