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

	public void setModel(String nom, MemoryManagerProduct instance, int profondeur_max)
	{
		if(nom == "Table")
			productsTable[profondeur_max] = instance;
		else if(nom == "RobotChrono")
			productsRobotChrono[profondeur_max] = instance;		
	}
	
	public MemoryManagerProduct getClone(String nom, int profondeur)
	{
		MemoryManagerProduct out = null;

		if(nom == "Table")
		{
			out = productsTable[profondeur-1];
			productsTable[profondeur].clone(out);
		}
		else if(nom == "RobotChrono")
		{
			out = productsRobotChrono[profondeur-1];
			productsRobotChrono[profondeur].clone(out);
		}

		return out;
	}
			
}
