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
		capteurs = (Capteurs)container.getService("Capteur");
		config.set("capteurs_on", true);
		capteurs.maj_config();
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

		// Avec capteurs
		log.debug(capteurs.mesurer(), this);
	//	Assert.assertTrue(capteurs.mesurer_infrarouge() != 3000);
		Assert.assertTrue(capteurs.mesurer() != 3000);

		// Sans capteurs
		config.set("capteurs_on", false);
		capteurs.maj_config();
		log.debug(capteurs.mesurer(), this);
	//	Assert.assertTrue(capteurs.mesurer_infrarouge() == 3000);
		Assert.assertTrue(capteurs.mesurer() == 3000);

		// Et re avec
		config.set("capteurs_on", true);
		capteurs.maj_config();
		log.debug(capteurs.mesurer(), this);
	//	Assert.assertTrue(capteurs.mesurer_infrarouge() != 3000);
		Assert.assertTrue(capteurs.mesurer() != 3000);

	}

/*    @Test
    public void faux_test() throws Exception
    {
        config.set("capteurs_on", true);
        for(int i = 0; i < 10000; i++)
        {
            System.out.println(capteurs.mesurer_ultrason());
            Sleep.sleep(100);
        }
    }*/
}
