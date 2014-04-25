package tests;

import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import smartMath.Vec2;
import strategie.MemoryManager;
import strategie.NoteScriptVersion;
import strategie.Strategie;
import table.Table;

import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;

import pathfinding.Pathfinding;

/**
 * Tests unitaires de la stratégie
 * @author pf
 *
 */

public class JUnit_StrategieTest extends JUnit_Test {

	private RobotVrai robotvrai;
	private Strategie strategie;
	private MemoryManager memorymanager;
	private Pathfinding pathfinder;
	private Table table;
	private RobotChrono robotchrono;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		robotvrai = (RobotVrai)container.getService("RobotVrai");
		strategie = (Strategie) container.getService("Strategie");
		memorymanager = (MemoryManager) container.getService("MemoryManager");
		pathfinder = (Pathfinding) container.getService("Pathfinding");
		table = (Table) container.getService("Table");
		robotchrono = new RobotChrono(config, log);
	}

	@Test
	public void test_notescriptversion() throws Exception
	{
		NoteScriptVersion a = new NoteScriptVersion();
		Assert.assertTrue(a.version == 0);
		Assert.assertTrue(a.script == null);
		Assert.assertTrue(a.note == 0);

		ScriptManager scriptmanager = (ScriptManager)container.getService("ScriptManager");
		Script s = (Script)scriptmanager.getScript("ScriptFresque");
		a = new NoteScriptVersion(23, s, 12);
		Assert.assertTrue(a.note == 23);
		Assert.assertTrue(a.script == s);
		Assert.assertTrue(a.version == 12);
	}

	@Test
	public void test_evaluation() throws Exception
	{
		robotvrai.setPosition(new Vec2(0, 1700));
		robotchrono.setPosition(new Vec2(0, 1700));
		log.debug("Strategie starting", this);
		strategie.evaluate();
	}
}
