package tests;

import org.junit.Before;
import org.junit.Test;

import robot.cartes.Actionneurs;

public class JUnit_ActionneursTest extends JUnit_Test {

	Actionneurs actionneurs;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_ActionneursTest.setUp()", this);
		actionneurs = (Actionneurs)container.getService("Actionneurs");
	}
	
	@Test
	public void test_bac() throws Exception
	{
		log.debug("JUnit_ActionneursTest.test_bac()", this);
		actionneurs.bac_bas();
		Thread.sleep(500);
		actionneurs.bac_haut();
		Thread.sleep(500);
		actionneurs.bac_bas();
		Thread.sleep(500);
	}

	@Test
	public void test_pinces() throws Exception
	{
		log.debug("JUnit_ActionneursTest.test_pinces()", this);
		actionneurs.ouvrir_pince_gauche();
		Thread.sleep(500);
		actionneurs.ouvrir_pince_droite();
		Thread.sleep(500);
		actionneurs.fermer_pince_gauche();
		Thread.sleep(500);
		actionneurs.fermer_pince_droite();
		Thread.sleep(500);
		actionneurs.milieu_pince_gauche();
		Thread.sleep(500);
		actionneurs.milieu_pince_droite();
		Thread.sleep(500);
		actionneurs.lever_pince_gauche();
		Thread.sleep(500);
		actionneurs.lever_pince_droite();
		Thread.sleep(500);
		actionneurs.baisser_pince_gauche();	
		Thread.sleep(500);
		actionneurs.baisser_pince_droite();
		Thread.sleep(500);
	}
	
	@Test
	public void test_rateau() throws Exception
	{
		log.debug("JUnit_ActionneursTest.test_rateau()", this);
		actionneurs.rateau_super_bas_droit();
		Thread.sleep(500);
		actionneurs.rateau_super_bas_gauche();
		Thread.sleep(500);
		actionneurs.rateau_bas_droit();
		Thread.sleep(500);
		actionneurs.rateau_bas_gauche();
		Thread.sleep(500);
		actionneurs.rateau_haut_droit();
		Thread.sleep(500);
		actionneurs.rateau_haut_gauche();
		Thread.sleep(500);
		actionneurs.rateau_ranger_droit();
		Thread.sleep(500);
		actionneurs.rateau_ranger_gauche();
		Thread.sleep(500);
	}	

}
