package tests;

import hook.sortes.HookGenerator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import enums.Vitesse;
import robot.RobotVrai;
import smartMath.Vec2;

/**
 * Tests unitaires des hooks (en rouge: avec sym√©trie)
 * @author pf
 *
 */

public class JUnit_HookRougeTest extends JUnit_Test {

	private RobotVrai robotvrai;
	@SuppressWarnings("unused")
	private HookGenerator hookgenerator;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_HookRougeTest.setUp()", this);
		config.set("couleur", "rouge");
		robotvrai = (RobotVrai) container.getService("RobotVrai");
		robotvrai.setPosition(new Vec2(0, 1500));
		robotvrai.setOrientation(0);
        robotvrai.set_vitesse(Vitesse.ENTRE_SCRIPTS);
	}
	
	// TODO Ècrire un test par type de hook
	@Test
	public void test_hookAbscisse_avancer() throws Exception
	{
		Assert.assertTrue(666 != 42);
	}
	
}
