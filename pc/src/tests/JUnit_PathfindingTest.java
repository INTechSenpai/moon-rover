package tests;

import java.util.ArrayList;
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
		robotvrai.setPosition(new Vec2(1000, 900));
		robotvrai.setOrientation(Math.PI/2);
	}
	
	@Test
	public void itineraire_test() throws Exception
	{
		System.out.println(pathfinding.cheminAStar(new Vec2(-500,1300), new Vec2(1000, 500)).size());
		Assert.assertTrue(pathfinding.cheminAStar(new Vec2(-500,1300), new Vec2(1000, 500)).size() > 1);
	}
	
    @Test
    public void pathfinding_test() throws Exception
    {
        pathfinding.update_simple_pathfinding();
        robotvrai.setPosition(new Vec2(1100, 600));
        ArrayList<Vec2> chemin = pathfinding.chemin(robotvrai.getPosition(), new Vec2(-1100, 300));
        robotvrai.suit_chemin(chemin, null);
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
				robotvrai.va_au_point_pathfinding(pathfinding, arrivee, null);
			}
			catch(Exception e)
			{
				log.critical(e, this);
			}
		}
	}

}
