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

import exception.ScriptException;
import robot.Cote;
import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import smartMath.Vec2;
import table.Table;

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
	private HookGenerator hookgenerator;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		config.set("couleur", "jaune");
		
		scriptmanager = (ScriptManager)container.getService("ScriptManager");
		robotvrai = (RobotVrai)container.getService("RobotVrai");
		robotchrono = new RobotChrono(config, log);
		robotchrono.majRobotChrono(robotvrai);
		table = (Table)container.getService("Table");
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		robotvrai.setPosition(new Vec2(1251, 1695));
		//On démarre avec la cale !!!!
		robotvrai.setOrientation((float)(-Math.PI/2));
		robotvrai.set_vitesse_rotation("entre_scripts");
		robotvrai.set_vitesse_translation("entre_scripts");
		container.getService("threadPosition");
		container.demarreThreads();
		//robotvrai.set_vitesse_translation("30");
		robotvrai.avancer(100);
	}

	@Test
	public void test_ScriptLances_score() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptLances");
		Assert.assertTrue(s.score(0, robotvrai, table) == 12);
		robotvrai.tirerBalle();
		Assert.assertTrue(s.score(0, robotvrai, table) == 10);
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robotvrai);
		Hook hook = hookgenerator.hook_abscisse(20);
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);
		robotvrai.avancer(50, hooks);
		Assert.assertTrue(s.score(0, robotvrai, table) == 8);

	}
	@Test
	public void test_ScriptLances_agit() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptLances");
		s.agit(0, robotvrai, table, false);
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
			robotvrai.tirerBalle();
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
		Assert.assertTrue(s.version(robotvrai, table).size() == 3);
		robotvrai.deposer_fresques();
		Assert.assertTrue(s.version(robotvrai, table).size() == 0);
	}

	@Test
	public void test_ScriptFresques_agit() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptFresque");
		s.agit(0, robotvrai, table, false);
	}

	@Test
	public void test_ScriptDeposerFruits_agit() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptDeposerFruits");
		s.agit(0, robotvrai, table, false);
	}

	@Test
	public void test_ScriptFresques_calcule() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptFresque");
		RobotChrono robotchrono = new RobotChrono(config, log);
		Assert.assertTrue(s.calcule(0, robotchrono, table, true) > 0);
		Assert.assertTrue(s.calcule(1, robotchrono, table, true) > 0);
		Assert.assertTrue(s.calcule(2, robotchrono, table, true) > 0);
	}

	@Test
	public void test_ScriptTree_calcule() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptTree");
		RobotChrono robotchrono = new RobotChrono(config, log);
		Assert.assertTrue(s.calcule(0, robotchrono, table, true) > 0);
		Assert.assertTrue(s.calcule(1, robotchrono, table, true) > 0);
		Assert.assertTrue(s.calcule(2, robotchrono, table, true) > 0);
		Assert.assertTrue(s.calcule(3, robotchrono, table, true) > 0);
	}

	@Test
	public void test_ScriptTree_agit() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptTree");
		s.agit(1, robotvrai, table, true);
	}

	@Test(expected=ScriptException.class)
	public void test_erreur() throws Exception
	{
		s = (Script)scriptmanager.getScript("ABWABWA");
	}

	@Test
	public void test_ScriptDeposerFeu_versions() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptDeposerFeu");
		Assert.assertTrue(s.version(robotvrai, table).size() == 0);
		robotvrai.takefire(Cote.GAUCHE);
		Assert.assertTrue(s.version(robotvrai, table).size() == 5);
	}

	@Test
	public void test_ScriptDeposerFeu_agit() throws Exception
	{
		robotvrai.lever_pince(Cote.DROIT);
		robotvrai.lever_pince(Cote.GAUCHE);
		robotvrai.takefire(Cote.GAUCHE);
		s = (Script)scriptmanager.getScript("ScriptDeposerFeu");
		s.agit(2, robotvrai, table, true);
	}

	@Test
	public void test_ScriptTorche_agit() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptTorche");
		s.agit(0, robotvrai, table, true);
	}
	
	@Test
	public void test_takefire() throws Exception
	{
		robotvrai.takefire(Cote.DROIT);
	}

}
