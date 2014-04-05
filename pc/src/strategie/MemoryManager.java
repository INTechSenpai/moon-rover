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

	private Table[] productsTable;
	private RobotChrono[] productsRobotChrono;

	public MemoryManager(Read_Ini config, Log log, Table table)
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

		productsTable = new Table[nbmax];
		productsRobotChrono = new RobotChrono[nbmax];

		RobotChrono robotchrono = new RobotChrono(config, log);
		for(int i = 0; i < nbmax; i++)
		{
			productsTable[i] = table.clone();
			productsRobotChrono[i] = robotchrono.clone();
		}
}
	
	
	public void setModelTable(Table instance, int profondeur_max)
	{
		productsTable[profondeur_max] = instance;
	}
	
	public void setModelRobotChrono(RobotChrono instance, int profondeur_max)
	{
		productsRobotChrono[profondeur_max] = instance;
	}
	
	public Table getCloneTable(int profondeur)
	{
		Table out = productsTable[profondeur-1];
		productsTable[profondeur].clone(out);
		return out;
	}

	public RobotChrono getCloneRobotChrono(int profondeur)
	{
		RobotChrono out = productsRobotChrono[profondeur-1];
		productsRobotChrono[profondeur].clone(out);
		return out;
	}
	
	public void maj_config()
	{
		// TODO
	}
			
}
