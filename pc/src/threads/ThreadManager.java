package threads;

import java.util.Hashtable;

import utils.Log;
import utils.Read_Ini;
import container.Service;
import exception.ThreadException;

public class ThreadManager {
	
	private Read_Ini config;
	private Log log;
	
	private Hashtable<String, AbstractThread> threads;
	
	public ThreadManager(Service config, Service log, Service robotVrai, Service capteur, Service table)
	{
		this.config = (Read_Ini) config;
		this.log = (Log) log;

		threads = new Hashtable<String, AbstractThread>();

		threads.put("threadTimer", new ThreadTimer(config, log));
		threads.put("threadPosition", new ThreadPosition(config, log, robotVrai, threads.get("threadTimer")));
		threads.put("threadCapteurs", new ThreadCapteurs(config, log, robotVrai, threads.get("threadTimer"), table, capteur));
		
		this.log.debug("Lancement des threads", this);
		threads.get("threadTimer").start();
		threads.get("threadPosition").start();
		threads.get("threadCapteurs").start();
		
	}

	public AbstractThread getThread(String nom) throws ThreadException
	{
		AbstractThread thread = threads.get(nom);
		if(thread == null)
		{
			log.warning("Le thread suivant n'existe pas: "+nom, this);
			throw new ThreadException();
		}
		return thread;
	}
	
}
