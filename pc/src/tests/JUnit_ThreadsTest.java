package tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.RobotVrai;
import robot.cartes.Deplacements;
import smartMath.Vec2;
import container.Container;

/**
 * Tests unitaires des threads
 * @author pf
 *
 */

public class JUnit_ThreadsTest {

	Container container;
	
	@Before
	public void setUp() throws Exception {
		container = new Container();
	}
	
	@After
	public void tearDown() throws Exception {
		container.arreteThreads();
		container.destructeur();
		container = null;
	}


	@Test
	public void test_threadPosition() throws Exception
	{
		Deplacements deplacements = (Deplacements)container.getService("Deplacements");
		deplacements.set_x(0);
		deplacements.set_y(1500);
		deplacements.set_orientation(0);
		deplacements.set_vitesse_translation(80);
		RobotVrai robotvrai = (RobotVrai) container.getService("RobotVrai");
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(0,0)));
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(0,1500)));	
	}

	@Test
	public void test_arret() throws Exception
	{
		Deplacements deplacements = (Deplacements)container.getService("Deplacements");
		deplacements.set_x(0);
		deplacements.set_y(1500);
		deplacements.set_orientation(0);
		deplacements.set_vitesse_translation(80);
		RobotVrai robotvrai = (RobotVrai) container.getService("RobotVrai");
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(0,0)));
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(0,1500)));
		container.arreteThreads();
		deplacements.set_x(100);
		deplacements.set_y(1400);
		Thread.sleep(100);
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(0,1500)));
	}

	
}
