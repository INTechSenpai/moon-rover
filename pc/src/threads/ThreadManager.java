package threads;

import java.util.Hashtable;

import robot.cartes.Actionneurs;
import robot.cartes.Capteurs;
import robot.cartes.Deplacements;
import robot.cartes.laser.FiltrageLaser;
import robot.cartes.laser.Laser;
import table.Table;
import robot.RobotVrai;
import utils.Log;
import utils.Read_Ini;
import exceptions.ContainerException;
import exceptions.ThreadException;
import exceptions.serial.SerialManagerException;

/**
 * Service qui instancie les threads
 * @author pf
 *
 */

public class ThreadManager {
	
	private Log log;
	
	private Hashtable<String, AbstractThread> threads;
	
	public ThreadManager(Read_Ini config, Log log)
	{
		this.log = log;
		
		threads = new Hashtable<String, AbstractThread>();

		AbstractThread.log = log;
		AbstractThread.config = config;
		AbstractThread.stopThreads = false;
	}

	/**
	 * Donne un thread à partir de son nom. Utilisé par container uniquement.
	 * @param nom
	 * @return
	 * @throws ThreadException
	 * @throws ContainerException
	 * @throws ConfigException
	 * @throws SerialManagerException
	 */
	public AbstractThread getThreadTimer(Table table, Capteurs capteur, Deplacements deplacements, Actionneurs actionneurs)
	{
		AbstractThread thread = threads.get("threadTimer");
		if(thread == null)
			threads.put("threadTimer", new ThreadTimer(table, capteur, deplacements, actionneurs));
		return threads.get("threadTimer");
	}

	public AbstractThread getThreadCapteurs(RobotVrai robotvrai, Table table, Capteurs capteurs)
	{
		AbstractThread thread = threads.get("threadCapteurs");
		if(thread == null)
			threads.put("threadCapteurs", new ThreadCapteurs(robotvrai, table, capteurs));
		return threads.get("threadCapteurs");
	}

	public AbstractThread getThreadLaser(Laser laser, Table table, FiltrageLaser filtragelaser)
	{
		AbstractThread thread = threads.get("threadLaser");
		if(thread == null)
			threads.put("threadLaser", new ThreadLaser(laser, table, filtragelaser));
		return threads.get("threadLaser");
	}

	public void demarreThreads()
	{
		log.debug("Démarrage des threads enregistrés", this);
		for(String nom: threads.keySet())
			threads.get(nom).start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void arreteThreads()
	{
		AbstractThread.stopThreads = true;
	}
}
