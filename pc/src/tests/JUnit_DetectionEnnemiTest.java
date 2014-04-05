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
		Thread.sleep(300);
		container.demarreThreads();
		Thread.sleep(300);
		Assert.assertEquals(table.nb_obstacles(), 0);
		log.warning("Vous avez 5 secondes pour placer un obstacle devant le robot", this);
		Thread.sleep(6000);
		Assert.assertTrue(table.nb_obstacles() >= 1);
	}
	
	@Test
	public void non_detection_arbre() throws Exception
	{
		log.debug("JUnit_DetectionEnnemiTest.non_detection_arbre()", this);
		robotvrai.setPosition(new Vec2(1300, 1200));
		robotvrai.setOrientation(-(float)Math.PI/2);
		container.demarreThreads();
		Thread.sleep(300);
		Assert.assertTrue(table.nb_obstacles() == 0);
	}

	@Test
	public void non_detection_fresque() throws Exception
	{
		log.debug("JUnit_DetectionEnnemiTest.non_detection_fresque()", this);
		robotvrai.setPosition(new Vec2(0, 1700));
		robotvrai.setOrientation((float)Math.PI/2);
		container.demarreThreads();
		Thread.sleep(300);
		Assert.assertTrue(table.nb_obstacles() == 0);
	}


}
