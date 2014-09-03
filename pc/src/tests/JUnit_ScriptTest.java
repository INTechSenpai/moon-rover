package tests;

import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.methodes.TirerBalles;
import hook.sortes.HookGenerator;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import enums.Cote;
import exceptions.strategie.ScriptException;
import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import strategie.GameState;
import utils.Sleep;

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
		config.set("couleur", "rouge");
		
		scriptmanager = (ScriptManager)container.getService("ScriptManager");
        real_state = (GameState<RobotVrai>)container.getService("RealGameState");
        chrono_state = real_state.clone();
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		container.demarreTousThreads();
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

	/*
	 * Tests des versions
	 */
	
	@Test
	public void test_ScriptLances_versions() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptLances");
		for(int i = 0; i < 6; i++)
		{
			Assert.assertTrue(s.meta_version(real_state).size() == 2);
			real_state.robot.tirerBalle();
		}
		Assert.assertTrue(s.meta_version(real_state).size() == 0);
	}

	@Test
	public void test_ScriptTree_versions() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptTree");
		Assert.assertTrue(s.meta_version(real_state).size() == 4);
		real_state.table.pickTree(1);
		Assert.assertTrue(s.meta_version(real_state).size() == 3);
		real_state.table.pickTree(3);
		Assert.assertTrue(s.meta_version(real_state).size() == 2);
		real_state.table.pickTree(2);
		Assert.assertTrue(s.meta_version(real_state).size() == 1);
		real_state.table.pickTree(1);
		Assert.assertTrue(s.meta_version(real_state).size() == 1);
		real_state.table.pickTree(0);
		Assert.assertTrue(s.meta_version(real_state).size() == 0);
	}

	@Test
	public void test_ScriptFresques_versions() throws Exception
	{
		s = (Script)scriptmanager.getScript("ScriptFresque");
		Assert.assertTrue(s.meta_version(real_state).size() == 1);
		real_state.robot.deposer_fresques();
		Assert.assertTrue(s.meta_version(real_state).size() == 0);
	}

	@Test
	public void test_ScriptFresques_agit() throws Exception
	{
        real_state.robot.initialiser_actionneurs_deplacements();
        real_state.robot.recaler();
        Sleep.sleep(3000);
        real_state.robot.avancer(300);
        s = (Script)scriptmanager.getScript("ScriptFresque");
		s.agit(2, real_state, true);
	}

	@Test
	public void test_ScriptDeposerFruits_agit() throws Exception
	{
        real_state.robot.initialiser_actionneurs_deplacements();
        real_state.robot.recaler();
        Sleep.sleep(3000);
        real_state.robot.avancer(300);
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
	    real_state.robot.initialiser_actionneurs_deplacements();
        real_state.robot.recaler();
        real_state.robot.initialiser_actionneurs_deplacements();
        Sleep.sleep(6000);
        real_state.robot.avancer(200);
        real_state.table.setFruitsNoirs(new int[] {1, 5, 4, 3});
        // 0-2 : partie gauche de l'arbre
        // 3-5 : partie droite de l'arbre
        // De plus, 0 et 3 sont les fruits les plus proches du robot.

        //	    real_state.robot.setPosition(new Vec2(-1000, 300));
		s = (Script)scriptmanager.getScript("ScriptTree");
	    //s.agit(1, real_state, true);
	    s.agit(2, real_state, true);
	    //s.agit(3, real_state, true);
	    s = (Script)scriptmanager.getScript("ScriptDeposerFruits");
	    s.agit(0, real_state, true);
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
		Assert.assertTrue(s.meta_version(real_state).size() == 0);
		real_state.robot.takefire(Cote.GAUCHE, Cote.GAUCHE);
		Assert.assertTrue(s.meta_version(real_state).size() == 3);
	}

	@Test
	public void test_ScriptDeposerFeu_agit() throws Exception
	{
		real_state.robot.initialiser_actionneurs_deplacements();
        real_state.robot.recaler();
        real_state.robot.initialiser_actionneurs_deplacements();
        real_state.robot.avancer(200);
	    real_state.robot.lever_pince(Cote.DROIT);
	    real_state.robot.lever_pince(Cote.GAUCHE);
	    //real_state.robot.takefire(Cote.GAUCHE);
		s = (Script)scriptmanager.getScript("ScriptDeposerFeu");
		real_state.robot.setTient_feu(Cote.DROIT);
		s.agit(0, real_state, true);
		real_state.robot.setTient_feu(Cote.DROIT);
		s.agit(1, real_state, true);
		real_state.robot.setTient_feu(Cote.DROIT);
		s.agit(2, real_state, true);
		real_state.robot.setTient_feu(Cote.DROIT);
		s.agit(3, real_state, true);
		real_state.robot.setTient_feu(Cote.DROIT);
		s.agit(4, real_state, true);
		real_state.robot.setTient_feu(Cote.GAUCHE);
		s.agit(0, real_state, true);
		real_state.robot.setTient_feu(Cote.GAUCHE);
		s.agit(1, real_state, true);
		real_state.robot.setTient_feu(Cote.GAUCHE);
		s.agit(2, real_state, true);
		real_state.robot.setTient_feu(Cote.GAUCHE);
		s.agit(3, real_state, true);
		real_state.robot.setTient_feu(Cote.GAUCHE);
		real_state.robot.setTient_feu(Cote.DROIT);
		s.agit(4, real_state, true);
	}

	@Test
	public void test_ScriptTorche_agit() throws Exception
	{
		real_state.robot.initialiser_actionneurs_deplacements();
        real_state.robot.recaler();
        //Sleep.sleep(3000);
        real_state.robot.avancer(300);
        //real_state.robot.setTient_feu(Cote.GAUCHE);
		s = (Script)scriptmanager.getScript("ScriptTorche");
		//s.agit(1, real_state, true);
		s.agit(2, real_state, true);
		Script s1 = (Script)scriptmanager.getScript("ScriptDeposerFeu");
		s1.agit(2, real_state, true);
		//s1.agit(3, real_state, true);
	}

    @Test
    public void test_ScriptLances_agit() throws Exception
    {
        real_state.robot.initialiser_actionneurs_deplacements();
        real_state.robot.recaler();
        Sleep.sleep(2000);
        real_state.robot.avancer(400);
        s = (Script)scriptmanager.getScript("ScriptLances");
        s.agit(0, real_state, true);
    }

	
	@Test
	public void test_takefire() throws Exception
	{
	    real_state.robot.setTient_feu(Cote.DROIT);
	    real_state.robot.takefire(Cote.GAUCHE, Cote.GAUCHE);
	}
	@Test 
	public void test_ScriptFeuBord() throws Exception
	{
		real_state.robot.initialiser_actionneurs_deplacements();
        real_state.robot.recaler();
		//real_state.robot.setOrientation(-(float)Math.PI/2);
		//real_state.robot.setPosition(new Vec2(1270,1700));
        Sleep.sleep(3000);
        real_state.robot.avancer(300);
		s = (Script)scriptmanager.getScript("ScriptFeuBord");
		s.agit(1, real_state, true);
		s = (Script)scriptmanager.getScript("ScriptDeposerFeu");
		s.agit(0, real_state, true);
		
	}
}
