package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.*;
import smartMath.Vec2;
import table.Table;

	/**
	 * Tests unitaires pour la détection d'ennemi
	 * @author pf
	 *
	 */
public class JUnit_DetectionEnnemiTest extends JUnit_Test {

	private RobotVrai robotvrai;
	private Table table;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_DetectionEnnemiTest.setUp()", this);
		config.set("couleur", "jaune");
		robotvrai = (RobotVrai) container.getService("RobotVrai");
		table = (Table) container.getService("Table");
		container.getService("threadCapteurs");
		container.getService("threadPosition");
	}

	@Test
	public void test_ajoutObstacle() throws Exception
	{
		log.debug("JUnit_DetectionEnnemiTest.test_ajoutObstacle()", this);
		robotvrai.setPosition(new Vec2(-600, 1410));
		robotvrai.setOrientation((float)-Math.PI/2);
		Thread.sleep(1000);
		container.demarreThreads();
		Thread.sleep(1000);
		Assert.assertEquals(table.nb_obstacles(), 0);
		log.warning("!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this);
		log.warning("Vous avez 5 secondes pour placer un obstacle devant le robot", this);
		Thread.sleep(6000);
		Assert.assertTrue(table.nb_obstacles() >= 1);
	}
	
	@Test
	public void non_detection_arbre() throws Exception
	{
		log.debug("JUnit_DetectionEnnemiTest.non_detection_arbre()", this);
		robotvrai.setPosition(new Vec2(-1300, 1200));
		robotvrai.setOrientation(-(float)Math.PI/2);
		container.demarreThreads();
		Thread.sleep(1000);
		Assert.assertTrue(table.nb_obstacles() == 0);
	}

	@Test
	public void non_detection_fresque() throws Exception
	{
		log.debug("JUnit_DetectionEnnemiTest.non_detection_fresque()", this);
		robotvrai.setPosition(new Vec2(0, 1700));
		robotvrai.setOrientation((float)Math.PI/2);
		container.demarreThreads();
		Thread.sleep(1000);
		Assert.assertTrue(table.nb_obstacles() == 0);
		robotvrai.setPosition(new Vec2(-100, 1700));
		Thread.sleep(1000);
		Assert.assertTrue(table.nb_obstacles() == 0);
		robotvrai.setPosition(new Vec2(100, 1700));
		Thread.sleep(1000);
		Assert.assertTrue(table.nb_obstacles() == 0);
	}

	@Test
	public void non_detection_bac() throws Exception
	{
		log.debug("JUnit_DetectionEnnemiTest.non_detection_bac()", this);
		robotvrai.setPosition(new Vec2(-740, 1500));
		robotvrai.setOrientation((float)Math.PI/2);
		container.demarreThreads();
		Thread.sleep(1000);
		Assert.assertTrue(table.nb_obstacles() == 0);
		robotvrai.setPosition(new Vec2(-450, 1500));
		Thread.sleep(1000);
		Assert.assertTrue(table.nb_obstacles() == 0);
		robotvrai.setPosition(new Vec2(-1170, 1500));
		Thread.sleep(1000);
		Assert.assertTrue(table.nb_obstacles() == 0);
	}

	@Test
	public void non_detection_torche() throws Exception
	{
		log.debug("JUnit_DetectionEnnemiTest.non_detection_torche()", this);
		robotvrai.setPosition(new Vec2(-600, 1200));
		robotvrai.setOrientation(-(float)Math.PI/2);
		container.demarreThreads();
		Thread.sleep(1000);
		Assert.assertTrue(table.nb_obstacles() == 0);
		for(int i = 0; i < 5; i++)
		{
			robotvrai.avancer(-10);
			Thread.sleep(1000);
			Assert.assertTrue(table.nb_obstacles() == 0);
		}
	}


}
