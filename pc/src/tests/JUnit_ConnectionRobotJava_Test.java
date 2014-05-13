
package tests;

import org.junit.Before;
import org.junit.Test;

import enums.Vitesse;
import robot.RobotVrai;
import smartMath.Vec2;

/**
 * Tests unitaires des scripts
 * @author pf
 *
 */

public class JUnit_ConnectionRobotJava_Test  extends JUnit_Test {

	private RobotVrai robotvrai;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		config.set("couleur", "jaune");
		
		robotvrai = (RobotVrai)container.getService("RobotVrai");
		robotvrai.setPosition(new Vec2(1251, 1695));
		//On démarre avec la cale !!!!
		robotvrai.setOrientation((float)(-Math.PI/2));
		robotvrai.set_vitesse(Vitesse.ENTRE_SCRIPTS);
		container.getService("threadPosition");
		container.demarreThreads();
		
	}
	@Test
	public void test_bidon() throws Exception
	{
		robotvrai.avancer(100);
		robotvrai.avancer(-100);
	}

}
