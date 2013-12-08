package container;

import java.util.Hashtable;
import java.util.Map;
import utils.*;
import table.Table;
import robot.*;

public class Container {

	private Map<String,Service> services = new Hashtable<String,Service>();
	private SerialManager serialmanager = null;
	
	public Service getService(String nom)
	{
		if(services.containsKey(nom));
		else if(nom == "Log")
			services.put(nom, (Service)new Log(getService("Read_Ini")));
		else if(nom == "Read_Ini")
			services.put(nom, (Service)new Read_Ini("ABWABWA"));
		else if(nom == "Table")
			services.put(nom, (Service)new Table(	getService("Log"),
													getService("Read_Ini")));
		else if(nom == "SerieAsservissement")
		{
			if(serialmanager == null)
				serialmanager = new SerialManager(getService("Log"));
			services.put(nom, (Service)serialmanager.serieAsservissement);
		}
		else if(nom == "SerieCapteursActionneurs")
		{
			if(serialmanager == null)
				serialmanager = new SerialManager(getService("Log"));
			services.put(nom, (Service)serialmanager.serieCapteursActionneurs);
		}
		else if(nom == "SerieLaser")
		{
			if(serialmanager == null)
				serialmanager = new SerialManager(getService("Log"));
			services.put(nom, (Service)serialmanager.serieLaser);
		}
		else if(nom == "Deplacements")
			services.put(nom, (Service)new Deplacements(getService("Read_Ini"),
														getService("Log"),
														getService("SerieAsservissement")));
		else if(nom == "Capteur")
			services.put(nom, (Service)new Capteur(	getService("Read_Ini"),
													getService("Log"),
													getService("SerieCapteursActionneurs")));
		else if(nom == "Actionneurs")
			services.put(nom, (Service)new Actionneurs(	getService("Read_Ini"),
														getService("Log"),
														getService("SerieCapteursActionneurs")));
		else
			return null;
		return services.get(nom);
	}

	
}
