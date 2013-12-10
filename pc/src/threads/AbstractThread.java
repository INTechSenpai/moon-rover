package threads;

import utils.Log;
import utils.Read_Ini;
import container.Service;

public abstract class AbstractThread extends Thread implements Service {

	protected Read_Ini config;
	protected Log log;

	protected static boolean stop_threads = false;
	
	AbstractThread(Service config, Service log)
	{
		this.config = (Read_Ini) config;
		this.log = (Log) log;
	}

}
