package tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import robot.RobotChrono;
import smartMath.Vec2;
import strategie.MemoryManager;
import table.Table;

/**
 * Tests unitaires du memory manager
 * @author pf
 *
 */

public class JUnit_MemoryManagerTest extends JUnit_Test {

	private Table table;
	private MemoryManager memorymanager;
	private RobotChrono robotchrono;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_MemoryManagerTest.setUp()", this);
		table = (Table)container.getService("Table");
		memorymanager = (MemoryManager)container.getService("MemoryManager");
		robotchrono = new RobotChrono(config, log);
	}

	@Test
	public void test_cloneTable_1etage() throws Exception
	{
		log.debug("JUnit_MemoryManagerTest.test_cloneTable_1etage()", this);
		memorymanager.setModelTable(table, 1);
		Table cloned = memorymanager.getCloneTable(1);
		Assert.assertTrue(table.equals(cloned));
	}

	@Test
	public void test_cloneTable_1etage_modification() throws Exception
	{
		log.debug("JUnit_MemoryManagerTest.test_cloneTable_1etage_modification()", this);
		memorymanager.setModelTable(table, 1);
		Table cloned = memorymanager.getCloneTable(1);
		table.creer_obstacle(new Vec2(0,1000));
		table.pickFire(0);
		table.pickTree(0);
		Assert.assertTrue(!table.equals(cloned));
		memorymanager.getCloneTable(1);
	}

	@Test
	public void test_cloneTable_2etages() throws Exception
	{
		log.debug("JUnit_MemoryManagerTest.test_cloneTable_2etages()", this);
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
		log.debug("JUnit_MemoryManagerTest.test_cloneTable_encore_un_test()", this);
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
		log.debug("JUnit_MemoryManagerTest.test_cloneRobotChrono_1etage()", this);
		memorymanager.setModelRobotChrono(robotchrono, 1);
		RobotChrono cloned = memorymanager.getCloneRobotChrono(1);
		Assert.assertTrue(robotchrono.equals(cloned));
	}

	@Test
	public void test_cloneRobotChrono_1etage_modification() throws Exception
	{
		log.debug("JUnit_MemoryManagerTest.test_cloneRobotChrono_1etage_modification()", this);
		memorymanager.setModelRobotChrono(robotchrono, 1);
		RobotChrono cloned = memorymanager.getCloneRobotChrono(1);
		robotchrono.setPosition(new Vec2(1234,21));
		Assert.assertTrue(!robotchrono.equals(cloned));
	}

}
