package tests;

import static org.junit.Assert.*;
import hook.HookGenerator;

import org.junit.Before;
import org.junit.Test;

import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import smartMath.Vec2;
import table.Table;

public class JUnit_Demo extends JUnit_Test {

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
		robotvrai.setOrientation((float)Math.PI);
		robotvrai.set_vitesse_rotation("entre_scripts");
		robotvrai.set_vitesse_translation("entre_scripts");
		container.getService("threadPosition");
		container.demarreThreads();
		robotvrai.set_vitesse_translation("30");
		robotvrai.setPosition(new Vec2(1300, 1200));
	}

	@Test
	public void defile() throws Exception {
		robotvrai.avancer(1000);
		robotvrai.tourner_relatif(-((float)Math.PI));
		robotvrai.avancer(1000);
	}
	
	@Test
	public void arbre() throws Exception {
		robotvrai.avancer(500);
/*		robotvrai.tourner(-((float)(Math.PI /2)));
		robotvrai.avancer(700);
*/		s = (Script)scriptmanager.getScript("ScriptTree");
		s.agit(1, robotvrai, table, true);
	}

}
