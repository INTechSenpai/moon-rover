package table;

import static org.junit.Assert.*;

import org.junit.Test;

import utils.Log;
import utils.Read_Ini;
import container.Container;
import exception.ConfigException;
import exception.ContainerException;
import exception.SerialManagerException;
import exception.ThreadException;


public class TableTest {

	@Test
	public void testClone() throws ContainerException, ThreadException, ConfigException, SerialManagerException
	{
		Container container = new Container();
		Table table1 = new Table((Log)container.getService("Log"), (Read_Ini)container.getService("Read_Ini"));
		Table table2 = new Table((Log)container.getService("Log"), (Read_Ini)container.getService("Read_Ini"));
			
		table1.clone(table2);
		
		
		fail("Not yet implemented");
	}

}
