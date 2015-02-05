package tests;

import obstacles.ObstacleManager;

import org.junit.Before;
import org.junit.Test;

import pathfinding.GridSpace;
import pathfinding.Pathfinding;
import smartMath.Vec2;
import enums.PathfindingNodes;
import enums.ServiceNames;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;

/**
 * Tests unitaires de la recherche de chemin.
 * @author pf
 *
 */

// TODO: ajouter des tests avec des vérifications de chemin

public class JUnit_Pathfinding extends JUnit_Test {

	private Pathfinding pathfinding;
	private ObstacleManager obstaclemanager;
	private GridSpace gridspace;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = (Pathfinding) container.getService(ServiceNames.PATHFINDING);
		obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
		gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
    }

	@Test(expected=PathfindingRobotInObstacleException.class)
    public void test_robot_dans_obstacle() throws Exception
    {
    	obstaclemanager.creer_obstacle(new Vec2(100, 100));
    	pathfinding.computePath(new Vec2(80, 80), PathfindingNodes.COIN1, gridspace);
    }

	@Test(expected=PathfindingException.class)
    public void test_obstacle() throws Exception
    {
    	obstaclemanager.creer_obstacle(PathfindingNodes.COIN1.getCoordonnees());
    	pathfinding.computePath(new Vec2(80, 80), PathfindingNodes.COIN1, gridspace);
    }
	
}
