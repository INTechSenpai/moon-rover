package container;

import java.util.Hashtable;
import java.util.Map;

import hook.HookGenerator;
import pathfinding.Pathfinding;
import exception.ConfigException;
import exception.ContainerException;
import exception.SerialManagerException;
import exception.ThreadException;
import utils.*;
import scripts.ScriptManager;
import strategie.MemoryManager;
import strategie.Strategie;
import table.Table;
import threads.ThreadAnalyseEnnemi;
import threads.ThreadTimer;
import threads.ThreadManager;
import robot.RobotVrai;
import robot.cartes.Actionneurs;
import robot.cartes.Capteurs;
import robot.cartes.Deplacements;
import robot.cartes.FiltrageLaser;
import robot.cartes.Laser;
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
 * RobotChrono
 * ScriptManager
 * Strategie
 * thread* (threadTimer, threadPosition, threadStrategie, threadCapteurs, threadLaser)
 * Pathfinding
 * HookGenerator
 * Strategie
 * Laser
 * FiltrageLaser
 * CheckUp
 * 
 * @author pf
 *
 */

public class Container {

	private Map<String,Service> services = new Hashtable<String,Service>();
	private SerialManager serialmanager = null;
	private ThreadManager threadmanager = null;
	
	public Service getService(String nom) throws ContainerException, ThreadException, ConfigException, SerialManagerException
	{
		if(services.containsKey(nom));
		else if(nom == "Read_Ini")
		{
			services.put(nom, (Service)new Read_Ini("../pc/config/"));
		}
		else if(nom == "Log")
			services.put(nom, (Service)new Log(	(Read_Ini)getService("Read_Ini")));
		else if(nom == "Table")
		{
			services.put(nom, (Service)new Table(	(Log)getService("Log"),
													(Read_Ini)getService("Read_Ini")));
			((Table) services.get(nom)).initialise();
		}
		else if(nom.length() > 4 && nom.substring(0,5).equals("serie"))
		{
				if(serialmanager == null)
					serialmanager = new SerialManager((Log)getService("Log"));
				services.put(nom, (Service)serialmanager.getSerial(nom));
		}
		else if(nom == "Deplacements")
			services.put(nom, (Service)new Deplacements((Log)getService("Log"),
														(Serial)getService("serieAsservissement")));
		else if(nom == "Capteur")
			services.put(nom, (Service)new Capteurs(	(Read_Ini)getService("Read_Ini"),
													(Log)getService("Log"),
													(Serial)getService("serieCapteursActionneurs")));
		else if(nom == "Actionneurs")
			services.put(nom, (Service)new Actionneurs(	(Read_Ini)getService("Read_Ini"),
														(Log)getService("Log"),
														(Serial)getService("serieCapteursActionneurs")));
		else if(nom == "HookGenerator")
			services.put(nom, (Service)new HookGenerator(	(Read_Ini)getService("Read_Ini"),
															(Log)getService("Log"),
															(Capteurs)getService("Capteur")));		
		else if(nom == "RobotVrai")
			services.put(nom, (Service)new RobotVrai(	(Pathfinding)getService("Pathfinding"),
														(Capteurs)getService("Capteur"),
														(Actionneurs)getService("Actionneurs"),
														(Deplacements)getService("Deplacements"),
														(HookGenerator)getService("HookGenerator"),
														(Table)getService("Table"),
														(Read_Ini)getService("Read_Ini"),
														(Log)getService("Log")));		
		else if(nom == "ScriptManager")
			services.put(nom, (Service)new ScriptManager(	(Pathfinding)getService("Pathfinding"),
															(HookGenerator)getService("HookGenerator"),
															(Read_Ini)getService("Read_Ini"),
															(Log)getService("Log"),
															(RobotVrai)getService("RobotVrai")));
		else if(nom == "Strategie")
			services.put(nom, (Service)new Strategie(	(MemoryManager)getService("MemoryManager"),
														(ThreadAnalyseEnnemi)getService("threadAnalyseEnnemi"),
														(ThreadTimer)getService("threadTimer"),
														(ScriptManager)getService("ScriptManager"),
														(Pathfinding)getService("Pathfinding"),
														(Table)getService("Table"),
														(Read_Ini)getService("Read_Ini"),
														(Log)getService("Log")));			 
		else if(nom.length() > 5 && nom.substring(0,6).equals("thread"))
		{
			if(threadmanager == null)
			{
				threadmanager = new ThreadManager(	(Read_Ini)getService("Read_Ini"),
													(Log)getService("Log"));
			}
				services.put(nom, (Service)threadmanager.getThread(nom));
		}
		else if(nom == "Pathfinding")
			services.put(nom, (Service)new Pathfinding(	(Table)getService("Table"),
														(Read_Ini)getService("Read_Ini"),
														(Log)getService("Log")));
		else if(nom == "MemoryManager")
			services.put(nom, (Service)new MemoryManager(	(Read_Ini)getService("Read_Ini"),
															(Log)getService("Log"),
															(Table)getService("Table")));
		else if(nom == "Laser")
			services.put(nom, (Service)new Laser(	(Read_Ini)getService("Read_Ini"),
													(Log)getService("Log"),
													(Serial)getService("serieLaser"),
													(RobotVrai)getService("RobotVrai")));
		else if(nom == "FiltrageLaser")
			services.put(nom, (Service)new FiltrageLaser(	(Read_Ini)getService("Read_Ini"),
															(Log)getService("Log")));

		else if(nom == "CheckUp")
			services.put(nom, (Service)new CheckUp(	(Log)getService("Log"),
													(RobotVrai)getService("RobotVrai")));
		else
		{
			System.out.println("Erreur de getService pour le service: "+nom);
			throw new ContainerException();
		}
		return services.get(nom);
	}

	
}
