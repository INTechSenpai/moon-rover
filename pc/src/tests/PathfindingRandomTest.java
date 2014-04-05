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
import robot.cartes.Deplacements;
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
		Deplacements deplacements = (Deplacements)container.getService("Deplacements");
		robotchrono = new RobotChrono(config, log);
		robotchrono.majRobotChrono(robotvrai);
		table = (Table)container.getService("Table");
		hookgenerator = (HookGenerator)container.getService("HookGenerator");
		robotvrai.setPosition(new Vec2(1300, 1700));
		robotvrai.setOrientation((float)Math.PI);
		container.getService("threadPosition");
		container.demarreThreads();
		robotvrai.set_vitesse_translation("30");
		robotvrai.recaler();
		robotvrai.set_vitesse_rotation("entre_scripts");
		robotvrai.set_vitesse_translation("entre_scripts");
		//deplacements.set_vitesse_translation(500);		// A ne pas faire en vrai, danger pour les actionneurs
		//deplacements.set_vitesse_rotation(500);
		robotvrai.avancer(600);
		robotvrai.tourner(3.14f);
		robotvrai.avancer(300);
	}

	
	// ===========================================  Va au point spécifié
	@Test
	public void test_simple() throws Exception
	{
		
		// init
		//robotvrai.setPosition(new Vec2(1300, 1200));
				
		
		int cmParCase = 2;
		
		Pathfinding finder = new Pathfinding(table, config, log, cmParCase);

		
		Vec2 arrivee = new Vec2(-1000,500);
		
		
		if (finder.map.canCross((int)((float)(arrivee.x + 1500) / cmParCase /10), (int)((float)(arrivee.y) / cmParCase /10)))
		{
			ArrayList<Vec2> chemin = finder.chemin(robotvrai.getPosition(), arrivee);
			
			if (chemin != null)
			{
				
				
				// suit le teajet
				for(int j = 0; j < chemin.size(); j++)
				{
					Vec2 newpos = new Vec2(0,0);
					newpos.x =  chemin.get(j).x;
					newpos.y =  chemin.get(j).y;
					
					robotvrai.va_au_point(newpos);
					
				}
				
			}
		}
		else
			System.out.println("Arrivee unreachable");
				
	}
	
	
	// ================================ Test de marche aléatoire ========================================

	@Test
	public void test_marche_aleatoire() throws Exception
	{

		int compteTrajets = 0;
		int cmParCase =2;
		
		Pathfinding finder = new Pathfinding(table, config, log, cmParCase);
		Random randomGenerator = new Random();
		
	    while(true)
	    {
			
			Vec2 arrivee = new Vec2(randomGenerator.nextInt(3000)-1500,randomGenerator.nextInt(2000))
					,depart;
			while (finder.map.canCross((int)((float)(arrivee.x + 1500) / cmParCase /10), (int)((float)(arrivee.y) / cmParCase /10)) == false)
			{
				arrivee.x = randomGenerator.nextInt(3000)-1500;
				arrivee.y = randomGenerator.nextInt(2000); 
			}
			depart = robotvrai.getPosition();
			if (finder.map.canCross((int)((float)(depart.x + 1500) / cmParCase /10), (int)((float)(depart.y) / cmParCase /10)) == false)
				System.out.println("depart not crossable");
			

			System.out.println("==========================================================================================================\nNouveau trajet : Depart = " + depart.x + " ; "+ depart.y + "     Arrivée = " + arrivee.x + " ; "+ arrivee.y);
			ArrayList<Vec2> chemin = finder.chemin(depart, arrivee);

			if (chemin != null)
			{
				
				
				/*
				// affiche la feuille de route
				Vec2 newpos = new Vec2(0,0);
				System.out.println("Chemin (test_marche_aleatoire) : ");
				//newpos.x = depart.x +  chemin.get(0).x;
				//.y = depart.y +  chemin.get(0).y;
				System.out.println("pox n°" + 0 + " : " + newpos);
				for(int j = 0; j < chemin.size(); j++)
				{
					newpos.x = chemin.get(j).x;
					newpos.y = chemin.get(j).y;
					System.out.println("pox n°" + j + " : " + newpos);
					
				}
				

				// Affiche le calcul du chemin
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
				
				
				
				// suit le trajet
				Vec2 newpos = new Vec2(0,0);
			/*	newpos.x = depart.x +  chemin.get(0).x;
				newpos.y = depart.y +  chemin.get(0).y;
				System.out.println("Goto : " + newpos);
				robotvrai.va_au_point(newpos);*/
				for(int j = 0; j < chemin.size(); j++)
				{
					newpos.x = chemin.get(j).x;
					newpos.y = chemin.get(j).y;
					

					//System.out.println("Goto : " + newpos);
					robotvrai.va_au_point(newpos);
					//Thread.sleep(1000);
					
				}
				compteTrajets++;

				System.out.println("Trajets effectués : " + compteTrajets);
				
				
			}
	    }

	}
}
