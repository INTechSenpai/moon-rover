package tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import smartMath.Vec2;

/**
 * Tests unitaires pour le package smartMath
 * @author pf
 *
 */

public class JUnit_MathTest {

	Vec2 a;
	Vec2 b;
	
	@Before
	public void setUp() throws Exception {
		a = new Vec2(10, 500);
		b = new Vec2(20, -20);
	}

	@Test
	public void test_Vec2() throws Exception
	{
		Assert.assertTrue(a.equals(a));
		Assert.assertTrue(a.PlusNewVector(b).equals(new Vec2(30, 480)));
	}

	// TODO test de matrices
	
}
