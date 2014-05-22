package tests;

import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.methodes.LeverRateau;
import hook.methodes.TakeFire;
import hook.methodes.TirerBalles;
import hook.sortes.HookGenerator;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import enums.Cote;
import enums.Vitesse;
import robot.RobotVrai;
import smartMath.Vec2;
import utils.Sleep;

/**
 * Tests unitaires des hooks (en jaune: sans symétrie)
 * @author pf
 *
 */

public class JUnit_HookJauneTest extends JUnit_Test {

	private RobotVrai robotvrai;
	private HookGenerator hookgenerator;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_HookJauneTest.setUp()", this);
		config.set("couleur", "jaune");
		robotvrai = (RobotVrai) container.getService("RobotVrai");
		robotvrai.setPosition(new Vec2(0, 1500));
		robotvrai.setOrientation(0);
		robotvrai.set_vitesse(Vitesse.ENTRE_SCRIPTS);
		container.getService("threadPosition");
//		container.demarreThreads();
	}

	@Test
	public void test_hookAbscisse_avancer() throws Exception
	{
		log.debug("JUnit_HookJauneTest.test_hookAbscisse_avancer()", this);
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		int nb_balles = robotvrai.getNbrLances();
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robotvrai);
		Hook hook = hookgenerator.hook_abscisse(20);
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);		
		robotvrai.avancer(10, hooks);
		Assert.assertTrue(nb_balles == robotvrai.getNbrLances());
		robotvrai.avancer(50, hooks);
		Assert.assertTrue(nb_balles != robotvrai.getNbrLances());
	}

	
	@Test
	public void test_hookAbscisse_suit_chemin() throws Exception
	{
		log.debug("JUnit_HookJauneTest.test_hookAbscisse_suit_chemin()", this);
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
	public void test_hookPosition_suit_chemin() throws Exception
	{
		log.debug("JUnit_HookJauneTest.test_hookPosition_suit_chemin()", this);
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
	public void test_hookPosition_avancer() throws Exception
	{
		log.debug("JUnit_HookJauneTest.test_hookPosition_avancer()", this);
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		int nb_balles = robotvrai.getNbrLances();
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robotvrai);
		Hook hook = hookgenerator.hook_position(new Vec2(20, 1500));
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);
		robotvrai.avancer(10, hooks);
		Assert.assertTrue(nb_balles == robotvrai.getNbrLances());
		robotvrai.avancer(50, hooks);
		Assert.assertTrue(nb_balles != robotvrai.getNbrLances());
	}
	
	@Test
	public void test_hookAbscisse_takeFire() throws Exception
	{
		log.debug("JUnit_HookJauneTest.test_hookAbscisse_takeFire()", this);
		Assert.assertTrue(!robotvrai.isTient_feu(Cote.GAUCHE));
		Assert.assertTrue(!robotvrai.isTient_feu(Cote.DROIT));
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable takefire = new TakeFire(robotvrai);
		Hook hook = hookgenerator.hook_abscisse(100);
		hook.ajouter_callback(new Callback(takefire, false));
		hooks.add(hook);		
		robotvrai.avancer(200, hooks);
		Assert.assertTrue(robotvrai.isTient_feu(Cote.GAUCHE));
	}

	@Test
	public void test_hookAbscisse_takeFire_suit_chemin() throws Exception
	{
		log.debug("JUnit_HookJauneTest.test_hookAbscisse_takeFire_suit_chemin()", this);
		Assert.assertTrue(!robotvrai.isTient_feu(Cote.GAUCHE));
		Assert.assertTrue(!robotvrai.isTient_feu(Cote.DROIT));
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable takefire = new TakeFire(robotvrai);
		Hook hook = hookgenerator.hook_position(new Vec2(20, 1400));
		hook.ajouter_callback(new Callback(takefire, true));
		hooks.add(hook);
		ArrayList<Vec2> chemin = new ArrayList<Vec2>();
		chemin.add(new Vec2(20, 1400));
		chemin.add(new Vec2(1200, 1400));
		robotvrai.suit_chemin(chemin, hooks);
		Assert.assertTrue(robotvrai.isTient_feu(Cote.GAUCHE));
		Assert.assertTrue(robotvrai.getPosition().distance(new Vec2(1200,1400)) < 5);
	}

	@Test
	public void test_hookFeu_takeFire() throws Exception
	{
		log.debug("JUnit_HookJauneTest.test_hookAbscisse_takeFire()", this);
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		robotvrai.initialiser_actionneurs_deplacements();
		Sleep.sleep(2000);
		Executable takefire = new TakeFire(robotvrai);
		Hook hook = hookgenerator.hook_feu();
		hook.ajouter_callback(new Callback(takefire, false));
		hooks.add(hook);
		robotvrai.avancer(1000, hooks);
	}

	@Test
	public void test_hookAbscisse_leverRateau() throws Exception
	{
		log.debug("JUnit_HookJauneTest.test_hookAbscisse_leverRateau()", this);
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable leverrateau_droit = new LeverRateau(robotvrai, Cote.DROIT);
		Executable leverrateau_gauche = new LeverRateau(robotvrai, Cote.GAUCHE);
		Hook hook = hookgenerator.hook_abscisse(20);
		hook.ajouter_callback(new Callback(leverrateau_droit, true));
		hook.ajouter_callback(new Callback(leverrateau_gauche, true));
		hooks.add(hook);		
		robotvrai.avancer(50, hooks);
	}

	
}
