package strategie;

import java.util.Hashtable;

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

	public MemoryManager(Service config, Service log, Service table, Service robotchrono)
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
			
		register("Table", (MemoryManagerProduct)table);
		register("RobotChrono", (MemoryManagerProduct)robotchrono);
	}

	public void register(String nom, MemoryManagerProduct instance)
	{
		log.debug("Instanciation de "+instance.getNom(), this);
		productsObjects.put(nom, new MemoryManagerProduct[nbmax]);
		productsIndices.put(nom, 0);
		productsModels.put(nom, instance);
		MemoryManagerProduct[] arrayProducts = productsObjects.get(nom);

		// Création des objets pour ce MemoryManagerProduct
		for(int i = 0; i < nbmax; i++)
			arrayProducts[i] = instance.clone();
	}
	
	public void setModele(MemoryManagerProduct instance)
	{
		// On actualise le modèle sans allocation de mémoire
		MemoryManagerProduct model = productsModels.get(instance.getNom());
		instance.clone(model);
	}
	
	public MemoryManagerProduct getClone(String nom)
	{
		MemoryManagerProduct out;

		// Table et Robotchrono sont traités particulièrement car les indices
		// sous forme de int sont plus rapides à traiter que les productsIndices
		
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
