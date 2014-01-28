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
		log.debug("JUnit_RobotChronoTest.setUp()", this);
		robotchrono = new RobotChrono(config, log);
		robotchrono.setPosition(new Vec2(0, 1500));
		robotchrono.setOrientation(0);
	}

	@Test
	public void test_avancer() throws Exception
	{
		log.debug("JUnit_RobotChronoTest.test_avancer()", this);
		robotchrono.avancer(10);
		Assert.assertTrue(robotchrono.getPosition().equals(new Vec2(10,1500)));
	}

	@Test
	public void test_va_au_point_symetrie() throws Exception
	{
		log.debug("JUnit_RobotChronoTest.test_va_au_point_symetrie()", this);
		config.set("couleur", "jaune");
		robotchrono = new RobotChrono(config, log);
		robotchrono.setPosition(new Vec2(0, 1500));
		robotchrono.setOrientation(0);
		robotchrono.va_au_point(new Vec2(10, 1400));
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(10,1400)) < 2);

		config.set("couleur", "rouge");
		robotchrono = new RobotChrono(config, log);
		robotchrono.setPosition(new Vec2(0, 1500));
		robotchrono.setOrientation(0);
		robotchrono.va_au_point(new Vec2(10, 1400));
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(-10,1400)) < 2);
	}
	
	@Test
	public void test_va_au_point() throws Exception
	{
		log.debug("JUnit_RobotChronoTest.test_va_au_point()", this);
		robotchrono.va_au_point(new Vec2(10, 1400));
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(10,1400)) < 2);
	}

	@Test
	public void test_tourner() throws Exception
	{
		log.debug("JUnit_RobotChronoTest.test_tourner()", this);
		robotchrono.tourner((float)1.2);
		Assert.assertTrue(robotchrono.getOrientation()==(float)1.2);
	}

	@Test
	public void test_suit_chemin() throws Exception
	{
		log.debug("JUnit_RobotChronoTest.test_suit_chemin()", this);
		ArrayList<Vec2> chemin = new ArrayList<Vec2>();
		chemin.add(new Vec2(20, 1400));
		chemin.add(new Vec2(40, 1500));
		robotchrono.suit_chemin(chemin);
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(40,1500)) < 2);
		
	}

	@Test
	public void test_conventions_vitesse() throws Exception
	{
		robotchrono.set_vitesse_rotation("entre_scripts");
		robotchrono.set_vitesse_rotation("recal_faible");
		robotchrono.set_vitesse_rotation("recal_forte");
		robotchrono.set_vitesse_rotation("vitesse_mammouth");
		robotchrono.set_vitesse_rotation("ABWABWA");

		robotchrono.set_vitesse_translation("entre_scripts");
		robotchrono.set_vitesse_translation("recal_faible");
		robotchrono.set_vitesse_translation("recal_forte");
		robotchrono.set_vitesse_translation("vitesse_mammouth");
		robotchrono.set_vitesse_translation("ABWABWA");

	}
	
	@Test
	public void test_actionneurs() throws Exception
	{
		robotchrono.bac_bas();
		robotchrono.bac_haut();
		robotchrono.deposer_fresques();
		robotchrono.isFresquesPosees();
		robotchrono.rateau(PositionRateau.BAS, Cote.DROIT);
		robotchrono.rateau(PositionRateau.BAS, Cote.GAUCHE);
		robotchrono.rateau(PositionRateau.HAUT, Cote.DROIT);
		robotchrono.rateau(PositionRateau.HAUT, Cote.GAUCHE);
		robotchrono.rateau(PositionRateau.RANGER, Cote.DROIT);
		robotchrono.rateau(PositionRateau.RANGER, Cote.GAUCHE);
		robotchrono.rateau(PositionRateau.SUPER_BAS, Cote.DROIT);
		robotchrono.rateau(PositionRateau.SUPER_BAS, Cote.GAUCHE);
		robotchrono.tirerBalles();
		robotchrono.takefire();
		robotchrono.sleep(100);
	}
		
}
