package tests;

import static org.junit.Assert.*;
import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.HookGenerator;
import hook.methodes.TirerBalles;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.Pathfinding;
import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import smartMath.Vec2;
import table.Table;

public class PathfindingIntegrationTest extends JUnit_Test
{
	private Table table;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		config.set("couleur", "jaune");
	//	robotchrono = new RobotChrono(config, log);
		table = (Table)container.getService("Table");
		table.initialise();
		container.getService("threadPosition");
		container.demarreThreads();
	}

	@Test
	public void test_InstanciationPathfinding() throws Exception
	{
		Pathfinding finder = new Pathfinding(table, config, log, 1);
		Assert.assertTrue(true);

	}
}
