package strategie;

import robot.RobotChrono;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Classe qui gère les objets utilisés dans l'arbre des possibles de la stratégie
 * Benchmark (eeepc): pour 1000 tables, il y a 35ms d'instanciation et 70ms d'affectation
 * @author pf
 */

public class MemoryManager implements Service {

	private Log log;
	private Read_Ini config;
	
	private int nbmax;

	private MemoryManagerProduct[] productsTable;
	private MemoryManagerProduct[] productsRobotChrono;

	public MemoryManager(Read_Ini config, Log log, Table table, RobotChrono robotchrono)
	{
		this.log = log;
		this.config = config;
		try {
			nbmax = Integer.parseInt(this.config.get("profondeur_max_arbre"));
		}
		catch(Exception e)
		{
			nbmax = 10;
			this.log.critical(e, this);
		}

		productsTable = new MemoryManagerProduct[nbmax];
		productsRobotChrono = new MemoryManagerProduct[nbmax];

		for(int i = 0; i < nbmax; i++)
		{
			productsTable[i] = table.clone();
			productsRobotChrono[i] = robotchrono.clone();
		}
}

	public MemoryManagerProduct getClone(String nom, MemoryManagerProduct modele, int profondeur)
	{
		MemoryManagerProduct out = null;

		if(nom == "Table")
		{
			out = productsTable[profondeur-1];
			modele.clone(out);
		}
		else if(nom == "RobotChrono")
		{
			out = productsRobotChrono[profondeur-1];
			modele.clone(out);
		}

/*		else
		{
			out = productsObjects.get(nom)[productsIndices.get(nom)];
			modele.clone(out);
			productsIndices.put(nom, (productsIndices.get(nom)+1) % nbmax);
		}*/
		return out;
	}
			
}
