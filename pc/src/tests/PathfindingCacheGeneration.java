package tests;

import org.junit.Before;
import org.junit.Test;

import pathfinding.cache.CacheHolder;
import robot.Cote;
import table.Table;

/**
 * Génère les fichiers de cache du pathfinding
 * @author pf, Martial
 *
 */

public class PathfindingCacheGeneration extends JUnit_Test
{
	private Table table;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		table = (Table)container.getService("Table");
	}
	
	@Test
	public void PathfindingCacheGenerationTest1() throws Exception
	{
		log.debug("\n\n ====== Test generation cache pathfinding =====", this);
		CacheHolder.cache_file_generate(config, log, table, "distance-"+table.codeTorches()+".cache");
	}

	@Test
	public void PathfindingCacheGenerationTest2() throws Exception
	{
		log.debug("\n\n ====== Test generation cache pathfinding =====", this);
		table.torche_disparue(Cote.DROIT);
		CacheHolder.cache_file_generate(config, log, table, "distance-"+table.codeTorches()+".cache");
	}

	@Test
	public void PathfindingCacheGenerationTest3() throws Exception
	{
		log.debug("\n\n ====== Test generation cache pathfinding =====", this);
		table.torche_disparue(Cote.GAUCHE);
		CacheHolder.cache_file_generate(config, log, table, "distance-"+table.codeTorches()+".cache");
	}

	@Test
	public void PathfindingCacheGenerationTest4() throws Exception
	{
		log.debug("\n\n ====== Test generation cache pathfinding =====", this);
		table.torche_disparue(Cote.DROIT);
		table.torche_disparue(Cote.GAUCHE);
		CacheHolder.cache_file_generate(config, log, table, "distance-"+table.codeTorches()+".cache");
	}
		
}
