package threads;

import java.util.Hashtable;

import container.Container;
import robot.RobotVrai;
import robot.cartes.Capteur;
import strategie.Strategie;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import exception.ConfigException;
import exception.ContainerException;
import exception.ThreadException;

/**
 * Service qui instancie les threads
 * @author pf
 *
 */

public class ThreadManager {
	
	private Read_Ini config;
	private Log log;
	private RobotVrai robotvrai;
	private Capteur capteur;
	private Table table;
	private Strategie strategie;
	private Container container;
	
	private Hashtable<String, AbstractThread> threads;
	
	public ThreadManager(Service config, Service log, Service robotvrai, Service capteur, Service table)
	{
		this.config = (Read_Ini) config;
		this.log = (Log) log;
		this.robotvrai = (RobotVrai) robotvrai;
		this.capteur = (Capteur) capteur;
		this.table = (Table) table;
		
		container = new Container();
		
		threads = new Hashtable<String, AbstractThread>();
	}

	public AbstractThread getThread(String nom) throws ThreadException, ContainerException, ConfigException
	{
		AbstractThread thread = threads.get(nom);
		// si le thread n'est pas déjà lancé, on le crée
		if(thread == null)
		{
			if(nom == "threadTimer")
				threads.put("threadTimer", new ThreadTimer(config, log));
			else if(nom == "threadPosition")
				threads.put("threadPosition", new ThreadPosition(config, log, robotvrai, threads.get("threadTimer")));
			else if(nom == "threadCapteurs")
				threads.put("threadCapteurs", new ThreadCapteurs(config, log, robotvrai, threads.get("threadTimer"), table, capteur));
			else if(nom == "threadStrategie")
			{
				strategie = (Strategie) container.getService("Strategie");
				threads.put("threadStrategie", new ThreadStrategie(config, log, strategie));
			}
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
