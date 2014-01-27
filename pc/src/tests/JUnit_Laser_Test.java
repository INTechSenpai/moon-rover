package tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import robot.cartes.Laser;

public class JUnit_Laser_Test extends JUnit_Test {

	Laser laser;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_Laser_Test.setUp()", this);
		laser = (Laser) container.getService("Laser");
		
	}
	
	@Test
	public void test_avant_verification() throws Exception
	{
		log.debug("JUnit_Laser_Test.test_avant_verification()", this);
		Assert.assertTrue(laser.balises_actives().size() == 0);
		Assert.assertTrue(laser.balises_ignorees().size() == 2);		
	}

	@Test
	public void test_apres_verification() throws Exception
	{
		log.debug("JUnit_Laser_Test.test_apres_verification()", this);
		laser.verifier_balises_connectes();
		Assert.assertTrue(laser.balises_actives().size() == 2);
		Assert.assertTrue(laser.balises_ignorees().size() == 0);		
	}

	@Test
	public void test_coherence() throws Exception
	{
		log.debug("JUnit_Laser_Test.test_coherence()", this);
		laser.verifier_balises_connectes();
		laser.verifier_coherence_balise();
	}
	
	@Test
	public void test_on_off() throws Exception
	{
		log.debug("JUnit_Laser_Test.test_on_off()", this);
		laser.allumer();
		laser.eteindre();
	}

}
