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
	}

	@Test
	public void test() throws Exception {
		robotvrai.avancer(100);

	}

}
