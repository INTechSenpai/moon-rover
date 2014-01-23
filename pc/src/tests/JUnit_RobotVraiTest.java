package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.*;
import robot.cartes.*;
import smartMath.Vec2;
	/**
	 * Tests unitaires pour RobotVrai (non, sans blague...)
	 * @author pf
	 *
	 */
public class JUnit_RobotVraiTest extends JUnit_Test {

	private RobotVrai robotvrai;
	private Deplacements deplacements;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		robotvrai = (RobotVrai) container.getService("RobotVrai");
		deplacements = (Deplacements)container.getService("Deplacements");
		deplacements.set_x(0);
		deplacements.set_y(1500);
		deplacements.set_orientation(0);
		deplacements.set_vitesse_translation(80);
		deplacements.set_vitesse_rotation(130);
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
		float[] infos_float = deplacements.get_infos_x_y_orientation();
		assertEquals(0, infos_float[0], 0);
		assertEquals(1500, infos_float[1], 0);
		assertEquals(1200, infos_float[2], 0);
	}
	
	@Test
	public void test_getOrientation() throws Exception
	{
		deplacements.tourner((float)1.2);
		Thread.sleep(500);
		robotvrai.update_x_y_orientation();
		assertEquals(robotvrai.getOrientation(), 1.2, 0.001);
	}

	@Test
	public void test_update_x_y_orientation() throws Exception
	{
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(0,0)));
		robotvrai.update_x_y_orientation();
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(0,1500)));
	}
	
	@Test
	public void test_avancer() throws Exception
	{
		robotvrai.setOrientation(0);
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		robotvrai.update_x_y_orientation();
		robotvrai.avancer(10);
		robotvrai.update_x_y_orientation();
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(10,1500)));
	}

	@Test
	public void test_va_au_point() throws Exception
	{
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		robotvrai.va_au_point(new Vec2(10, 1400));
		robotvrai.update_x_y_orientation();
		Assert.assertTrue(robotvrai.getPosition().distance(new Vec2(10,1400)) < 2);
	}

	@Test
	public void test_tourner() throws Exception
	{
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		robotvrai.tourner((float)1.2);
		assertEquals(robotvrai.getOrientation(), 1.2, 0.001);
	}

	@Test
	public void test_suit_chemin() throws Exception
	{
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		ArrayList<Vec2> chemin = new ArrayList<Vec2>();
		chemin.add(new Vec2(20, 1400));
		chemin.add(new Vec2(40, 1500));
		robotvrai.suit_chemin(chemin);		
		Assert.assertTrue(robotvrai.getPosition().distance(new Vec2(40,1500)) < 2);
		
	}

}
