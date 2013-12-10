package threads;

import java.util.Hashtable;

import utils.Log;
import utils.Read_Ini;
import container.Service;

public class ThreadManager {
	
	private Read_Ini config;
	private Log log;
	
	// TODO mettre les vrais scripts...
	
	private Hashtable<String, AbstractThread> threads;
	
	public ThreadManager(Service config, Service log)
	{
		this.config = (Read_Ini) config;
		this.log = (Log) log;
		
		threads.put("threadTimer", new ThreadTimer(config, log));
		threads.put("threadPosition", new ThreadPosition(config, log));
		
	}

	public AbstractThread getThread(String nom)
	{
		AbstractThread thread = threads.get(nom);
		if(thread == null)
		{
			log.warning("Le thread suivant n'existe pas: "+nom, this);
			// TODO lancer exception			
		}
		return thread;
	}
	
}
