package tests;

import org.junit.Before;
import org.junit.After;

import container.Container;

public abstract class JUnit_Test {

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

	
}
