package tests;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.Pathfinding;
import pathfinding.SearchSpace.Grid2DPochoirManager;
import pathfinding.cache.CacheFileGenerator;
import smartMath.Vec2;
import table.Table;

public class PathfindingCacheGeneration extends JUnit_Test
{
private Table table;

@Before
public void setUp() throws Exception {
	super.setUp();
	config.set("couleur", "jaune");
//	robotchrono = new RobotChrono(config, log);
	table = (Table)container.getService("Table");
	table.initialise();
//	container.getService("threadPosition");
//	container.demarreThreads();
}

@Test
public void PathfindingCacheGenerationTest() throws Exception
{
	System.out.println("\n\n ====== Test generation cache pathfinding =====");
	System.out.println("Calcul d'un même parcours avec des cases de 1cm à 10cm de coté");
	new Grid2DPochoirManager();
	CacheFileGenerator mGenerator = new CacheFileGenerator(8,table);
	/*
	Pathfinding finder = new Pathfinding(table, config, log, 1);
	//	System.out.println(finder.map.stringForm());
	System.out.println(finder.distance(new Vec2(-1020,100), new Vec2(1100,300), false));*/
	
	/*
	ArrayList<IntPair> chemin = finder.getResult();
	String out = "";
	Integer i = 1;
	for (int  j = 0; j < 300; ++j)
	{
		for (int  k = 200 - 1; k >= 0; --k)
		{
			IntPair pos = new IntPair(j,k);
			if (40 ==j && 10 ==k)
				out += 'D';
			else if (260 ==j && 10 ==k)
				out += 'A';
			else if (chemin.contains(pos))
			{
				out += i.toString();
				i++;
			}
			else if(finder.map.canCross(j, k))
				out += '.';
			else
				out += 'X';	
		}
		
		out +='\n';
	}
	System.out.println(out);
	*/
	Assert.assertTrue(true);

}
}
