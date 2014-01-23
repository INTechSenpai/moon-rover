package tests;

import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.HookGenerator;
import hook.methodes.TirerBalles;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Read_Ini;

/**
 * Tests unitaires des scripts
 * @author pf
 *
 */

public class JUnit_ScriptTest extends JUnit_Test {

	private ScriptManager scriptmanager;
	private Script s;
	private RobotVrai robotvrai;
	private RobotChrono robotchrono;
	private Table table;
	private Read_Ini config;
	private Log log;
	private HookGenerator hookgenerator;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		scriptmanager = (ScriptManager)container.getService("ScriptManager");
		robotvrai = (RobotVrai)container.getService("RobotVrai");
		config = (Read_Ini)container.getService("Read_Ini");
		log = (Log)container.getService("Log");
		robotchrono = new RobotChrono(config, log);
		robotchrono.majRobotChrono(robotvrai);
		table = (Table)container.getService("Table");
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		robotvrai.setPosition(new Vec2(0, 1500));
		robotvrai.setOrientation(0);
		robotvrai.set_vitesse_rotation("entre_scripts");
		robotvrai.set_vitesse_translation("entre_scripts");

	}

	@Test
	public void test_ScriptLances_score() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptLances");
		Assert.assertTrue(s.score(0, robotvrai, table) == 16);
		robotvrai.tirerBalles();
		Assert.assertTrue(s.score(0, robotvrai, table) == 14);
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robotvrai);
		Hook hook = hookgenerator.hook_abscisse(20);
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);		
		robotvrai.avancer(50, hooks);
		Assert.assertTrue(s.score(0, robotvrai, table) == 12);

	}

	/*
	 * Tests des versions
	 */
	
	@Test
	public void test_ScriptLances_versions() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptLances");
		for(int i = 0; i < 8; i++)
		{
			Assert.assertTrue(s.version(robotvrai, table).size() == 2);
			robotvrai.tirerBalles();
		}
		Assert.assertTrue(s.version(robotvrai, table).size() == 0);
	}

	@Test
	public void test_ScriptTree_versions() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptTree");
		Assert.assertTrue(s.version(robotvrai, table).size() == 4);
		table.pickTree(1);
		Assert.assertTrue(s.version(robotvrai, table).size() == 3);
		table.pickTree(3);
		Assert.assertTrue(s.version(robotvrai, table).size() == 2);
		table.pickTree(2);
		Assert.assertTrue(s.version(robotvrai, table).size() == 1);
		table.pickTree(1);
		Assert.assertTrue(s.version(robotvrai, table).size() == 1);
		table.pickTree(0);
		Assert.assertTrue(s.version(robotvrai, table).size() == 0);
	}

	@Test
	public void test_ScriptFresques_versions() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptFresque");
		Assert.assertTrue(s.version(robotvrai, table).size() == 1);
		robotvrai.deposer_fresques();
		Assert.assertTrue(s.version(robotvrai, table).size() == 0);
	}

	@Test
	public void test_ScriptDeposerFeu_versions() throws Exception
	{
		// TODO
		s = (Script)scriptmanager.getScript("ScriptDeposerFeu");
	}

}
