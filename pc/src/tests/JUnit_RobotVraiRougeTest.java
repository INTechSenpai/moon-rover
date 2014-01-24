package tests;

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
		robotvrai.va_au_point(new Vec2(10, 1400), null, false, 0, false, false, false);
		robotvrai.update_x_y_orientation();
		Assert.assertTrue(robotvrai.getPosition().distance(new Vec2(-10,1400)) < 2);
	}
	

}
