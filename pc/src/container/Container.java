package container;

import hook.HookGenerator;

import java.util.Hashtable;
import java.util.Map;

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
		else if(nom.substring(0,5) == "serie")
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
														getService("SerieAsservissement")));
		else if(nom == "Capteur")
			services.put(nom, (Service)new Capteur(	getService("Read_Ini"),
													getService("Log"),
													getService("SerieCapteursActionneurs")));
		else if(nom == "Actionneurs")
			services.put(nom, (Service)new Actionneurs(	getService("Read_Ini"),
														getService("Log"),
														getService("SerieCapteursActionneurs")));
		else if(nom == "HookGenerator")
			services.put(nom, (Service)new HookGenerator(	getService("Read_Ini"),
															getService("Log")));		
		else if(nom == "RobotVrai")
			services.put(nom, (Service)new RobotVrai(	getService("Capteur"),
															getService("Actionneurs"),
															getService("Deplacements"),
															getService("HookGenerator"),
															getService("Table"),
															getService("Read_Ini"),
															getService("Log")));		
		else if(nom == "RobotChrono")
			services.put(nom, (Service)new RobotChrono(	getService("Capteur"),
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
			services.put(nom, (Service)new Strategie(	getService("ScriptManager"),
														getService("Table"),
														getService("Read_Ini"),
														getService("Log")));
			 
		else if(nom.substring(0,6) == "thread")
		{
			if(threadmanager == null)
				threadmanager = new ThreadManager(	getService("Read_Ini"),
													getService("Log"));
			services.put(nom, (Service)threadmanager.getThread(nom));
		}
		else
			return null;
		return services.get(nom);
	}

	
}
