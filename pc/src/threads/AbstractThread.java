package threads;

import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Classe abstraite des threads
 * @author pf
 *
 */

public abstract class AbstractThread extends Thread implements Service {

	protected static Read_Ini config;
	protected static Log log;

	protected static boolean stop_threads = false;
	
	AbstractThread(Service config, Service log)
	{
		this.config = (Read_Ini) config;
		this.log = (Log) log;
	}


	public static void sleep(long duree)
	{
		try {
			Thread.sleep(duree);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

}

