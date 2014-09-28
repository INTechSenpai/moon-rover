package threads;

import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Classe abstraite des threads
 * @author pf,marsu
 *
 */

public abstract class AbstractThread extends Thread implements Service {

	protected static Read_Ini config;
	protected static Log log;

	protected static boolean stopThreads = false;
	
	public AbstractThread(Service config, Service log)
	{
		AbstractThread.config = (Read_Ini) config;
		AbstractThread.log = (Log) log;
	}

	protected AbstractThread()
	{		
	}

	public void updateConfig()
	{
	}
	
	public abstract void run();

}

