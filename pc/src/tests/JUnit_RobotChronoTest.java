package tests;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.*;
import smartMath.Vec2;
	/**
	 * Tests unitaires pour RobotChrono
	 * @author pf
	 *
	 */
public class JUnit_RobotChronoTest extends JUnit_Test {

	private RobotChrono robotchrono;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		robotchrono = new RobotChrono(config, log);
		robotchrono.setPosition(new Vec2(0, 1500));
		robotchrono.setOrientation(0);
	}

	@Test
	public void test_avancer() throws Exception
	{
		robotchrono.avancer(10);
		Assert.assertTrue(robotchrono.getPosition().equals(new Vec2(10,1500)));
	}

	@Test
	public void test_va_au_point_symetrie() throws Exception
	{
		config.set("couleur", "jaune");
		robotchrono = new RobotChrono(config, log);
		robotchrono.setPosition(new Vec2(0, 1500));
		robotchrono.setOrientation(0);
		robotchrono.va_au_point(new Vec2(10, 1400), null, false, 0, false, true, false);
		System.out.println(robotchrono.getPosition());
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(10,1400)) < 2);

		config.set("couleur", "rouge");
		robotchrono = new RobotChrono(config, log);
		robotchrono.setPosition(new Vec2(0, 1500));
		robotchrono.setOrientation(0);
		robotchrono.va_au_point(new Vec2(10, 1400), null, false, 0, false, true, false);
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(-10,1400)) < 2);
	}
	
	@Test
	public void test_va_au_point() throws Exception
	{
		robotchrono.va_au_point(new Vec2(10, 1400));
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(10,1400)) < 2);
	}

	@Test
	public void test_tourner() throws Exception
	{
		robotchrono.tourner((float)1.2);
		Assert.assertTrue(robotchrono.getOrientation()==(float)1.2);
	}

	@Test
	public void test_suit_chemin() throws Exception
	{
		ArrayList<Vec2> chemin = new ArrayList<Vec2>();
		chemin.add(new Vec2(20, 1400));
		chemin.add(new Vec2(40, 1500));
		robotchrono.suit_chemin(chemin);
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(40,1500)) < 2);
		
	}

}
