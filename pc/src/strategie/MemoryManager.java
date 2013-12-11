package strategie;

import java.util.Hashtable;

import robot.RobotChrono;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import factories.FactoryProduct;

/**
 * Classe qui gère les objets utilisés dans l'arbre des possibles de la stratégie
 * @author pf
 *
 */

public class MemoryManager implements Service {

	private Log log;
	private Read_Ini config;
	
	private final int nbmax;

	protected Hashtable<String, FactoryProduct> productsModels;
	protected Hashtable<String, Integer> productsIndices;
	protected Hashtable<String, FactoryProduct[]> productsObjects;
	

	public MemoryManager(Service config, Service log)
	{
		this.log = (Log) log;
		this.config = (Read_Ini) config;

		nbmax = Integer.parseInt(this.config.config.getProperty("nb_max_noeuds"));

		register("Table");
		register("RobotChrono");
	}

	public void register(String nom)
	{
		if(nom == "Table")
		{
			log.debug("Instanciation des tables", this);
			productsObjects.put("Table", new Table[nbmax]);
			for(int i = 0; i < nbmax; i++)
				productsObjects.get("Table")[i] = new Table(null, null);
		}
		else if(nom == "RobotChrono")
		{
			log.debug("Instanciation des robotchrono", this);
			productsObjects.put("RobotChrono", new RobotChrono[nbmax]);
			for(int i = 0; i < nbmax; i++)
				productsObjects.get("RobotChrono")[i] = new RobotChrono(null, null);
		}
		else
			log.warning("Erreur lors de l'enregistrement de "+nom, this);
	}
	
	public void setModele(FactoryProduct instance)
	{
		productsModels.put(instance.TypeName(), instance);
		productsIndices.put(instance.TypeName(), 0);		
	}
	
	public FactoryProduct getClone(String nom)
	{
		FactoryProduct out = productsObjects.get(nom)[productsIndices.get(nom)];
		out = productsModels.get(nom).Clone();
		return out;
	}
			
}
