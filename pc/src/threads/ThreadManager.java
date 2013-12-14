package threads;

import java.util.Hashtable;

import pathfinding.Pathfinding;
import robot.cartes.Capteur;
import robot.cartes.Deplacements;
import robot.cartes.Laser;
import container.Container;
import table.Table;
import robot.RobotChrono;
import robot.RobotVrai;
import strategie.MemoryManager;
import strategie.Strategie;
import utils.Log;
import utils.Read_Ini;
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
	
	public ThreadManager(Read_Ini config, Log log)
	{
		this.config = config;
		this.log = log;
		
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
				threads.put("threadTimer", new ThreadTimer(config, log, (Table)container.getService("Table"), (Capteur)container.getService("Capteur"), (Deplacements)container.getService("Deplacements")));
			else if(nom == "threadPosition")
				threads.put("threadPosition", new ThreadPosition(config, log, (RobotVrai)container.getService("RobotVrai"), (ThreadTimer)threads.get("threadTimer")));
			else if(nom == "threadCapteurs")
				threads.put("threadCapteurs", new ThreadCapteurs(config, log, (RobotVrai)container.getService("RobotVrai"), (ThreadTimer)threads.get("threadTimer"), (Table)container.getService("Table"), (Capteur)container.getService("Capteur")));
			else if(nom == "threadStrategie")
				threads.put("threadStrategie", new ThreadStrategie(config, log, (Strategie)container.getService("Strategie"), (Table)container.getService("Table"), (RobotVrai)container.getService("RobotVrai"), (RobotChrono)container.getService("RobotChrono"), (Pathfinding)container.getService("Pathfinding"), (MemoryManager)container.getService("MemoryManager")));
			else if(nom == "threadLaser")
				threads.put("threadLaser", new ThreadLaser(config, log, (Laser)container.getService("Laser"), (Table)container.getService("Table"), (ThreadTimer)threads.get("threadTimer")));
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
