package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import enums.Cote;
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
		Assert.assertTrue(table.nbrTree(1, Cote.DROIT) == 3);
		Assert.assertTrue(table.nbrTree(1, Cote.GAUCHE) == 3);
		Assert.assertTrue(table.nbrTotalTree(1) == 6);
		table.pickTree(1);
		Assert.assertTrue(table.nbrTree(1, Cote.GAUCHE) == 0);
		Assert.assertTrue(table.nbrTree(1, Cote.DROIT) == 0);
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
	public void test_creer_obstacle() throws Exception {
		log.debug("JUnit_TableTest.test_creer_obstacle()", this);
		long ancien_hash = table.hashTable();
		table.creer_obstacle(new Vec2(100, 100));
        Assert.assertNotEquals(ancien_hash, table.hashTable());
	}

	@Test
	public void test_pickFire() throws Exception {
		log.debug("JUnit_TableTest.test_pickFire()", this);
		long ancien_hash = table.hashTable();
		table.pickFire(2);
        Assert.assertNotEquals(ancien_hash, table.hashTable());
	}

	@Test
	public void test_pickTree() throws Exception {
		log.debug("JUnit_TableTest.test_pickTree()", this);
		long ancien_hash = table.hashTable();
		Assert.assertTrue(!table.isTreeTaken(2));
		table.pickTree(2);
        Assert.assertNotEquals(ancien_hash, table.hashTable());
		Assert.assertTrue(table.isTreeTaken(2));
	}

	@Test
	public void test_putFire() throws Exception {
		log.debug("JUnit_TableTest.test_putFire()", this);
		long ancien_hash = table.hashTable();
		table.putFire(2);
        Assert.assertNotEquals(ancien_hash, table.hashTable());
	}
	
	@Test
	public void test_obstacle_existe() throws Exception {
		log.debug("JUnit_TableTest.test_obstacle_existe()", this);		
		Assert.assertTrue(table.obstacle_existe(new Vec2(-517, 900)));
	}


    @Test
    public void test_obstacle_existe2() throws Exception {
        log.debug("JUnit_TableTest.test_obstacle_existe2()", this);
        table.creer_obstacle(new Vec2(100, 100));
        Assert.assertTrue(table.obstaclePresent(new Vec2(120, 120), 100));
    }
    
    @Test
    public void test_clone() throws Exception {
        log.debug("JUnit_TableTest.clone()", this);
        table.pickFire(2);
        table.pickTree(0);
        table.creer_obstacle(new Vec2(100, 100));
        table.putFire(3);
        Table cloned = table.clone();
        Assert.assertEquals(cloned.hashTable(), table.hashTable());        
    }

    @Test
    public void test_equals() throws Exception {
        table.pickFire(2);
        table.pickTree(0);
        table.creer_obstacle(new Vec2(100, 100));
        table.putFire(3);
        Table cloned = table.clone();
        Assert.assertTrue(cloned.equals(table));        
    }
}
