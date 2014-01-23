package tests;

import static org.junit.Assert.*;
import hook.HookGenerator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import exception.ContainerException;
import table.Table;
import container.Container;
import pathfinding.*;
import robot.*;
import robot.cartes.*;
import robot.serial.*;
import scripts.ScriptManager;
import smartMath.Vec2;
import strategie.*;
	/**
	 * Tests unitaires pour RobotVrai (non, sans blague...)
	 * @author pf
	 *
	 */
public class JUnit_RobotVraiTest {

	Container container;
	RobotVrai robotvrai;
	Deplacements deplacements;
	
	@Before
	public void setUp() throws Exception {
		container = new Container();
		robotvrai = (RobotVrai) container.getService("RobotVrai");
		deplacements = (Deplacements)container.getService("Deplacements");
		deplacements.set_x(0);
		deplacements.set_y(1500);
		deplacements.set_orientation(0);
		deplacements.set_vitesse_translation(80);

	}
	
	@After
	public void tearDown() throws Exception {
		robotvrai = null;
		container.destructeur();
		container = null;
	}


	@Test
	public void test_setPosition() throws Exception
	{
		robotvrai.setPosition(new Vec2(300, 400));
		float[] infos_float = deplacements.get_infos_x_y_orientation();
		Assert.assertTrue(infos_float[0] == 300);
		Assert.assertTrue(infos_float[1] == 400);
		Assert.assertTrue(infos_float[2] == 0);
	}

	@Test
	public void test_setOrientation() throws Exception
	{
		robotvrai.setOrientation((float)1.2);
		Thread.sleep(1000);
		float[] infos_float = deplacements.get_infos_x_y_orientation();
		assertEquals(0, infos_float[0], 0);
		assertEquals(1500, infos_float[1], 0);
		System.out.print(infos_float[2]);
		assertEquals(1200, infos_float[2], 0);
	}

}
