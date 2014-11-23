package container;

import java.util.Hashtable;
import java.util.Map;

import hook.types.HookFactory;
import exceptions.ContainerException;
import exceptions.ThreadException;
import exceptions.serial.SerialManagerException;
import utils.*;
import scripts.ScriptManager;
import strategie.GameState;
import table.Table;
import threads.ThreadManager;
import robot.Locomotion;
import robot.RobotReal;
import robot.cards.ActuatorsManager;
import robot.cards.Sensors;
import robot.cards.LocomotionCardWrapper;
import robot.cards.laser.LaserFiltration;
import robot.cards.laser.Laser;
import robot.serial.SerialManager;
import robot.serial.SerialConnexion;


//TODO: virer proprement de tous les fichiers le ThreadPosition
/**
 * 
 * Gestionnaire de la durée de vie des objets dans le code.
 * Permet à n'importe quelle classe implémentant l'interface "Service" d'appeller d'autres instances de services via son constructeur.
 * Une classse implémentant service n'est instanciée que par la classe "Container"
 * Les différents services appelables sont: //TODO; update this
 * Log
 * Config
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
 * (à compléter peut-être) ===>  penser a mettre a jour le test unitaire en fonction de l'ajout de services
 * 
 * @author pf
 */
public class Container
{

	// liste des services déjà instanciés. Contient au moins Config et Log. Les autres services appelables seront présents s'ils ont déjà étés appellés au moins une fois
	private Map<String,Service> instanciedServices = new Hashtable<String,Service>();
	
	private SerialManager serialmanager = null; //idem que threadmanager
	private ThreadManager threadmanager;	 // TODO: Pourquoi ce manager est-il memebre de container ?
	
	//gestion des log
	private Log log;
	
	//gestion de la configuration du robot
	private Config config;

	/**
	 * Fonction à appeler à la fin du programme.
	 * ferme la connexion serie, termine les différents threads, et ferme le log.
	 */
	public void destructor()
	{
		// stoppe les différents threads
		stopAllThreads();
		Sleep.sleep(700); // attends qu'ils soient bien tous arrètés
		
		// coupe les connexions séries
		if(serialmanager != null)
		{
			if(serialmanager.serieAsservissement != null)
				serialmanager.serieAsservissement.close();
			if(serialmanager.serieCapteursActionneurs != null)
				serialmanager.serieCapteursActionneurs.close();
			if(serialmanager.serieLaser != null)
				serialmanager.serieLaser.close();
		}
		
		// ferme le log
		log.close();
	}
	
	
	/**
	 * instancie le gestionnaire de dépendances et quelques services critiques
	 * Services instanciés:
	 * 		Config
	 * 		Log
	 * Instancie aussi le ThreadManager. // TODO: voir si l'on peut proprer cela ( ce n'est pa a priori le role de container puisque ThreadManager n'est pas un service) 
	 * @throws ContainerException en cas de problème avec le fichier de configuration ou le système de log
	 */
	public Container() throws ContainerException
	{
		try
		{
			// affiche la configuration avant toute autre chose
			System.out.println("== Container bootstrap ==");
			System.out.println("Loading config from current directory : " +  System.getProperty("user.dir"));
			
			//parse le ficher de configuration.
			instanciedServices.put("Config", (Service)new Config("./config/"));
			config = (Config)instanciedServices.get("Config");
			
			// démarre le système de log
			instanciedServices.put("Log", (Service)new Log(config));
			log = (Log)instanciedServices.get("Log");
		}
		catch(Exception e)
		{
			throw new ContainerException();
		}
		
		// instancie le gestionnnaire de thread
		threadmanager = new ThreadManager(config, log); //TODO: pourquoi ce manager est instancié ici alors que le manager des serial ne l'est qu'au premier appel ?
	}

