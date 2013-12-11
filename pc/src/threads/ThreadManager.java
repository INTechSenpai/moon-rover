package threads;

import java.util.Hashtable;

import container.Container;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import exception.ConfigException;
import exception.ContainerException;
import exception.SerialManagerException;
import exception.ThreadException;

/**
 * Service qui instancie les threads
 * @author pf
 *
 */

public class ThreadManager {
	
	private Read_Ini config;
	private Log log;
	private Container container;
	
	private Hashtable<String, AbstractThread> threads;
	
	public ThreadManager(Service config, Service log)
	{
		this.config = (Read_Ini) config;
		this.log = (Log) log;
		
		container = new Container();
		
		threads = new Hashtable<String, AbstractThread>();
	}

	public AbstractThread getThread(String nom) throws ThreadException, ContainerException, ConfigException, SerialManagerException
	{
		AbstractThread thread = threads.get(nom);
		// si le thread n'est pas déjà lancé, on le crée
		if(thread == null)
		{
			if(nom == "threadTimer")
				threads.put("threadTimer", new ThreadTimer(config, log, container.getService("Table"), container.getService("Capteur"), container.getService("Deplacements")));
			else if(nom == "threadPosition")
				threads.put("threadPosition", new ThreadPosition(config, log, container.getService("RobotVrai"), threads.get("threadTimer")));
			else if(nom == "threadCapteurs")
				threads.put("threadCapteurs", new ThreadCapteurs(config, log, container.getService("RobotVrai"), threads.get("threadTimer"), container.getService("Table"), container.getService("Capteur")));
			else if(nom == "threadStrategie")
				threads.put("threadStrategie", new ThreadStrategie(config, log, container.getService("Strategie"), container.getService("Table"), container.getService("RobotVrai"), container.getService("RobotChrono"), container.getService("Pathfinding")));
			else if(nom == "threadLaser")
				threads.put("threadLaser", new ThreadLaser(config, log, container.getService("Laser"), container.getService("Table"), threads.get("threadTimer")));
			else
			{
				log.warning("Le thread suivant n'existe pas: "+nom, this);
				throw new ThreadException();
			}
			this.log.debug("Lancement du thread "+nom, this);
			threads.get(nom).start();
		}
		return threads.get(nom);
	}
	
}
