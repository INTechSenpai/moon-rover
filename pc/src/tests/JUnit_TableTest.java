package tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import container.Container;
import smartMath.Vec2;
import strategie.MemoryManager;
import table.Table;

	/**
	 * Tests unitaires pour la classe table
	 * @author pf
	 *
	 */

public class JUnit_TableTest {

	private Table table;
	private Container container;
	
	@Before
	public void setUp() throws Exception {
		container = new Container();
		table = (Table)container.getService("Table");
	}
	
	@After
	public void tearDown() throws Exception {
		table = null;
	}
	
	@Test
	public void test_nbrTree() throws Exception {
		Assert.assertTrue(table.nbrLeftTree(1) == 3);
		Assert.assertTrue(table.nbrRightTree(1) == 3);
		Assert.assertTrue(table.nbrTotalTree(1) == 6);
		table.pickTree(1);
		Assert.assertTrue(table.nbrLeftTree(1) == 0);
		Assert.assertTrue(table.nbrRightTree(1) == 0);
		Assert.assertTrue(table.nbrTotalTree(1) == 0);
	}

	@Test
	public void test_nearestTorch() throws Exception {
		Assert.assertTrue(table.nearestFire(new Vec2(300,200)) == 4);
		Assert.assertTrue(table.nearestFire(new Vec2(1300,200)) == 1);
		Assert.assertTrue(table.nearestFire(new Vec2(-300,200)) == 5);
		Assert.assertTrue(table.nearestFire(new Vec2(-1300,200)) == 7);
	}
	
	@Test
	public void benchmark_clone() throws Exception {
		MemoryManager memorymanager = (MemoryManager)container.getService("MemoryManager");
		Table table1;
		for(int i = 0; i < 10000; i++)
		{
			table1 = memorymanager.getCloneTable(1);
		}

	}
	
}
