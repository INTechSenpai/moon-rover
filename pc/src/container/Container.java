package container;

import java.util.Hashtable;
import java.util.Map;
import utils.*;
import table.Table;

public class Container {

	private Map<String,Service> services = new Hashtable<String,Service>();
	
	public Service getService(String nom)
	{
		if(services.containsKey(nom));
		else if(nom == "Log")
			services.put(nom, (Service)new Log(getService("Read_Ini")));
		else if(nom == "Read_Ini")
			services.put(nom, (Service)new Read_Ini("ABWABWA"));
		else if(nom == "Table")
			services.put(nom, (Service)new Table(getService("Log"),getService("Read_Ini")));
		else
			return null;
		return services.get(nom);
	}

	
}
