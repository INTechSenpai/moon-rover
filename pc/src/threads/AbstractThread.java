package threads;

import utils.Log;
import utils.Read_Ini;
import container.Service;

public class AbstractThread extends Thread implements Service {

	protected Read_Ini config;
	protected Log log;

	boolean stop_thread = false;
	AbstractThread(Service config, Service log)
	{
		this.config = (Read_Ini) config;
		this.log = (Log) log;
	}

}
