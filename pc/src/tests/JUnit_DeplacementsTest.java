package tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import container.Container;
import robot.cartes.*;

	/**
	 * Tests unitaires pour Deplacements
	 * @author pf
	 *
	 */
public class JUnit_DeplacementsTest {

	Container container;
	Deplacements deplacements;
	
	@Before
	public void setUp() throws Exception {
		container = new Container();
		deplacements = (Deplacements)container.getService("Deplacements");
		deplacements.set_x(0);
		deplacements.set_y(1500);
		deplacements.set_orientation(0);
		deplacements.set_vitesse_translation(80);
	}
	
	@After
	public void tearDown() throws Exception {
		container.destructeur();
		container = null;
		deplacements = null;
	}

	@Test
	public void test_infos_xyo() throws Exception
	{
		float[] infos_float = deplacements.get_infos_x_y_orientation();
		Assert.assertTrue(infos_float[0] == 0);
		Assert.assertTrue(infos_float[1] == 1500);
		Assert.assertTrue(infos_float[2] == 0);
	}

	@Test
	public void test_avancer() throws Exception
	{
		deplacements.avancer(10);
		Thread.sleep(1000);
		float[] infos_float = deplacements.get_infos_x_y_orientation();
		Assert.assertTrue(infos_float[0] == 10);
		Assert.assertTrue(infos_float[1] == 1500);
		Assert.assertTrue(infos_float[2] == 0);

	}

	@Test
	public void test_tourner() throws Exception
	{
		deplacements.tourner((float)1.2);
		Thread.sleep(2000);
		float[] infos_float = deplacements.get_infos_x_y_orientation();
		Assert.assertTrue(infos_float[0] == 0);
		Assert.assertTrue(infos_float[1] == 1500);
		Assert.assertTrue(infos_float[2] == 1200);
	}

}
