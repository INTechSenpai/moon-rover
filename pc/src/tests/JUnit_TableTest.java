package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import smartMath.Vec2;
import table.Table;

	/**
	 * Tests unitaires pour la classe table
	 * @author pf
	 *
	 */

public class JUnit_TableTest extends JUnit_Test {

	private Table table;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_TableTest.setUp()", this);
		table = (Table)container.getService("Table");
	}
	
	@Test
	public void test_nbrTree() throws Exception {
		log.debug("JUnit_TableTest.test_nbrTree()", this);
		Assert.assertTrue(table.nbrLeftTree(1) == 3);
		Assert.assertTrue(table.nbrRightTree(1) == 3);
		Assert.assertTrue(table.nbrTotalTree(1) == 6);
		table.pickTree(1);
		Assert.assertTrue(table.nbrLeftTree(1) == 0);
		Assert.assertTrue(table.nbrRightTree(1) == 0);
		Assert.assertTrue(table.nbrTotalTree(1) == 0);
	}

	@Test
	public void test_nearestTorch() throws Exception
	{
		log.debug("JUnit_TableTest.test_nearestTorch()", this);
		Assert.assertTrue(table.nearestTorch(new Vec2(300,200)) == 0);
		Assert.assertTrue(table.nearestTorch(new Vec2(1300,200)) == 0);
		Assert.assertTrue(table.nearestTorch(new Vec2(-300,200)) == 1);
		Assert.assertTrue(table.nearestTorch(new Vec2(-1300,200)) == 1);
	}

	@Test
	public void test_nearestFire() throws Exception {
		log.debug("JUnit_TableTest.test_nearestFire()", this);
		// TODO
	}

	@Test
	public void test_creer_obstacle() throws Exception {
		log.debug("JUnit_TableTest.test_creer_obstacle()", this);
		int ancien_hash = table.hashTable();
		table.creer_obstacle(new Vec2(100, 100));
		Assert.assertTrue(ancien_hash != table.hashTable());
	}

	@Test
	public void test_pickFire() throws Exception {
		log.debug("JUnit_TableTest.test_pickFire()", this);
		int ancien_hash = table.hashTable();
		table.pickFire(2);
		Assert.assertTrue(ancien_hash != table.hashTable());
	}

	@Test
	public void test_pickTree() throws Exception {
		log.debug("JUnit_TableTest.test_pickTree()", this);
		int ancien_hash = table.hashTable();
		Assert.assertTrue(!table.isTreeTaken(2));
		table.pickTree(2);
		Assert.assertTrue(ancien_hash != table.hashTable());
		Assert.assertTrue(table.isTreeTaken(2));
	}

	@Test
	public void test_putFire() throws Exception {
		log.debug("JUnit_TableTest.test_putFire()", this);
		int ancien_hash = table.hashTable();
		table.putFire(2);
		Assert.assertTrue(ancien_hash != table.hashTable());
	}

}
