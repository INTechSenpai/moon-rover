package container;

import java.util.Hashtable;
import java.util.Map;

import hook.types.HookGenerator;
import exceptions.ContainerException;
import exceptions.ThreadException;
import exceptions.serial.SerialManagerException;
import utils.*;
import scripts.ScriptManager;
import strategie.GameState;
import table.Table;
import threads.ThreadManager;
import robot.RobotReal;
import robot.cards.ActuatorsManager;
import robot.cards.Sensors;
import robot.cards.Locomotion;
import robot.cards.laser.LaserFiltration;
import robot.cards.laser.Laser;
import robot.highlevel.LocomotionHiLevel;
import robot.serial.SerialManager;
import robot.serial.Serial;

/**
 * Les différents services appelables sont:
 * Log
 * Read_Ini
 * Table
 * serie* (serieAsservissement, serieCapteursActionneurs, serieLaser)
 * Deplacements
 * Capteur
 * Actionneurs
 * HookGenerator
 * RobotVrai
 * ScriptManager
 * Strategie
 * thread* (threadTimer, threadStrategie, threadCapteurs, threadLaser)
 * Pathfinding
 * MemoryManager
 * Laser
 * FiltrageLaser
 * CheckUp
 * GameState
 * (à compléter peut-être)
 * 
 * @author pf
 *
 */

// penser a mettre a jour le test unitaire en fonction de l'ajout de services

public class Container
{

	private Map<String,Service> services = new Hashtable<String,Service>();
	private SerialManager serialmanager = null;
	private ThreadManager threadmanager;
	private Log log;
	private Config config;

	/**
	 * Fonction à appeler à la fin du programme.
	 */
	public void destructeur()
	{
		arreteThreads();
		Sleep.sleep(700);
		if(serialmanager != null)
		{
			if(serialmanager.serieAsservissement != null)
				serialmanager.serieAsservissement.close();
			if(serialmanager.serieCapteursActionneurs != null)
				serialmanager.serieCapteursActionneurs.close();
			if(serialmanager.serieLaser != null)
				serialmanager.serieLaser.close();
		}
		log.destructeur();
	}
	
	/*
	 * Table et pathfinding sont des services mais il en existe plusieurs versions.
	 * Le container renvoie la table et le pathfinding utilisé en dehors de l'arbre des possibles.
	 * La gestion des tables et des pathfindings dans l'arbre des possibles est faite par le MemoryManager.
	 */
	public Container() throws ContainerException
	{
		try
		{
			System.out.println("Loading config from current directory : " +  System.getProperty("user.dir"));
			services.put("Read_Ini", (Service)new Config("./config/"));
			config = (Config)services.get("Read_Ini");
			services.put("Log", (Service)new Log(config));
			log = (Log)services.get("Log");
		}
		catch(Exception e)
		{
			throw new ContainerException();
		}
		threadmanager = new ThreadManager(config, log);
	}

    @SuppressWarnings("unchecked")
	public Service getService(String nom) throws ContainerException, ThreadException, SerialManagerException
	{
		if(services.containsKey(nom))
			;
		else if(nom == "Table")
			services.put(nom, (Service)new Table(	(Log)getService("Log"),
													(Config)getService("Read_Ini")));
		else if(nom.length() > 4 && nom.substring(0,5).equals("serie"))
		{
			if(serialmanager == null)
				serialmanager = new SerialManager(log);
			services.put(nom, (Service)serialmanager.getSerial(nom));
		}
		else if(nom == "Deplacements")
			services.put(nom, (Service)new Locomotion((Log)getService("Log"),
														(Serial)getService("serieAsservissement")));
		else if(nom == "Capteur")
			services.put(nom, (Service)new Sensors(	(Config)getService("Read_Ini"),
			                                                (Log)getService("Log"),
			                                                (Serial)getService("serieCapteursActionneurs")));
		else if(nom == "Actionneurs")
			services.put(nom, (Service)new ActuatorsManager(	(Config)getService("Read_Ini"),
														(Log)getService("Log"),
														(Serial)getService("serieCapteursActionneurs")));
		else if(nom == "HookGenerator")
			services.put(nom, (Service)new HookGenerator(	(Config)getService("Read_Ini"),
															(Log)getService("Log"),
															(GameState<RobotReal>)getService("RealGameState")));
		else if(nom == "RobotVrai")
			services.put(nom, (Service)new RobotReal(	(LocomotionHiLevel)getService("DeplacementsHautNiveau"),
														(Table)getService("Table"),
														(Config)getService("Read_Ini"),
														(Log)getService("Log")));		
        else if(nom == "DeplacementsHautNiveau")
            services.put(nom, (Service)new LocomotionHiLevel(  (Log)getService("Log"),
                                                                    (Config)getService("Read_Ini"),
                                                                    (Table)getService("Table"),
                                                                    (Locomotion)getService("Deplacements")));
        else if(nom == "RealGameState")
            services.put(nom, (Service)new GameState<RobotReal>(  (Config)getService("Read_Ini"),
                                                                  (Log)getService("Log"),
                                                                  (Table)getService("Table"),
                                                                  (RobotReal)getService("RobotVrai")));
 
		else if(nom == "ScriptManager")
			services.put(nom, (Service)new ScriptManager(	(Config)getService("Read_Ini"),
															(Log)getService("Log")));
		else if(nom == "threadTimer")
			services.put(nom, (Service)threadmanager.getThreadTimer(	(Table)getService("Table"),
																		(Sensors)getService("Capteur"),
																		(Locomotion)getService("Deplacements"),
		                                                                (ActuatorsManager)getService("Actionneurs")));
		else if(nom == "threadCapteurs")
			services.put(nom, (Service)threadmanager.getThreadCapteurs(	(RobotReal)getService("RobotVrai"),
																		(Table)getService("Table"),
																		(Sensors)getService("Capteur")));
		else if(nom == "threadLaser")
			services.put(nom, (Service)threadmanager.getThreadLaser(	(Laser)getService("Laser"),
																		(Table)getService("Table"),
																		(LaserFiltration)getService("FiltrageLaser")));
		else if(nom == "Laser")
			services.put(nom, (Service)new Laser(	(Config)getService("Read_Ini"),
													(Log)getService("Log"),
													(Serial)getService("serieLaser"),
													(RobotReal)getService("RobotVrai")));
		else if(nom == "FiltrageLaser")
			services.put(nom, (Service)new LaserFiltration(	(Config)getService("Read_Ini"),
															(Log)getService("Log")));

		else if(nom == "CheckUp")
			services.put(nom, (Service)new CheckUp(	(Log)getService("Log"),
													(RobotReal)getService("RobotVrai")));
		else
		{
			log.critical("Erreur de getService pour le service: "+nom, this);
			if(!nom.equals("threadPosition")) // TODO: virer proprement de tous les fichiers
			        throw new ContainerException();
		}
		return services.get(nom);
	}	
		
	/**
	 * Demande au thread manager de démarrer les threads enregistrés
	 */
	public void demarreThreads()
	{
		threadmanager.demarreThreads();
	}

	/**
	 * Demande au thread manager de démarrer tous les threads
	 */
	//TODO: gestion propre des exeptions
	public void demarreTousThreads()
	{
		try {
			getService("threadLaser");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			getService("threadCapteurs");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			getService("threadTimer");
		} catch (Exception e) {
			e.printStackTrace();
		}
		threadmanager.demarreThreads();
	}

	/**
	 * Demande au thread manager d'arrêter les threads
	 */
	public void arreteThreads()
	{
		threadmanager.arreteThreads();
	}
	
}
