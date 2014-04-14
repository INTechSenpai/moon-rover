package tests;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import pathfinding.Pathfinding;
import robot.RobotVrai;
import smartMath.Vec2;

// TODO

/**
 * Tests du pathfinding
 * @author pf
 *
 */

public class JUnit_PathfindingTest extends JUnit_Test 
{
	
	Pathfinding pathfinding;
	RobotVrai robotvrai;
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		pathfinding = (Pathfinding) container.getService("Pathfinding");
		robotvrai = (RobotVrai) container.getService("RobotVrai");
	}
	
	@Test
	public void itineraire_test() throws Exception
	{
		System.out.println(pathfinding.cheminAStar(new Vec2(-500,1300), new Vec2(1000, 500)).size());
		Assert.assertTrue(pathfinding.cheminAStar(new Vec2(-500,1300), new Vec2(1000, 500)).size() > 1);
	}
	

	@Test
	public void long_itineraire_test() throws Exception
	{
		Random randomgenerator = new Random();
		Vec2 arrivee;
		robotvrai.setPosition(new Vec2(0, 1400));
		while(true)
		{
			arrivee = new Vec2((Math.abs(randomgenerator.nextInt())%3000)-1500, Math.abs(randomgenerator.nextInt())%2000);
			log.debug("Depart: "+robotvrai.getPosition()+", arrivée: "+arrivee, this);
			try {
				robotvrai.va_au_point_pathfinding(pathfinding, arrivee);
			}
			catch(Exception e)
			{
				log.critical(e, this);
			}
		}
	}

}
