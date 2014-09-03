package tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import robot.cartes.laser.FiltrageLaser;
import robot.cartes.laser.Laser;
import robot.RobotVrai;
import smartMath.Vec2;
import table.Table;
import utils.Sleep;


public class JUnit_Laser_Test extends JUnit_Test {

	Laser laser;
	FiltrageLaser filtragelaser;
	RobotVrai robotvrai;
	Table table;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_Laser_Test.setUp()", this);
		filtragelaser = (FiltrageLaser) container.getService("FiltrageLaser");
		laser = (Laser) container.getService("Laser");
		robotvrai = (RobotVrai) container.getService("RobotVrai");
		table = (Table) container.getService("Table");
	}

	@Test
	public void test_avant_verification() throws Exception
	{
		//Ok
		//Aucune balise n'est allumée
		log.debug("JUnit_Laser_Test.test_avant_verification()", this);
		Assert.assertTrue(laser.balises_actives().size() == 0);
		Assert.assertTrue(laser.balises_ignorees().size() == 2);		
	}

	@Test
	public void test_apres_verification() throws Exception
	{
		//Les balises sont censées être toutes les deux allumées
		log.debug("JUnit_Laser_Test.test_apres_verification()", this);
		laser.allumer();
		Sleep.sleep(3000);
		laser.verifier_balises_connectes();
		Assert.assertTrue(laser.balises_actives().size() == 2);
		Assert.assertTrue(laser.balises_ignorees().size() == 0);
		laser.eteindre();

	}

	@Test
	public void test_coherence() throws Exception
	{
		log.debug("JUnit_Laser_Test.test_coherence()", this);
		//Assert.assertTrue(laser.verifier_balises_connectes() == 1);
		log.debug("Ca raconte quoi sur la cohérence des mesures?", this);
		laser.verifier_coherence_balise();
	}

	@Test
	public void test_pour_kayou() throws Exception
	{
		container.getService("threadLaser");
		container.demarreThreads();
		laser.allumer();
		while(true)
		{
			Sleep.sleep(100);
		}

	}

	@Test
	public void test_on_off() throws Exception
	{
		//Ok
		log.debug("JUnit_Laser_Test.test_on_off()", this);
		laser.allumer();
		Sleep.sleep(2000);
		laser.eteindre();
	}

	@Test
	public void test_position_balise() throws Exception
	{
		log.debug("JUnit_Laser_Test.test_position_balise()", this);
		robotvrai.setOrientation(0);
		robotvrai.setPosition(new Vec2(30,100));
		Vec2 pos_balise0 = new Vec2(0,300);
		Vec2 pos_balise1; //position de la baslise enregistrée
		laser.allumer();
		Sleep.sleep(3000);
		pos_balise1 = laser.position_balise(0);
		//Position incohérente, il faut déjà connaître le sens du laser pour le caler avec le sens du robot
		log.debug("La balise 1 se trouve en ("+pos_balise1+") selon le laser.",this);
		//Il y a réception d'un acquittement, d'où erreur
		float ecart = pos_balise1.distance(pos_balise0);
		log.debug("L'écart est de : "+ecart, this);
		Assert.assertTrue( ecart < 500);
		laser.eteindre();
		//La pile de la seconde balise est vide, il faut la remplacer
		//Assert.assertTrue(laser.position_balise(1).distance(new Vec2(600,)) < 500);
	}

	@Test
	public void test_vitesse() throws Exception
	{
		//Ok
		log.debug("JUnit_Laser_Test.test_vitesse()", this);
		laser.allumer();
		Sleep.sleep(1000);
		Assert.assertTrue(filtragelaser.vitesse().SquaredLength() < 10);
		Sleep.sleep(1000);
		laser.eteindre();
	}
	@Test
	public void test_position_balise_relative() throws Exception
	{
		log.debug("JUnit_Laser_Test.test_position_balise()", this);
		robotvrai.setOrientation(0);
		robotvrai.setPosition(new Vec2(0,300));
		Vec2 pos_balise0 = new Vec2(0,300);
		Vec2 pos_balise1;
		laser.allumer();
		Sleep.sleep(3000);
		pos_balise1 = laser.position_balise_relative(0);
		//Position incohérente, il faut déjà connaître le sens du laser pour le caler avec le sens du robot
		log.debug("La balise 1 se trouve en ("+pos_balise1+") selon le laser.",this);
		//Il y a réception d'un acquittement, d'où erreur
		float ecart = pos_balise1.distance(pos_balise0);
		log.debug("L'écart est de : "+ecart, this);
		Assert.assertTrue( ecart < 500);
		laser.eteindre();
		//La pile de la seconde balise est vide, il faut la remplacer
		//Assert.assertTrue(laser.position_balise(1).distance(new Vec2(600,)) < 500);
	}

}
