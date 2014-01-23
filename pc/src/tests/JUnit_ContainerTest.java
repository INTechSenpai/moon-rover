package tests;

import hook.HookGenerator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import exception.ContainerException;
import table.Table;
import container.Container;
import pathfinding.*;
import robot.*;
import robot.cartes.*;
import robot.serial.*;
import scripts.ScriptManager;
import strategie.*;
import utils.*;
import threads.*;

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
		@SuppressWarnings("unused")
		Log log = (Log)container.getService("ABWABWA");
	}

	@Test
	public void test_log() throws Exception
	{
		@SuppressWarnings("unused")
		Log log = (Log)container.getService("Log");
	}

	@Test
	public void test_config() throws Exception
	{
		@SuppressWarnings("unused")
		Read_Ini config = (Read_Ini)container.getService("Read_Ini");
	}

	@Test
	public void test_table() throws Exception
	{
		@SuppressWarnings("unused")
		Table table = (Table)container.getService("Table");
	}

	@Test
	public void test_deplacements() throws Exception
	{
		@SuppressWarnings("unused")
		Deplacements deplacements = (Deplacements)container.getService("Deplacements");
	}

	@Test
	public void test_capteurs() throws Exception
	{
		@SuppressWarnings("unused")
		Capteurs capteurs = (Capteurs)container.getService("Capteur");
	}

	@Test
	public void test_actionneurs() throws Exception
	{
		@SuppressWarnings("unused")
		Actionneurs actionneurs = (Actionneurs)container.getService("Actionneurs");
	}

	@Test
	public void test_HookGenerator() throws Exception
	{
		@SuppressWarnings("unused")
		HookGenerator hookgenerator = (HookGenerator)container.getService("HookGenerator");
	}

	@Test
	public void test_RobotVrai() throws Exception
	{
		@SuppressWarnings("unused")
		RobotVrai robotvrai = (RobotVrai)container.getService("RobotVrai");
	}

	@Test
	public void test_ScriptManager() throws Exception
	{
		@SuppressWarnings("unused")
		ScriptManager scriptmanager = (ScriptManager)container.getService("ScriptManager");
	}

	@Test
	public void test_Strategie() throws Exception
	{
		@SuppressWarnings("unused")
		Strategie strategie = (Strategie)container.getService("Strategie");
	}

	@Test
	public void test_pathfinding() throws Exception
	{
		@SuppressWarnings("unused")
		Pathfinding pathfinding = (Pathfinding)container.getService("Pathfinding");
	}

	@Test
	public void test_MemoryManager() throws Exception
	{
		@SuppressWarnings("unused")
		MemoryManager memorymanager = (MemoryManager)container.getService("MemoryManager");
	}

	@Test
	public void test_Laser() throws Exception
	{
		@SuppressWarnings("unused")
		Laser laser = (Laser)container.getService("Laser");
	}

	@Test
	public void test_FiltrageLaser() throws Exception
	{
		@SuppressWarnings("unused")
		FiltrageLaser filtrageLaser = (FiltrageLaser)container.getService("FiltrageLaser");
	}

	@Test
	public void test_CheckUp() throws Exception
	{
		@SuppressWarnings("unused")
		CheckUp checkUp = (CheckUp)container.getService("CheckUp");
	}

	@Test
	public void test_serieAsservissement() throws Exception
	{
		@SuppressWarnings("unused")
		Serial serieAsservissement = (Serial)container.getService("serieAsservissement");
	}

	@Test
	public void test_serieCapteursActionneurs() throws Exception
	{
		@SuppressWarnings("unused")
		Serial serieCapteursActionneurs = (Serial)container.getService("serieCapteursActionneurs");
	}

	@Test
	public void test_serieLaser() throws Exception
	{
		@SuppressWarnings("unused")
		Serial serieLaser = (Serial)container.getService("serieLaser");
	}

	@Test
	public void test_threadTimer() throws Exception
	{
		@SuppressWarnings("unused")
		ThreadTimer threadTimer = (ThreadTimer)container.getService("threadTimer");
	}

	@Test
	public void test_threadStrategie() throws Exception
	{
		@SuppressWarnings("unused")
		ThreadStrategie threadStrategie = (ThreadStrategie)container.getService("threadStrategie");
	}

}
