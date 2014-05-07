package tests;

import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import smartMath.Vec2;
import strategie.MemoryManager;
import strategie.NoteScriptVersion;
import strategie.Strategie;
import table.Table;
import utils.Sleep;

import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;

import pathfinding.Pathfinding;

/**
 * Tests unitaires de la stratégie
 * @author pf
 *
 */

public class JUnit_StrategieThreadTest extends JUnit_Test {

	private RobotVrai robotvrai;
	private Strategie strategie;
	private MemoryManager memorymanager;
	private Pathfinding pathfinder;
	private Table table;
	private RobotChrono robotchrono;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		robotvrai = (RobotVrai)container.getService("RobotVrai");
		strategie = (Strategie) container.getService("Strategie");
		memorymanager = (MemoryManager) container.getService("MemoryManager");
		pathfinder = (Pathfinding) container.getService("Pathfinding");
		table = (Table) container.getService("Table");
		robotchrono = new RobotChrono(config, log);
		config.set("couleur", "jaune");
		
		table = (Table)container.getService("Table");
		Vec2 initpos = new Vec2(1100,1300);
		robotvrai.setPosition(initpos);
		robotchrono.setPosition(initpos);
		Sleep.sleep(100);
		robotvrai.setOrientation((float)Math.PI);
		robotvrai.set_vitesse_rotation("entre_scripts");
		robotvrai.set_vitesse_translation("entre_scripts");
		container.getService("threadPosition");
		container.getService("threadStrategie");
		robotvrai.setPosition(initpos);
		robotchrono.setPosition(initpos);
		Sleep.sleep(100);
		container.demarreThreads();
		robotvrai.setPosition(initpos);
		robotchrono.setPosition(initpos);
		Sleep.sleep(100);
	}
 
	@Test
	public void test_Thread() throws Exception
	{

		log.debug("Strategie Test Thread : *Start", this);
		
		while(true)
			strategie.boucle_strategie();
	}
}
