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
import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
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
		
		scriptmanager = (ScriptManager)container.getService("ScriptManager");
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

		Pathfinding finder = new Pathfinding(table, config, log, 1);
		ArrayList<Vec2> chemin = finder.chemin(robotvrai.getPosition(), new Vec2(-1100,350));
		
		for(int i = 0; i < chemin.size(); i++)
		{
			Vec2 newpos = new Vec2(0,0);
			newpos.x = robotvrai.getPosition().x +  chemin.get(i).x;
			newpos.y = robotvrai.getPosition().y +  chemin.get(i).y;
			
			robotvrai.va_au_point(newpos);
		}
		Assert.assertTrue(true);

	}

}
