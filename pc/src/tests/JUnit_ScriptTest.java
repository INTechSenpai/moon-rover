package tests;

import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.HookGenerator;
import hook.methodes.TirerBalles;

import java.util.ArrayList;

import org.junit.After;
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
import container.Container;

public class JUnit_ScriptTest {

	Container container;
	ScriptManager scriptmanager;
	Script s;
	RobotVrai robotvrai;
	RobotChrono robotchrono;
	Table table;
	Read_Ini config;
	Log log;
	HookGenerator hookgenerator;
	
	@Before
	public void setUp() throws Exception {
		container = new Container();
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
	
	@After
	public void tearDown() throws Exception {
		container.destructeur();
		container = null;
	}

	@Test
	public void test_ScriptLances_score() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptLances");
		Assert.assertTrue(s.score(0, robotvrai, table) == 16);
		
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robotvrai);
		Hook hook = hookgenerator.hook_abscisse(20);
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);		
		robotvrai.avancer(50, hooks);
		Assert.assertTrue(s.score(0, robotvrai, table) == 14);

	}

	
}
