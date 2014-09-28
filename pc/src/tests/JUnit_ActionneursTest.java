package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.cartes.Actionneurs;

public class JUnit_ActionneursTest extends JUnit_Test {

	Actionneurs actionneurs;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_ActionneursTest.setUp()", this);
		actionneurs = (Actionneurs)container.getService("Actionneurs");
	}
	
	// TODO : un test par actionneur
	@Test
	public boolean exempleTest() throws Exception
	{

		Assert.assertTrue( 42 != 1337 );
		return true;
	}
}
