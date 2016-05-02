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
		robot.tourner(Math.PI, Speed.STANDARD);
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Hook hookBaisse = new HookDemiPlan(log, new Vec2<ReadOnly>(1000, 120), new Vec2<ReadOnly>(-10, 0));
		Hook hookMilieu = new HookDemiPlan(log, new Vec2<ReadOnly>(700, 120), new Vec2<ReadOnly>(-10, 0));
		Hook hookOuvre = new HookDemiPlan(log, new Vec2<ReadOnly>(500, 120), new Vec2<ReadOnly>(-10, 0));
		Hook hookFerme = new HookDemiPlan(log, new Vec2<ReadOnly>(450, 120), new Vec2<ReadOnly>(-10, 0));
		Hook hookLeve = new HookDemiPlan(log, new Vec2<ReadOnly>(400, 120), new Vec2<ReadOnly>(-10, 0));

		hookBaisse.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_BAS));
		hookMilieu.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_MILIEU));
		hookOuvre.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_OUVRE));
		hookFerme.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_FERME));
		hookLeve.ajouter_callback(new UtiliseActionneur(ActuatorOrder.AX12_POISSON_HAUT));

		hooks.add(hookBaisse);
		hooks.add(hookMilieu);
		hooks.add(hookOuvre);
		hooks.add(hookFerme);
		hooks.add(hookLeve);

		robot.tourner(Math.PI, Speed.STANDARD);
		robot.avancer(700, hooks, Speed.STANDARD);
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
