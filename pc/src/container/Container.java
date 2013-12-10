package container;

import hook.HookGenerator;

import java.util.Hashtable;
import java.util.Map;

import pathfinding.Pathfinding;
import exception.SerialManagerException;
import utils.*;
import scripts.ScriptManager;
import table.Table;
import threads.ThreadManager;
import robot.RobotChrono;
import robot.RobotVrai;
import robot.Strategie;
import robot.cartes.Actionneurs;
import robot.cartes.Capteur;
import robot.cartes.Deplacements;
import robot.serial.SerialManager;

/**
 * Les différents services appelables sont:
 * Log
 * Read_Ini
 * Table
 * serie* (serieAsservissement, ...)
 * Deplacements
 * Capteur
 * Actionneurs
 * HookGenerator
 * RobotVrai
 * RobotChrono
 * ScriptManager
 * Strategie
 * thread* (threadTimer, ...)
 * ScriptManager
 * Pathfinding
 * HookGenerator
 * 
 * @author pf
 *
 */

public class Container {

	private Map<String,Service> services = new Hashtable<String,Service>();
	private SerialManager serialmanager = null;
	private ThreadManager threadmanager = null;
	
	public Service getService(String nom)
	{
		if(services.containsKey(nom));
		else if(nom == "Read_Ini")
			services.put(nom, (Service)new Read_Ini("../pc/config/"));
		else if(nom == "Log")
			services.put(nom, (Service)new Log(getService("Read_Ini")));
		else if(nom == "Table")
			services.put(nom, (Service)new Table(	getService("Log"),
													getService("Read_Ini")));
		else if(nom.length() > 4 && nom.substring(0,5).equals("serie"))
		{
			if(serialmanager == null)
				serialmanager = new SerialManager(getService("Log"));
			try
			{
				services.put(nom, (Service)serialmanager.getSerial(nom));
			}
			catch(SerialManagerException e)
			{
				System.out.println(e);
			}
		}
		else if(nom == "Deplacements")
			services.put(nom, (Service)new Deplacements(getService("Log"),
														getService("serieAsservissement")));
		else if(nom == "Capteur")
			services.put(nom, (Service)new Capteur(	getService("Read_Ini"),
													getService("Log"),
													getService("serieCapteursActionneurs")));
		else if(nom == "Actionneurs")
			services.put(nom, (Service)new Actionneurs(	getService("Read_Ini"),
														getService("Log"),
														getService("serieCapteursActionneurs")));
		else if(nom == "HookGenerator")
			services.put(nom, (Service)new HookGenerator(	getService("Read_Ini"),
															getService("Log")));		
		else if(nom == "RobotVrai")
			services.put(nom, (Service)new RobotVrai(	getService("Pathfinding"),
														getService("Capteur"),
														getService("Actionneurs"),
														getService("Deplacements"),
														getService("HookGenerator"),
														getService("Table"),
														getService("Read_Ini"),
														getService("Log")));		
		else if(nom == "RobotChrono")
			services.put(nom, (Service)new RobotChrono(	getService("Pathfinding"),
														getService("Capteur"),
														getService("Actionneurs"),
														getService("Deplacements"),
														getService("HookGenerator"),
														getService("Table"),
														getService("Read_Ini"),
														getService("Log")));		
		else if(nom == "ScriptManager")
			services.put(nom, (Service)new ScriptManager(	getService("RobotVrai"),
															getService("RobotChrono"),
															getService("HookGenerator"),
															getService("Table"),
															getService("Read_Ini"),
															getService("Log")));
		else if(nom == "Strategie")
			services.put(nom, (Service)new Strategie(	getService("threadTimer"),
														getService("ScriptManager"),
														getService("Pathfinding"),
														getService("Table"),
														getService("Read_Ini"),
														getService("Log")));			 
		else if(nom.length() > 5 && nom.substring(0,6).equals("thread"))
		{
			if(threadmanager == null)
				threadmanager = new ThreadManager(	getService("Read_Ini"),
													getService("Log"),
													getService("RobotVrai"),
													getService("Capteur"),
													getService("Table"));
			services.put(nom, (Service)threadmanager.getThread(nom));
		}
		else if(nom == "Pathfinding")
			services.put(nom, (Service)new Pathfinding(	getService("Table"),
														getService("Read_Ini"),
														getService("Log")));
		else
		{
			System.out.println("Erreur de getService pour le service: "+nom);
			return null;
		}
		return services.get(nom);
	}

	
}
