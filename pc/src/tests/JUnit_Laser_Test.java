package tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import robot.cartes.Laser;
import robot.cartes.laser.FiltrageLaser;
import smartMath.Vec2;

public class JUnit_Laser_Test extends JUnit_Test {

	Laser laser;
	FiltrageLaser filtragelaser;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_Laser_Test.setUp()", this);
		filtragelaser = (FiltrageLaser) container.getService("FiltrageLaser");
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

	@Test
	public void test_position_balise() throws Exception
	{
		log.debug("JUnit_Laser_Test.test_position_balise()", this); // TODO vérifier les valeurs
		Assert.assertTrue(laser.position_balise(0).distance(new Vec2((float)1620,(float)50)) < 500);
		Assert.assertTrue(laser.position_balise(1).distance(new Vec2((float)1620,(float)50)) < 500);
	}
	
	@Test
	public void test_vitesse() throws Exception
	{
		log.debug("JUnit_Laser_Test.test_vitesse()", this);
		Assert.assertTrue(filtragelaser.vitesse().SquaredLength() < 10);
	}
	
}
