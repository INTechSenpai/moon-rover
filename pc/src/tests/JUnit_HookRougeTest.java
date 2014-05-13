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

import enums.Vitesse;
import robot.RobotVrai;
import smartMath.Vec2;

/**
 * Tests unitaires des hooks (en rouge: avec symétrie)
 * @author pf
 *
 */

public class JUnit_HookRougeTest extends JUnit_Test {

	private RobotVrai robotvrai;
	private HookGenerator hookgenerator;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_HookRougeTest.setUp()", this);
		config.set("couleur", "rouge");
		robotvrai = (RobotVrai) container.getService("RobotVrai");
		robotvrai.setPosition(new Vec2(0, 1500));
		robotvrai.setOrientation(0);
        robotvrai.set_vitesse(Vitesse.ENTRE_SCRIPTS);
	}

	@Test
	public void test_hookAbscisse_avancer_symetrie() throws Exception
	{
		log.debug("JUnit_HookRougeTest.test_hookAbscisse_avancer_symetrie()", this);
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		int nb_balles = robotvrai.getNbrLances();
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robotvrai);
		Hook hook = hookgenerator.hook_abscisse(-20);
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);
		robotvrai.avancer(10, hooks);
		Assert.assertTrue(nb_balles == robotvrai.getNbrLances());
		robotvrai.avancer(50, hooks);
		Assert.assertTrue(nb_balles != robotvrai.getNbrLances());
	}
	
	@Test
	public void test_hookAbscisse_suit_chemin_symetrie() throws Exception
	{
		log.debug("JUnit_HookRougeTest.test_hookAbscisse_suit_chemin_symetrie()", this);
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		int nb_balles = robotvrai.getNbrLances();
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robotvrai);
		Hook hook = hookgenerator.hook_abscisse(30);
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);
		Assert.assertTrue(nb_balles == robotvrai.getNbrLances());
		ArrayList<Vec2> chemin = new ArrayList<Vec2>();
		chemin.add(new Vec2(20, 1400));
		chemin.add(new Vec2(40, 1500));
		robotvrai.suit_chemin(chemin, hooks);
		Assert.assertTrue(nb_balles != robotvrai.getNbrLances());
	}

	@Test
	public void test_hookPosition_suit_chemin_symetrie() throws Exception
	{
		log.debug("JUnit_HookRougeTest.test_hookPosition_suit_chemin_symetrie()", this);
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		int nb_balles = robotvrai.getNbrLances();
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robotvrai);
		Hook hook = hookgenerator.hook_position(new Vec2(20, 1400));
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);
		Assert.assertTrue(nb_balles == robotvrai.getNbrLances());
		ArrayList<Vec2> chemin = new ArrayList<Vec2>();
		chemin.add(new Vec2(20, 1400));
		chemin.add(new Vec2(40, 1500));
		robotvrai.suit_chemin(chemin, hooks);
		Assert.assertTrue(nb_balles != robotvrai.getNbrLances());
	}

	@Test
	public void test_hookPosition_avancer_symetrie() throws Exception
	{
		log.debug("JUnit_HookRougeTest.test_hookPosition_avancer_symetrie()", this);
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		int nb_balles = robotvrai.getNbrLances();
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robotvrai);
		Hook hook = hookgenerator.hook_position(new Vec2(-20, 1500));
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);
		robotvrai.avancer(10, hooks);
		Assert.assertTrue(nb_balles == robotvrai.getNbrLances());
		robotvrai.avancer(50, hooks);
		Assert.assertTrue(nb_balles != robotvrai.getNbrLances());
	}
	
	
}
