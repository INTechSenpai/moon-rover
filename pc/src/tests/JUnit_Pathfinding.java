package tests;

import hook.Hook;
import hook.HookFactory;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import astar.AStar;
import astar.MemoryManager;
import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import astar.arcmanager.PathfindingArcManager;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import table.GameElementNames;
import utils.ConfigInfo;
import vec2.ReadWrite;
import vec2.Vec2;
import enums.Tribool;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;

/**
 * Tests unitaires de la recherche de chemin.
 * @author pf
 *
 */

public class JUnit_Pathfinding extends JUnit_Test {

	private AStar<PathfindingArcManager, SegmentTrajectoireCourbe> pathfinding;
	private GameState<RobotChrono> state_chrono;
	private GameState<RobotReal> state;
	private MemoryManager memorymanager;
	private HookFactory hookfactory;
	
	@SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
    	config.set(ConfigInfo.DUREE_PEREMPTION_OBSTACLES, 100);
        pathfinding = (AStar<PathfindingArcManager, SegmentTrajectoireCourbe>) container.getService(ServiceNames.A_STAR_PATHFINDING);
		state = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
		state_chrono = state.cloneGameState();
		memorymanager = (MemoryManager) container.getService(ServiceNames.MEMORY_MANAGER);
		hookfactory = (HookFactory) container.getService(ServiceNames.HOOK_FACTORY);
	}

	@Test(expected=PathfindingRobotInObstacleException.class)
    public void test_robot_dans_obstacle() throws Exception
    {
		state_chrono.robot.setPosition(new Vec2<ReadWrite>(80, 80));
    	state_chrono.gridspace.creer_obstacle(new Vec2<ReadWrite>(80, 80));
    	pathfinding.computePath(state_chrono, PathfindingNodes.values()[0], false);
    }

	@Test(expected=PathfindingException.class)
    public void test_obstacle() throws Exception
    {
		state_chrono.robot.setPosition(new Vec2<ReadWrite>(80, 80));
		state_chrono.gridspace.creer_obstacle(PathfindingNodes.values()[0].getCoordonnees());
    	pathfinding.computePath(state_chrono, PathfindingNodes.values()[0], false);
    }

	@Test
    public void test_brute_force() throws Exception
    {
    	for(PathfindingNodes i: PathfindingNodes.values())
        	for(PathfindingNodes j: PathfindingNodes.values())
        	{
    			state_chrono.robot.reinitDate();
    			state_chrono.robot.setPosition(i.getCoordonnees());
    			pathfinding.computePath(state_chrono, j, true);
        	}
    }
	
	@Test
    public void test_element_jeu_disparu() throws Exception
    {
    	// une fois ces éléments pris, le chemin est libre
    	state_chrono.gridspace.setDone(GameElementNames.PLOT_1, Tribool.TRUE);
    	state_chrono.gridspace.setDone(GameElementNames.PLOT_2, Tribool.TRUE);
    	state_chrono.gridspace.setDone(GameElementNames.VERRE_2, Tribool.TRUE);
    	state_chrono.robot.setPositionPathfinding(PathfindingNodes.CLAP_DROIT_SECOND);
    	pathfinding.computePath(state_chrono, PathfindingNodes.CLAP_DROIT, false);
    }

	@Test(expected=PathfindingException.class)
    public void test_element_jeu_disparu_2() throws Exception
    {
		// Exception car il y a un verre sur le passage
		state_chrono.robot.setPositionPathfinding(PathfindingNodes.CLAP_DROIT_SECOND);
    	pathfinding.computePath(state_chrono, PathfindingNodes.CLAP_DROIT, false);
    }
	
	@Test
    public void test_element_jeu_disparu_3() throws Exception
    {
		// Pas d'exception car on demande au pathfinding de passer sur les éléments de jeux.
		state_chrono.robot.setPositionPathfinding(PathfindingNodes.CLAP_DROIT_SECOND);
    	pathfinding.computePath(state_chrono, PathfindingNodes.CLAP_DROIT, true);
    }

	@Test
    public void test_peremption_pendant_trajet() throws Exception
    {
    	state_chrono.robot.setPosition(new Vec2<ReadWrite>(80, 80));
		state_chrono.gridspace.creer_obstacle(PathfindingNodes.values()[0].getCoordonnees());
    	pathfinding.computePath(state_chrono, PathfindingNodes.values()[0], true);
    }

	@Test
	public void test_pathfinding2() throws Exception
	{
		PathfindingNodes i = PathfindingNodes.SORTIE_ZONE_DEPART;
		PathfindingNodes j = PathfindingNodes.CLAP_GAUCHE;
		state_chrono.robot.setPositionPathfinding(i);
		ArrayList<SegmentTrajectoireCourbe> chemin = pathfinding.computePath(state_chrono, j, true);
		for(SegmentTrajectoireCourbe n: chemin)
			log.debug(n, this);
	}
	
	@Test
    public void test_pathfinding() throws Exception
    {
		Random randomgenerator = new Random();
		PathfindingNodes i = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
		PathfindingNodes j = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
		state_chrono.robot.setPositionPathfinding(i);
		ArrayList<SegmentTrajectoireCourbe> chemin = pathfinding.computePath(state_chrono, j, true);
		for(SegmentTrajectoireCourbe n: chemin)
			log.debug(n, this);
    }

	@Test
    public void test_parcours() throws Exception
    {
		PathfindingNodes i = PathfindingNodes.COTE_MARCHE_DROITE;
		PathfindingNodes j = PathfindingNodes.COTE_MARCHE_GAUCHE;
		state_chrono.robot.setPositionPathfinding(i);
		state.robot.setPosition(i.getCoordonnees());
		ArrayList<SegmentTrajectoireCourbe> chemin = pathfinding.computePath(state_chrono, j, true);
		for(SegmentTrajectoireCourbe n: chemin)
			log.debug(n.objectifFinal+" courbe? "+(n.differenceDistance!=0), this);
		state.robot.suit_chemin(chemin, new ArrayList<Hook>());
    }

	
	@Test
    public void test_memorymanager_vide() throws Exception
    {
		Random randomgenerator = new Random();
		for(int k = 0; k < 100; k++)
		{
//			state_chrono.robot.reinitDate();
			PathfindingNodes i = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
			PathfindingNodes j = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
			state_chrono.robot.setPositionPathfinding(i);
			long old_hash = state_chrono.getHash();
			pathfinding.computePath(state_chrono, j, true);
			Assert.assertEquals(old_hash, state_chrono.getHash());
			Assert.assertTrue(memorymanager.isMemoryManagerEmpty(0));
		}
    }

	@Test
	public void test_hook_chrono_suit_chemin() throws Exception
	{
		state_chrono = state.cloneGameState();
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScriptsChrono(state_chrono, 90000);
		state_chrono.robot.setPosition(PathfindingNodes.BAS.getCoordonnees().plusNewVector(new Vec2<ReadWrite>(10, 10)));
    	ArrayList<SegmentTrajectoireCourbe> chemin = pathfinding.computePath(state_chrono, PathfindingNodes.COTE_MARCHE_DROITE, true);

		ArrayList<Vec2<ReadWrite>> cheminVec2 = new ArrayList<Vec2<ReadWrite>>();
		cheminVec2.add(PathfindingNodes.BAS.getCoordonnees().plusNewVector(new Vec2<ReadWrite>(10, 10)));
		for(SegmentTrajectoireCourbe n: chemin)
		{
			log.debug(n, this);
			cheminVec2.add(n.objectifFinal.getCoordonnees().clone());
		}
    	
		Assert.assertEquals(PathfindingNodes.BAS.getCoordonnees().plusNewVector(new Vec2<ReadWrite>(10, 10)), state_chrono.robot.getPosition());
		Assert.assertTrue(state_chrono.gridspace.isDone(GameElementNames.PLOT_6) == Tribool.FALSE);
		state_chrono.robot.suit_chemin(chemin, hooks_table);
		Assert.assertTrue(state_chrono.gridspace.isDone(GameElementNames.PLOT_6) == Tribool.TRUE);
		Assert.assertEquals(PathfindingNodes.COTE_MARCHE_DROITE.getCoordonnees(), state_chrono.robot.getPosition());
		
    	// on vérifie qu'à présent qu'on a emprunté ce chemin, il n'y a plus d'élément de jeu dessus et donc qu'on peut demander un pathfinding sans exception
    	state_chrono.robot.setPosition(PathfindingNodes.BAS.getCoordonnees());
    	pathfinding.computePath(state_chrono, PathfindingNodes.COTE_MARCHE_DROITE, false);
	}


}
