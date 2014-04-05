package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import container.Container;
import smartMath.Vec2;
import robot.Cote;
import robot.RobotChrono;
import robot.RobotVrai;
import table.Table;


public class JUnit_RecalageTest extends JUnit_Test {

	private RobotVrai robotvrai;
	private RobotChrono robotchrono;
	private Table table;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		config.set("couleur", "jaune");
		
		robotvrai = (RobotVrai)container.getService("RobotVrai");
		robotchrono = new RobotChrono(config, log);
		robotchrono.majRobotChrono(robotvrai);
		table = (Table)container.getService("Table");
		robotvrai.setPosition(new Vec2(1300, 1700));
		
		robotvrai.setOrientation((float)(Math.PI));
		robotvrai.set_vitesse_rotation("entre_scripts");
		robotvrai.set_vitesse_translation("entre_scripts");
		container.getService("threadPosition");
		container.demarreThreads();
	}
	public void test_recaler()
	{
		robotvrai.setOrientation((float)Math.PI);
		robotvrai.recaler();
	}

}
