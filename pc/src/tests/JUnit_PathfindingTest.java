package tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import pathfinding.Pathfinding;
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
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		pathfinding = (Pathfinding) container.getService("Pathfinding");
	}
	
	@Test
	public void itineraire_test() throws Exception
	{
		Assert.assertTrue(pathfinding.chemin(new Vec2(-500,1000), new Vec2(500, 1000)).size() > 1);
	}
	
	
}
