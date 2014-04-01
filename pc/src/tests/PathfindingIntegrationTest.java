package tests;

import static org.junit.Assert.*;
import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.HookGenerator;
import hook.methodes.TirerBalles;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.Pathfinding;
import pathfinding.SearchSpace.Grid2DPochoirManager;
import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import smartMath.IntPair;
import smartMath.Vec2;
import table.Table;

public class PathfindingIntegrationTest extends JUnit_Test
{
	private Table table;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		config.set("couleur", "jaune");
	//	robotchrono = new RobotChrono(config, log);
		table = (Table)container.getService("Table");
		table.initialise();
//		container.getService("threadPosition");
	//	container.demarreThreads();
	}

	@Test
	public void test_InstanciationPathfinding() throws Exception
	{
		System.out.println("\n\n ====== Test d'intégration pathfinding =====");
		System.out.println("Calcul d'un même parcours avec des cases de 1cm à 10cm de coté");
		Vec2 depart = new Vec2(1205,1203);
		Vec2 arrivee =  new Vec2(-1100,300);
		new Grid2DPochoirManager();
		Pathfinding finder = new Pathfinding(table, config, log, 2);
		System.out.println(finder.chemin(depart, arrivee));
		for(int i = 1; i < 11; ++i)
		{
			
			
			//	System.out.println(finder.map.stringForm());
		}
		
		
		
		ArrayList<IntPair> chemin = finder.getResult();
		String out = "";
		Integer i = 1;
		for (int  j = 0; j < finder.map.getSizeX(); ++j)
		{
			for (int  k = finder.map.getSizeY() - 1; k >= 0; --k)
			{
				IntPair pos = new IntPair(j,k);
				if (finder.getDepart().x ==j && finder.getDepart().y ==k)
					out += 'D';
				else if (finder.getArrivee().x ==j && finder.getArrivee().y ==k)
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
		
		Assert.assertTrue(true);

	}
}
