package tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import exception.ContainerException;
import container.Container;

	/**
	 * Tests unitaires pour le container
	 * Sert surtout à vérifier l'absence de cycle de dépendances, et d'éventuelles fautes de frappe...
	 * @author pf
	 *
	 */
public class JUnit_ContainerTest {

	Container container;
	
	@Before
	public void setUp() throws Exception {
		container = new Container();
	}
	
	@After
	public void tearDown() throws Exception {
		container.destructeur();
		container = null;
	}

	@Test(expected=ContainerException.class)
	public void test_erreur() throws Exception
	{
		container.getService("ABWABWA");
	}

	@Test
	public void test_oublie() throws Exception
	{
		Assert.assertTrue(!container.contient("Table"));
		container.getService("Table");
		Assert.assertTrue(container.contient("Table"));
		container.oublie("Table");
		Assert.assertTrue(!container.contient("Table"));
	}
	
	
	@Test
	public void test_log() throws Exception
	{
		container.getService("Log");
	}

	@Test
	public void test_config() throws Exception
	{
		container.getService("Read_Ini");
	}

	@Test
	public void test_table() throws Exception
	{
		container.getService("Table");
	}

	@Test
	public void test_deplacements() throws Exception
	{
		container.getService("Deplacements");
	}

	@Test
	public void test_capteurs() throws Exception
	{
		container.getService("Capteur");
	}

	@Test
	public void test_actionneurs() throws Exception
	{
		container.getService("Actionneurs");
	}

	@Test
	public void test_HookGenerator() throws Exception
	{
		container.getService("HookGenerator");
	}

	@Test
	public void test_RobotVrai() throws Exception
	{
		container.getService("RobotVrai");
	}

	@Test
	public void test_ScriptManager() throws Exception
	{
		container.getService("ScriptManager");
	}

	@Test
	public void test_Strategie() throws Exception
	{
		container.getService("Strategie");
	}

	@Test
	public void test_pathfinding() throws Exception
	{
		container.getService("Pathfinding");
	}

	@Test
	public void test_MemoryManager() throws Exception
	{
		container.getService("MemoryManager");
	}

	@Test
	public void test_Laser() throws Exception
	{
		container.getService("Laser");
	}

	@Test
	public void test_FiltrageLaser() throws Exception
	{
		container.getService("FiltrageLaser");
	}

	@Test
	public void test_CheckUp() throws Exception
	{
		container.getService("CheckUp");
	}

	@Test
	public void test_serieAsservissement() throws Exception
	{
		container.getService("serieAsservissement");
	}

	@Test
	public void test_serieCapteursActionneurs() throws Exception
	{
		container.getService("serieCapteursActionneurs");
	}

	@Test
	public void test_serieLaser() throws Exception
	{
		container.getService("serieLaser");
	}

	@Test
	public void test_threadTimer() throws Exception
	{
		container.getService("threadTimer");
	}

	@Test
	public void test_threadStrategie() throws Exception
	{
		container.getService("threadStrategie");
	}

}
