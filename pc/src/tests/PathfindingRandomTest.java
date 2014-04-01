package tests;

import static org.junit.Assert.*;
import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.HookGenerator;
import hook.methodes.TirerBalles;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.Pathfinding;
import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import smartMath.IntPair;
import smartMath.Vec2;
import table.Table;

public class PathfindingRandomTest extends JUnit_Test
{

	private ScriptManager scriptmanager;
	private Script s;
	private RobotVrai robotvrai;
	private RobotChrono robotchrono;
	private Table table;
	private HookGenerator hookgenerator;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		config.set("couleur", "jaune");
		
		//scriptmanager = (ScriptManager)container.getService("ScriptManager");
		robotvrai = (RobotVrai)container.getService("RobotVrai");
		robotchrono = new RobotChrono(config, log);
		robotchrono.majRobotChrono(robotvrai);
		table = (Table)container.getService("Table");
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		robotvrai.setPosition(new Vec2(1300, 1200));
		robotvrai.setOrientation((float)Math.PI);
		robotvrai.set_vitesse_rotation("entre_scripts");
		robotvrai.set_vitesse_translation("entre_scripts");
		container.getService("threadPosition");
		container.demarreThreads();
		robotvrai.set_vitesse_translation("30");
		robotvrai.avancer(100);
	}

	@Test
	public void test_simple() throws Exception
	{
		/*
		s = (Script)scriptmanager.getScript("ScriptLances");
		Assert.assertTrue(s.score(0, robotvrai, table) == 12);
		robotvrai.tirerBalle();
		Assert.assertTrue(s.score(0, robotvrai, table) == 10);
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robotvrai);
		Hook hook = hookgenerator.hook_abscisse(20);
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);
		robotvrai.avancer(50, hooks);
		*/
		int cmParCase =2;
		Pathfinding finder = new Pathfinding(table, config, log, cmParCase);
		System.out.println("robotvrai.getPosition() : " + robotvrai.getPosition().x + " -- " + robotvrai.getPosition().y);
		ArrayList<Vec2> chemin = finder.chemin(robotvrai.getPosition(), new Vec2(-1100,300));

		
		for(int j = 0; j < chemin.size(); j++)
		{
			Vec2 newpos = new Vec2(0,0);
			newpos.x = robotvrai.getPosition().x +  chemin.get(j).x;
			newpos.y = robotvrai.getPosition().y +  chemin.get(j).y;
			
			robotvrai.va_au_point(newpos);			
		}		
		/*
	    while(true)
	    {
		int cmParCase =2;
		Pathfinding finder = new Pathfinding(table, config, log, cmParCase);
		
		Random randomGenerator = new Random();
		Vec2 arrivee = new Vec2(randomGenerator.nextInt(3000)-1500,randomGenerator.nextInt(2000));
		while (finder.map.canCross((int)((float)(arrivee.x + 1500) / cmParCase /10), (int)((float)(arrivee.y) / cmParCase /10)) == false)
		{
			arrivee.x = randomGenerator.nextInt(3000)-1500;
			arrivee.y = randomGenerator.nextInt(2000); 
		}
		System.out.println("\n\n\n=================== =====   Destination : " + arrivee.x + " - " + arrivee.y + "\n\n\n");
		
		
		
		ArrayList<Vec2> chemin = finder.chemin(robotvrai.getPosition(), arrivee);
		System.out.println("flag1");
		

		String out = "";
		Integer i = 1;
		for (int  j = 0; j < finder.map.getSizeX(); ++j)
		{
			for (int  k = finder.map.getSizeY() - 1; k >= 0; --k)
			{
				System.out.println("flag1");
				
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
				System.out.println("flag1");
				
			}
			
			out +='\n';
		}
		System.out.println(out);
		
		
		for(int j = 0; j < chemin.size(); j++)
		{
			Vec2 newpos = new Vec2(0,0);
			newpos.x = robotvrai.getPosition().x +  chemin.get(j).x;
			newpos.y = robotvrai.getPosition().y +  chemin.get(j).y;
			
			robotvrai.va_au_point(newpos);
			Thread.sleep(1000);
			
		}
	    }*/
	}

	@Test
	public void test_marche_aleatoire() throws Exception
	{
		/*
		s = (Script)scriptmanager.getScript("ScriptLances");
		Assert.assertTrue(s.score(0, robotvrai, table) == 12);
		robotvrai.tirerBalle();
		Assert.assertTrue(s.score(0, robotvrai, table) == 10);
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robotvrai);
		Hook hook = hookgenerator.hook_abscisse(20);
		hook.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook);
		robotvrai.avancer(50, hooks);
		*/

		int cmParCase = 2;
	    Random randomGenerator = new Random();
		Pathfinding finder = new Pathfinding(table, config, log, cmParCase);
		while (true)
		{
		    // g�n�re une deamnde de chemin ou les cases de d�part et d'arriv�e sont valides.
			
			Vec2 arrivee = new Vec2(randomGenerator.nextInt(3000)-1500,randomGenerator.nextInt(2000));
			while (finder.map.canCross((int)((float)(arrivee.x + 1500) / cmParCase /10), (int)((float)(arrivee.y) / cmParCase /10)) == false)
			{
				arrivee.x = randomGenerator.nextInt(3000)-1500;
				arrivee.y = randomGenerator.nextInt(2000); 
			}
			System.out.println("Destination : " + arrivee.x + " - " + arrivee.y);
			
			ArrayList<Vec2> chemin = finder.chemin(robotvrai.getPosition(), arrivee);
			
/*
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
			*/
			for(int m = 0; m < chemin.size(); m++)
			{
				Vec2 newpos = new Vec2(0,0);
				newpos.x = robotvrai.getPosition().x +  chemin.get(m).x;
				newpos.y = robotvrai.getPosition().y +  chemin.get(m).y;
				
				robotvrai.va_au_point(newpos);
			/*
				while(robotvrai.getPosition().distance(newpos) < 10)
	            	Thread.sleep(100);*/
			
			}

			
			Assert.assertTrue(true);
		}

	}
}
