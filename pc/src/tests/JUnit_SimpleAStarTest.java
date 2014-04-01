package tests;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import pathfinding.AStar;
import pathfinding.Pathfinding;
import pathfinding.SearchSpace.Grid2DSpace;
import smartMath.IntPair;
import smartMath.Vec2;
import table.Table;

/**
 * 
 */

/**
 * @author karton
 *
 */
public class JUnit_SimpleAStarTest extends JUnit_Test
{
	Table table;

	@Test
	public void test() throws Exception
	{
		Table table = (Table)container.getService("Table");
		
		int mapSizeX = 300;
		int mapSizeY = 200;


		Grid2DSpace map = new Grid2DSpace(new Vec2(mapSizeX,mapSizeY));
		
		//String mapStr = map.stringForm();
	    Random randomGenerator = new Random();
		
	    
	    // g�n�re une deamnde de chemin ou les cases de d�part et d'arriv�e sont valides.
		IntPair depart = new IntPair(randomGenerator.nextInt(map.getSizeX()),randomGenerator.nextInt(map.getSizeY()));
		while (map.canCross(depart.x, depart.y) == false)
		{
			depart.x = randomGenerator.nextInt(map.getSizeX());
			depart.y = randomGenerator.nextInt(map.getSizeY()); 
		}
		
		IntPair arrivee = new IntPair(randomGenerator.nextInt(map.getSizeX()),randomGenerator.nextInt(map.getSizeY()));
		while (map.canCross(arrivee.x, arrivee.y) == false)
		{
			arrivee.x = randomGenerator.nextInt(map.getSizeX());
			arrivee.y = randomGenerator.nextInt(map.getSizeY()); 
		}

		System.out.println("Calculating path...");
		
		
		
		
		
		// calcule le chemin
		AStar solver = new AStar(map, depart, arrivee);

		ArrayList<IntPair> cheminliss = new ArrayList<IntPair>();
		long duration = 0;
		long Lissduration = 0;
		int testNb = 100;
		for (int i = 0; i< testNb; ++i)
		{
			 // g�n�re une deamnde de chemin ou les cases de départ et d'arrivée sont valides.
			depart = new IntPair(randomGenerator.nextInt(map.getSizeX()),randomGenerator.nextInt(map.getSizeY()));
			while (map.canCross(depart.x, depart.y) == false)
			{
				depart.x = randomGenerator.nextInt(map.getSizeX());
				depart.y = randomGenerator.nextInt(map.getSizeY()); 
			}
			
			arrivee = new IntPair(randomGenerator.nextInt(map.getSizeX()),randomGenerator.nextInt(map.getSizeY()));
			while (map.canCross(arrivee.x, arrivee.y) == false)
			{
				arrivee.x = randomGenerator.nextInt(map.getSizeX());
				arrivee.y = randomGenerator.nextInt(map.getSizeY()); 
			}
			
			solver = new AStar(map, depart, arrivee);
			Pathfinding pathfinder = new Pathfinding(table, config, log, 2);

			long startTime = System.nanoTime();
			solver.process();
			long endTime = System.nanoTime();

			duration += (endTime - startTime)/1000000;

			startTime = System.nanoTime();
			cheminliss = pathfinder.lissage(solver.getChemin(), map);
			endTime = System.nanoTime();
			Lissduration += (endTime - startTime)/1000;
			
		}

		System.out.println("Path done in " + duration/testNb + "ms on average over " + testNb + " random tests.");
		System.out.println("Smoothing done in " + Lissduration/testNb + "µs on average over " + testNb + " random tests.");

		System.out.println("Calculating output...");
		System.out.println("chemin size :" + solver.getChemin().size());
		System.out.println("chemin validity :" + (solver.isValid()));
		
		
		
		

		ArrayList<IntPair> chemin = solver.getChemin();
		String out = "";
		for (int  j = 0; j < mapSizeX; ++j)
		{
			for (int  k = mapSizeY - 1; k >= 0; --k)
			{
				IntPair pos = new IntPair(j,k);
				if (depart.x ==j && depart.y ==k)
					out += 'D';
				else if (arrivee.x ==j && arrivee.y ==k)
					out += 'A';
				else if (cheminliss.contains(pos))
					out += 'O';
				else if (chemin.contains(pos))
					out += '|';
				else if(map.canCross(j, k))
					out += '.';
				else
					out += 'X';	
			}
			
			out +='\n';
		}
		System.out.println(out);
		System.out.println("Legend : A, Arrivée, D, départ, ., on peut passser, X, obstacle, |, chemin prévu et O, sommet du chemin lissé");
	}

}
