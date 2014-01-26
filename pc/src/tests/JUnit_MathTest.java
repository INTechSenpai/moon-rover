package tests;

import org.junit.Test;
import org.junit.Assert;

import exception.MatriceException;
import smartMath.Matrn;
import smartMath.Vec2;

/**
 * Tests unitaires pour le package smartMath
 * @author pf
 *
 */

public class JUnit_MathTest extends JUnit_Test {

	Vec2 a;
	Vec2 b;
	Matrn y;
	Matrn z;
	
	@Test
	public void test_Vec2() throws Exception
	{
		log.debug("JUnit_MathTest.test_Vec2()", this);
		a = new Vec2(10, 500);
		b = new Vec2(20, -20);
		Assert.assertTrue(a.equals(a));
		Assert.assertTrue(a.PlusNewVector(b).equals(new Vec2(30, 480)));
	}

	@Test
	public void test_add() throws Exception
	{
		log.debug("JUnit_MathTest.test_add()", this);
		y = new Matrn(2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);
		y.setCoeff(3, 1, 0);
		y.setCoeff(4, 1, 1);

		z = new Matrn(2);
		z.setCoeff(5, 0, 0);
		z.setCoeff(8, 0, 1);
		z.setCoeff(2, 1, 0);
		z.setCoeff(12, 1, 1);

		y.addition(z);
		Assert.assertTrue(y.getCoeff(0, 0) == 6);
		Assert.assertTrue(y.getCoeff(0, 1) == 10);
		Assert.assertTrue(y.getCoeff(1, 0) == 5);
		Assert.assertTrue(y.getCoeff(1, 1) == 16);
	}

	@Test
	public void test_mul() throws Exception
	{
		log.debug("JUnit_MathTest.test_mul()", this);
		y = new Matrn(2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);
		y.setCoeff(3, 1, 0);
		y.setCoeff(4, 1, 1);

		z = new Matrn(2);
		z.setCoeff(5, 0, 0);
		z.setCoeff(8, 0, 1);
		z.setCoeff(2, 1, 0);
		z.setCoeff(12, 1, 1);

		y.multiplier(z);
		Assert.assertTrue(y.getCoeff(0, 0) == 9);
		Assert.assertTrue(y.getCoeff(0, 1) == 32);
		Assert.assertTrue(y.getCoeff(1, 0) == 23);
		Assert.assertTrue(y.getCoeff(1, 1) == 72);
	}

	@Test
	public void test_transpose() throws Exception
	{
		log.debug("JUnit_MathTest.test_transpose()", this);
		y = new Matrn(3);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);
		y.setCoeff(12, 0, 2);		
		y.setCoeff(3, 1, 0);
		y.setCoeff(4, 1, 1);
		y.setCoeff(-1, 1, 2);
		y.setCoeff(51, 2, 0);
		y.setCoeff(-12, 2, 1);
		y.setCoeff(0, 2, 2);

		y.transpose();
		Assert.assertTrue(y.getCoeff(0, 0) == 1);
		Assert.assertTrue(y.getCoeff(0, 1) == 3);
		Assert.assertTrue(y.getCoeff(0, 2) == 51);
		Assert.assertTrue(y.getCoeff(1, 0) == 2);
		Assert.assertTrue(y.getCoeff(1, 1) == 4);
		Assert.assertTrue(y.getCoeff(1, 2) == -12);
		Assert.assertTrue(y.getCoeff(2, 0) == 12);
		Assert.assertTrue(y.getCoeff(2, 1) == -1);
		Assert.assertTrue(y.getCoeff(2, 2) == 0);
	}
	
	@Test(expected=MatriceException.class)
	public void test_exception_add() throws Exception
	{
		log.debug("JUnit_MathTest.test_exception_add()", this);
		y = new Matrn(2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);
		y.setCoeff(3, 1, 0);
		y.setCoeff(4, 1, 1);

		z = new Matrn(1);
		z.setCoeff(5, 0, 0);

		y.addition(z);
	}

	@Test(expected=MatriceException.class)
	public void test_exception_mul() throws Exception
	{
		log.debug("JUnit_MathTest.test_exception_mul()", this);
		y = new Matrn(1,2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);

		z = new Matrn(1,1);
		z.setCoeff(5, 0, 0);

		y.multiplier(z);
	}

	@Test(expected=MatriceException.class)
	public void test_exception_transpose() throws Exception
	{
		log.debug("JUnit_MathTest.test_exception_transpose()", this);
		y = new Matrn(1,2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);

		y.transpose();
	}
	
}
