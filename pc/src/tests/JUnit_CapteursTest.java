package tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import robot.cartes.Capteurs;

public class JUnit_CapteursTest extends JUnit_Test {

	Capteurs capteurs;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_ActionneursTest.setUp()", this);
		capteurs = (Capteurs)container.getService("Capteurs");
	}
	
	@Test
	public void test_feu() throws Exception
	{
		log.debug("JUnit_CapteursTest.test_feu()", this);
		Assert.assertTrue(!capteurs.isThereFire());
		Assert.assertTrue(!capteurs.isFireRed());
	}


}
