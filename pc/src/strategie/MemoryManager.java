package strategie;

import java.util.Hashtable;

import robot.RobotChrono;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Classe qui gère les objets utilisés dans l'arbre des possibles de la stratégie
 * @author pf
 *
 */

public class MemoryManager implements Service {

	private Log log;
	private Read_Ini config;
	
	private int nbmax;

	private Hashtable<String, MemoryManagerProduct> productsModels = new Hashtable<String, MemoryManagerProduct>();
	private Hashtable<String, Integer> productsIndices = new Hashtable<String, Integer>();
	private Hashtable<String, MemoryManagerProduct[]> productsObjects = new Hashtable<String, MemoryManagerProduct[]>();
	
	private int indiceRobotChrono = 0;
	private int indiceTable = 0;

	public MemoryManager(Service config, Service log)
	{
		this.log = (Log) log;
		this.config = (Read_Ini) config;
		try {
			nbmax = Integer.parseInt(this.config.get("nb_max_noeuds"));
		}
		catch(Exception e)
		{
			nbmax = 1;
			this.log.critical(e, this);
		}
			
		register("Table");
		register("RobotChrono");
	}

	public void register(String nom)
	{
		if(nom == "Table")
		{
			log.debug("Instanciation des tables", this);
			productsObjects.put(nom, new Table[nbmax]);
			productsIndices.put(nom, 0);
		}
		else if(nom == "RobotChrono")
		{
			log.debug("Instanciation des robotchrono", this);
			productsObjects.put(nom, new RobotChrono[nbmax]);
			productsIndices.put(nom, 0);
		}
		else
			log.warning("Erreur lors de l'enregistrement de "+nom, this);

	}
	
	public void setModele(MemoryManagerProduct instance)
	{
		productsModels.put(instance.getNom(), instance.clone());
	}
	
	public MemoryManagerProduct getClone(String nom)
	{
		MemoryManagerProduct out;
		if(nom == "Table")
		{
			out = productsObjects.get(nom)[indiceTable];
			productsModels.get(nom).clone(out);
			indiceTable++;
			indiceTable %= nbmax;
		}
		else if(nom == "RobotChrono")
		{
			out = productsObjects.get(nom)[indiceRobotChrono];
			productsModels.get(nom).clone(out);
			indiceRobotChrono++;
			indiceRobotChrono %= nbmax;
		}
		else
		{
			out = productsObjects.get(nom)[productsIndices.get(nom)];
			productsModels.get(nom).clone(out);
			productsIndices.put(nom, (productsIndices.get(nom)+1) % nbmax);			
		}
		return out;
	}
			
}
