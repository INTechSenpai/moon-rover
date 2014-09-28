package tests;

import org.junit.Before;
import org.junit.After;

import utils.Log;
import utils.Read_Ini;
import container.Container;

public abstract class JUnit_Test {

	protected Container container;
	protected Read_Ini config;
	protected Log log;
	
	@Before
	public void setUp() throws Exception
	{
		container = new Container();
		config = (Read_Ini) container.getService("Read_Ini");
		log = (Log) container.getService("Log");
	}

	@After
	public void tearDown() throws Exception {
		container.destructeur();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
}
