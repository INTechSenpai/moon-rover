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
		// TODO
		log.debug("JUnit_CapteursTest.test_feu()", this);
		Assert.assertTrue(!capteurs.isThereFireDroit());
		Assert.assertTrue(!capteurs.isFireRedDroit());
	}

	@Test
	public void desactivation_capteur() throws Exception
	{
		log.debug("JUnit_CapteursTest.desactivation_capteur()", this);
		config.set("capteurs_on", true);
		Assert.assertTrue(capteurs.mesurer_infrarouge() != 3000);
		config.set("capteurs_on", false);
		Assert.assertTrue(capteurs.mesurer_infrarouge() == 3000);
	}


}
