package tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.RobotVrai;
import robot.cartes.Deplacements;
import smartMath.Vec2;

/**
 * Tests de robotvrai lorsqu'il est rouge (en particulier pour les symétries)
 * @author pf
 *
 */

public class JUnit_RobotVraiRougeTest extends JUnit_Test {

	// TODO vérifier les symétries
	
	private RobotVrai robotvrai;
	private Deplacements deplacements;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		config.set("couleur", "rouge");
		robotvrai = (RobotVrai) container.getService("RobotVrai");
		deplacements = (Deplacements)container.getService("Deplacements");
		deplacements.set_x(0);
		deplacements.set_y(1500);
		deplacements.set_orientation(0);
		deplacements.set_vitesse_translation(80);
		deplacements.set_vitesse_rotation(130);
	}

	@Test
	public void test_va_au_point_symetrie() throws Exception
	{
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		robotvrai.va_au_point(new Vec2(10, 1400));
		robotvrai.update_x_y_orientation();
		Assert.assertTrue(robotvrai.getPosition().distance(new Vec2(-10,1400)) < 2);
	}

	@Test
	public void test_suit_chemin_symetrie() throws Exception
	{
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		ArrayList<Vec2> chemin = new ArrayList<Vec2>();
		chemin.add(new Vec2(20, 1400));
		chemin.add(new Vec2(40, 1500));
		robotvrai.suit_chemin_droit(chemin);
		Assert.assertTrue(robotvrai.getPosition().distance(new Vec2(-40,1500)) < 2);
	}

	@Test
	public void test_update_x_y_orientation() throws Exception
	{
		robotvrai.update_x_y_orientation();
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(0,1500)));
		deplacements.set_x(100);
		deplacements.set_y(1400);
		robotvrai.update_x_y_orientation();
		Assert.assertTrue(robotvrai.getPosition().equals(new Vec2(100,1400)));
	}
	
	@Test
	public void test_tourner_symetrie() throws Exception
	{
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		robotvrai.tourner((float)1.2);
		assertEquals(robotvrai.getOrientation(), Math.PI-1.2, 0.001);
	}

	@Test
	public void test_tourner_symetrie_2() throws Exception
	{
		container.getService("threadPosition");
		container.demarreThreads();
		Thread.sleep(100);
		robotvrai.tourner_sans_symetrie((float)1.2);
		assertEquals(robotvrai.getOrientation(), 1.2, 0.001);
	}

	@Test
	public void test_avancer_symetrie() throws Exception
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

}
