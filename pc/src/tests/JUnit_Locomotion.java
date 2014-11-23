package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.cards.*;
import utils.Sleep;

/**
 * Tests unitaires pour Deplacements
 * @author pf
 *
 */
public class JUnit_Locomotion extends JUnit_Test
{

	private LocomotionCardWrapper deplacements;
	
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		log.debug("JUnit_DeplacementsTest.setUp()", this);
		deplacements = (LocomotionCardWrapper)container.getService("Deplacements");
		deplacements.set_x(0);
		deplacements.set_y(1500);
		deplacements.set_orientation(0);
		deplacements.set_vitesse_translation(80);
	}
	
	@Test
	public void test_infos_xyo() throws Exception
	{
		log.debug("JUnit_DeplacementsTest.test_infos_xyo()", this);
		double[] infos_float = deplacements.get_infos_x_y_orientation();
		Assert.assertTrue(infos_float[0] == 0);
		Assert.assertTrue(infos_float[1] == 1500);
		Assert.assertTrue(infos_float[2] == 0);
	}

	@Test
	public void test_avancer() throws Exception
	{
		log.debug("JUnit_DeplacementsTest.test_avancer()", this);
		deplacements.avancer(100);
		Thread.sleep(1000);
		double[] infos_float = deplacements.get_infos_x_y_orientation();
		Assert.assertEquals(100, infos_float[0], 5);
		Assert.assertEquals(1500, infos_float[1], 5);
		Assert.assertEquals(0, infos_float[2], 50);

	}

	@Test
	public void test_tourner() throws Exception
	{
	    deplacements.activer_asservissement_rotation();
        deplacements.activer_asservissement_translation();
		log.debug("JUnit_DeplacementsTest.test_tourner()", this);
		System.out.println("Avant tourner");
		deplacements.turn((float)1.2);
        System.out.println("Après tourner");
		Thread.sleep(2000);
		double[] infos_float = deplacements.get_infos_x_y_orientation();
		Assert.assertEquals(0, infos_float[0], 5);
		Assert.assertEquals(1500, infos_float[1], 5);
		Assert.assertEquals(1200, infos_float[2], 50);
	}
	
	@Test
	public void test_set_x() throws Exception
	{
		log.debug("JUnit_DeplacementsTest.test_set_x()", this);
		deplacements.set_x(30);
		double[] infos_float = deplacements.get_infos_x_y_orientation();
		Assert.assertTrue(infos_float[0] == 30);
		Assert.assertTrue(infos_float[1] == 1500);
		Assert.assertTrue(infos_float[2] == 0);
	}

	@Test
	public void test_set_y() throws Exception
	{
		log.debug("JUnit_DeplacementsTest.test_set_y()", this);
		deplacements.set_y(330);
		double[] infos_float = deplacements.get_infos_x_y_orientation();
		Assert.assertTrue(infos_float[0] == 0);
		Assert.assertTrue(infos_float[1] == 330);
		Assert.assertTrue(infos_float[2] == 0);
	}
	
	@Test
	public void test_set_orientation() throws Exception
	{
		log.debug("JUnit_DeplacementsTest.test_set_orientation()", this);
		deplacements.set_orientation(1.234f);
		double[] infos_float = deplacements.get_infos_x_y_orientation();
		Assert.assertTrue(infos_float[0] == 0);
		Assert.assertTrue(infos_float[1] == 1500);
		Assert.assertTrue(infos_float[2] > 1233 && infos_float[2] < 1235);
	}

	@Test
	public void test_equilibrage() throws Exception
	{
	    deplacements.set_vitesse_translation(170);
	    deplacements.desactiver_asservissement_rotation();
	    deplacements.avancer(500);
        deplacements.activer_asservissement_rotation();
	    Sleep.sleep(1000);
	}
	
}
