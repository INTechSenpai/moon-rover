package tests;

import org.junit.Test;

import exception.ContainerException;

	/**
	 * Tests unitaires pour le container
	 * Sert surtout à vérifier l'absence de cycle de dépendances, et d'éventuelles fautes de frappe...
	 * @author pf
	 *
	 */
public class JUnit_ContainerTest extends JUnit_Test {
	
	@Test(expected=ContainerException.class)
	public void test_erreur() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_erreur()", this);
		container.getService("ABWABWA");
	}
	
	@Test
	public void test_log() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_log()", this);
		container.getService("Log");
	}

	@Test
	public void test_config() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_config()", this);
		container.getService("Read_Ini");
	}

	@Test
	public void test_table() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_table()", this);
		container.getService("Table");
	}

	@Test
	public void test_deplacements() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_deplacements()", this);
		container.getService("Deplacements");
	}

	@Test
	public void test_capteurs() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_capteurs()", this);
		container.getService("Capteur");
	}

	@Test
	public void test_actionneurs() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_actionneurs()", this);
		container.getService("Actionneurs");
	}

	@Test
	public void test_HookGenerator() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_HookGenerator()", this);
		container.getService("HookGenerator");
	}

	@Test
	public void test_RobotVrai() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_RobotVrai()", this);
		container.getService("RobotVrai");
	}

	@Test
	public void test_ScriptManager() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_ScriptManager()", this);
		container.getService("ScriptManager");
	}

	@Test
	public void test_Strategie() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_Strategie()", this);
		container.getService("Strategie");
	}

	@Test
	public void test_pathfinding() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_pathfinding()", this);
		container.getService("Pathfinding");
	}

	@Test
	public void test_MemoryManager() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_MemoryManager()", this);
		container.getService("MemoryManager");
	}

	@Test
	public void test_Laser() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_Laser()", this);
		container.getService("Laser");
	}

	@Test
	public void test_FiltrageLaser() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_FiltrageLaser()", this);
		container.getService("FiltrageLaser");
	}

	@Test
	public void test_CheckUp() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_CheckUp()", this);
		container.getService("CheckUp");
	}

	@Test
	public void test_serieAsservissement() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_serieAsservissement()", this);
		container.getService("serieAsservissement");
	}

	@Test
	public void test_serieCapteursActionneurs() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_serieCapteursActionneurs()", this);
		container.getService("serieCapteursActionneurs");
	}

	@Test
	public void test_serieLaser() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_serieLaser()", this);
		container.getService("serieLaser");
	}

	@Test
	public void test_threadTimer() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_threadTimer()", this);
		container.getService("threadTimer");
	}

	@Test
	public void test_threadStrategie() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_threadStrategie()", this);
		container.getService("threadStrategie");
	}

	@Test
	public void test_threadPosition() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_threadPosition()", this);
		container.getService("threadPosition");
	}
	
	@Test
	public void test_threadAnalyseEnnemi() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_threadAnalyseEnnemi()", this);
		container.getService("threadAnalyseEnnemi");
	}
	
	@Test
	public void test_threadCapteurs() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_threadCapteurs()", this);
		container.getService("threadCapteurs");
	}

}
