package tests;

import org.junit.Before;
import org.junit.Test;

import exception.CollisionException;
import robot.*;
import smartMath.Vec2;
import table.Table;

	/**
	 * Tests unitaires pour les méthodes bas niveau de robot
	 * @author pf
	 *
	 */

public class JUnit_RobotVraiBasNiveauTest extends JUnit_Test {

	private RobotVrai robotvrai;
	private Table table;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_RobotVraiJauneTest.setUp()", this);
		config.set("couleur", "jaune");
		robotvrai = (RobotVrai) container.getService("RobotVrai");
		table = (Table) container.getService("Table");
		robotvrai.setPosition(new Vec2(0, 1500));
		robotvrai.setOrientation(0);
	}

	@Test
	public void test_detecter_collision_non() throws Exception
	{
		log.debug("JUnit_RobotVraiBasNiveauTest.test_detecter_collision_non()", this);
		robotvrai.detecter_collision(true);
	}

	@Test(expected=CollisionException.class)
	public void test_detecter_collision_oui() throws Exception
	{
		log.debug("JUnit_RobotVraiBasNiveauTest.test_detecter_collision_oui()", this);
		table.creer_obstacle(new Vec2(200, 1500));
		robotvrai.detecter_collision(true);		
	}

}