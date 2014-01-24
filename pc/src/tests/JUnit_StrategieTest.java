package tests;

import robot.RobotVrai;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests unitaires de la stratégie
 * @author pf
 *
 */

public class JUnit_StrategieTest extends JUnit_Test {

	private RobotVrai robotvrai;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		robotvrai = (RobotVrai)container.getService("RobotVrai");
	}

}
