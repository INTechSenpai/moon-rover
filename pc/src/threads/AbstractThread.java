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
	
	public AbstractThread(Service config, Service log)
	{
		AbstractThread.config = (Read_Ini) config;
		AbstractThread.log = (Log) log;
	}

	protected AbstractThread()
	{		
	}

	public void maj_config()
	{
	}
	
	public abstract void run();

}