	@SuppressWarnings("unchecked")
	public Service getService(String serviceRequested) throws ContainerException, ThreadException, SerialManagerException
	{
    	// instancie le service demandé lors de son premier appel 
    	
    	// si le service est déja instancié, on ne le réinstancie pas
		if(instanciedServices.containsKey(serviceRequested))
			;
		
		// Si le service n'est pas encore instancié, on l'instancie avant de le retourner à l'utilisateur
		else if(serviceRequested == "Table")
			instanciedServices.put(serviceRequested, (Service)new Table(	(Log)getService("Log"),
													(Config)getService("Read_Ini")));
		else if(serviceRequested.length() > 4 && serviceRequested.substring(0,5).equals("serie")) // les séries
		{
			if(serialmanager == null)
				serialmanager = new SerialManager(log);
			instanciedServices.put(serviceRequested, (Service)serialmanager.getSerial(serviceRequested));
		}
		else if(serviceRequested == "Deplacements")
			instanciedServices.put(serviceRequested, (Service)new LocomotionCardWrapper((Log)getService("Log"),
														(SerialConnexion)getService("serieAsservissement")));
		else if(serviceRequested == "Capteur")
			instanciedServices.put(serviceRequested, (Service)new Sensors(	(Config)getService("Read_Ini"),
			                                                (Log)getService("Log"),
			                                                (SerialConnexion)getService("serieCapteursActionneurs")));
		else if(serviceRequested == "Actionneurs")
			instanciedServices.put(serviceRequested, (Service)new ActuatorsManager(	(Config)getService("Read_Ini"),
														(Log)getService("Log"),
														(SerialConnexion)getService("serieCapteursActionneurs")));
		else if(serviceRequested == "HookGenerator")
			instanciedServices.put(serviceRequested, (Service)new HookFactory(	(Config)getService("Read_Ini"),
															(Log)getService("Log"),
															(GameState<RobotReal>)getService("RealGameState")));
		else if(serviceRequested == "RobotVrai")
			instanciedServices.put(serviceRequested, (Service)new RobotReal(	(Locomotion)getService("DeplacementsHautNiveau"),
														(Table)getService("Table"),
														(Config)getService("Read_Ini"),
														(Log)getService("Log")));		
        else if(serviceRequested == "DeplacementsHautNiveau")
            instanciedServices.put(serviceRequested, (Service)new Locomotion(  (Log)getService("Log"),
                                                                    (Config)getService("Read_Ini"),
                                                                    (Table)getService("Table"),
                                                                    (LocomotionCardWrapper)getService("Deplacements")));
        else if(serviceRequested == "RealGameState")
            instanciedServices.put(serviceRequested, (Service)new GameState<RobotReal>(  (Config)getService("Read_Ini"),
                                                                  (Log)getService("Log"),
                                                                  (Table)getService("Table"),
                                                                  (RobotReal)getService("RobotVrai")));
 
		else if(serviceRequested == "ScriptManager")
			instanciedServices.put(serviceRequested, (Service)new ScriptManager(	(Config)getService("Read_Ini"),
															(Log)getService("Log")));
		else if(serviceRequested == "threadTimer")
			instanciedServices.put(serviceRequested, (Service)threadmanager.getThreadTimer(	(Table)getService("Table"),
																		(Sensors)getService("Capteur"),
																		(LocomotionCardWrapper)getService("Deplacements"),
		                                                                (ActuatorsManager)getService("Actionneurs")));
		else if(serviceRequested == "threadCapteurs")
			instanciedServices.put(serviceRequested, (Service)threadmanager.getThreadCapteurs(	(RobotReal)getService("RobotVrai"),
																		(Table)getService("Table"),
																		(Sensors)getService("Capteur")));
		else if(serviceRequested == "threadLaser")
			instanciedServices.put(serviceRequested, (Service)threadmanager.getThreadLaser(	(Laser)getService("Laser"),
																		(Table)getService("Table"),
																		(LaserFiltration)getService("FiltrageLaser")));
		else if(serviceRequested == "Laser")
			instanciedServices.put(serviceRequested, (Service)new Laser(	(Config)getService("Read_Ini"),
													(Log)getService("Log"),
													(SerialConnexion)getService("serieLaser"),
													(RobotReal)getService("RobotVrai")));
		else if(serviceRequested == "FiltrageLaser")
			instanciedServices.put(serviceRequested, (Service)new LaserFiltration(	(Config)getService("Read_Ini"),
															(Log)getService("Log")));

		else if(serviceRequested == "CheckUp")
			instanciedServices.put(serviceRequested, (Service)new CheckUp(	(Log)getService("Log"),
													(RobotReal)getService("RobotVrai")));
		
		// si le service demandé n'est pas connu, alors on log une erreur.
		else
		{
			log.critical("Erreur de getService pour le service (service inconnu): "+serviceRequested, this);
			throw new ContainerException();
		}
		
		// retourne le service en mémoire à l'utilisateur
		return instanciedServices.get(serviceRequested);
	}	
		
	/**
	 * Demande au thread manager de démarrer les threads instanciés 
	 * (ie ceux qui ont étés demandés à getService)
	 */
	public void startInstanciedThreads()
	{
		threadmanager.startInstanciedThreads();
	}

	/**
	 * Demande au thread manager de démarrer tous les threads
	 */
	//TODO: gestion propre des exeptions
	public void startAllThreads()
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
		threadmanager.startInstanciedThreads();
	}

	/**
	 * Demande au thread manager d'arrêter tout les threads
	 * Le thread principal (appellant cette méthode) continue son exécution
	 */
	public void stopAllThreads()
	{
		threadmanager.stopAllThreads();
	}
	
}
