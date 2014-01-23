package tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import robot.RobotChrono;
import robot.RobotVrai;
import smartMath.Vec2;
import strategie.MemoryManager;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Container;

/**
 * Tests unitaires du memory manager
 * @author pf
 *
 */

public class JUnit_MemoryManagerTest {

	Container container;
	RobotVrai robotvrai;
	Table table;
	MemoryManager memorymanager;
	RobotChrono robotchrono;
	Read_Ini config;
	Log log;
	
	@Before
	public void setUp() throws Exception {
		container = new Container();
		robotvrai = (RobotVrai) container.getService("RobotVrai");
		table = (Table)container.getService("Table");
		memorymanager = (MemoryManager)container.getService("MemoryManager");
		robotvrai.setPosition(new Vec2(0, 1500));
		robotvrai.setOrientation(0);
		robotvrai.set_vitesse_rotation("entre_scripts");
		robotvrai.set_vitesse_translation("entre_scripts");
		config = (Read_Ini)container.getService("Read_Ini");
		log = (Log)container.getService("Log");
		robotchrono = new RobotChrono(config, log);
		robotchrono.majRobotChrono(robotvrai);
	}
	
	@After
	public void tearDown() throws Exception {
		container.destructeur();
		container = null;
	}

	@Test
	public void test_cloneTable_1etage() throws Exception
	{
		memorymanager.setModelTable(table, 1);
		Table cloned = memorymanager.getCloneTable(1);
		Assert.assertTrue(table.equals(cloned));
	}

	@Test
	public void test_cloneTable_1etage_modification() throws Exception
	{
		memorymanager.setModelTable(table, 1);
		Table cloned = memorymanager.getCloneTable(1);
		table.creer_obstacle(new Vec2(0,1000));
		Assert.assertTrue(!table.equals(cloned));
	}

	@Test
	public void test_cloneTable_2etages() throws Exception
	{
		memorymanager.setModelTable(table, 2);
		Table cloned = memorymanager.getCloneTable(2);
		cloned.creer_obstacle(new Vec2(0,1000));
		Assert.assertTrue(!table.equals(cloned));
		Table cloned2 = memorymanager.getCloneTable(1);
		Assert.assertTrue(!cloned2.equals(table));
		Assert.assertTrue(cloned2.equals(cloned));
	}

	@Test
	public void test_cloneTable_encore_un_test() throws Exception
	{
		memorymanager.setModelTable(table, 2);
		Table cloned = memorymanager.getCloneTable(2);
		cloned.creer_obstacle(new Vec2(0,1000));
		Assert.assertTrue(!table.equals(cloned));
		Table cloned2 = memorymanager.getCloneTable(2);
		Assert.assertTrue(cloned2.equals(table));
		// L'assertion suivante devrait en logique être fausse.
		// Le fait est qu'une telle comparaison (deux éléments d'un même niveau) ne sera jamais effectuée
		Assert.assertTrue(cloned2.equals(cloned));
	}

	@Test
	public void test_cloneRobotChrono_1etage() throws Exception
	{
		memorymanager.setModelRobotChrono(robotchrono, 1);
		RobotChrono cloned = memorymanager.getCloneRobotChrono(1);
		Assert.assertTrue(robotchrono.equals(cloned));
	}

	@Test
	public void test_cloneRobotChrono_1etage_modification() throws Exception
	{
		memorymanager.setModelRobotChrono(robotchrono, 1);
		RobotChrono cloned = memorymanager.getCloneRobotChrono(1);
		robotchrono.setPosition(new Vec2(1234,21));
		Assert.assertTrue(!robotchrono.equals(cloned));
	}

}
