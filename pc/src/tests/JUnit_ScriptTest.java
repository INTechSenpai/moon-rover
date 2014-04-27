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
import strategie.GameState;

/**
 * Tests unitaires des scripts
 * @author pf
 *
 */

public class JUnit_ScriptTest extends JUnit_Test {

	private ScriptManager scriptmanager;
	private Script s;
	private HookGenerator hookgenerator;
	private GameState<RobotVrai> real_state;
	private GameState<RobotChrono> chrono_state;
	
    @SuppressWarnings("unchecked")
    @Before
	public void setUp() throws Exception {
		super.setUp();
		config.set("couleur", "jaune");
		
		scriptmanager = (ScriptManager)container.getService("ScriptManager");
        real_state = (GameState<RobotVrai>)container.getService("RealGameState");
        chrono_state = real_state.clone();
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		real_state.robot.setPosition(new Vec2(1251, 1695));
		//On démarre avec la cale !!!!
		real_state.robot.setOrientation((float)(-Math.PI/2));
		real_state.robot.set_vitesse_rotation("entre_scripts");
		real_state.robot.set_vitesse_translation("entre_scripts");
		container.getService("threadPosition");
		container.demarreThreads();
		//robotvrai.set_vitesse_translation("30");
		real_state.robot.avancer(100);
	}

	@Test
	public void test_ScriptLances_score() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptLances");
		Assert.assertTrue(s.score(0, real_state) == 12);
		real_state.robot.tirerBalle();
		Assert.assertTrue(s.score(0, real_state) == 10);
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(real_state.robot);
		Hook hook = hookgenerator.hook_abscisse(20);
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);
		real_state.robot.avancer(50, hooks);
		Assert.assertTrue(s.score(0, real_state) == 8);

	}
	@Test
	public void test_ScriptLances_agit() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptLances");
		s.agit(0, real_state, false);
	}

	/*
	 * Tests des versions
	 */
	
	@Test
	public void test_ScriptLances_versions() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptLances");
		for(int i = 0; i < 6; i++)
		{
			Assert.assertTrue(s.version(real_state).size() == 2);
			real_state.robot.tirerBalle();
		}
		Assert.assertTrue(s.version(real_state).size() == 0);
	}

	@Test
	public void test_ScriptTree_versions() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptTree");
		Assert.assertTrue(s.version(real_state).size() == 4);
		real_state.table.pickTree(1);
		Assert.assertTrue(s.version(real_state).size() == 3);
		real_state.table.pickTree(3);
		Assert.assertTrue(s.version(real_state).size() == 2);
		real_state.table.pickTree(2);
		Assert.assertTrue(s.version(real_state).size() == 1);
		real_state.table.pickTree(1);
		Assert.assertTrue(s.version(real_state).size() == 1);
		real_state.table.pickTree(0);
		Assert.assertTrue(s.version(real_state).size() == 0);
	}

	@Test
	public void test_ScriptFresques_versions() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptFresque");
		Assert.assertTrue(s.version(real_state).size() == 3);
		real_state.robot.deposer_fresques();
		Assert.assertTrue(s.version(real_state).size() == 0);
	}

	@Test
	public void test_ScriptFresques_agit() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptFresque");
		s.agit(0, real_state, false);
	}

	@Test
	public void test_ScriptDeposerFruits_agit() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptDeposerFruits");
		s.agit(0, real_state, false);
	}

	@Test
	public void test_ScriptFresques_calcule() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptFresque");
		Assert.assertTrue(s.calcule(0, chrono_state, true) > 0);
		Assert.assertTrue(s.calcule(1, chrono_state, true) > 0);
		Assert.assertTrue(s.calcule(2, chrono_state, true) > 0);
	}

	@Test
	public void test_ScriptTree_calcule() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptTree");
		Assert.assertTrue(s.calcule(0, chrono_state, true) > 0);
		Assert.assertTrue(s.calcule(1, chrono_state, true) > 0);
		Assert.assertTrue(s.calcule(2, chrono_state, true) > 0);
		Assert.assertTrue(s.calcule(3, chrono_state, true) > 0);
	}

	@Test
	public void test_ScriptTree_agit() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptTree");
		s.agit(1, real_state, true);
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
		Assert.assertTrue(s.version(real_state).size() == 0);
		real_state.robot.takefire(Cote.GAUCHE);
		Assert.assertTrue(s.version(real_state).size() == 5);
	}

	@Test
	public void test_ScriptDeposerFeu_agit() throws Exception
	{
	    real_state.robot.lever_pince(Cote.DROIT);
	    real_state.robot.lever_pince(Cote.GAUCHE);
	    real_state.robot.takefire(Cote.GAUCHE);
		s = (Script)scriptmanager.getScript("ScriptDeposerFeu");
		s.agit(2, real_state, true);
	}

	@Test
	public void test_ScriptTorche_agit() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptTorche");
		s.agit(1, real_state, true);
	}
	
	@Test
	public void test_takefire() throws Exception
	{
	    real_state.robot.setTient_feu(Cote.DROIT);
	    real_state.robot.takefire(Cote.GAUCHE);
	}
}
