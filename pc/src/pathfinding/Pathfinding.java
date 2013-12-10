package pathfinding;

import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Service de log, affiche à l'écran des informations avec différents niveaux de couleurs
 * @author pf
 *
 */

public class Pathfinding implements Service
{
	// Dépendances
	private Table table;
	private Read_Ini config;
	private Log log;
	
	public Pathfinding(Service table, Service config, Service log)
	{
		this.table = (Table) table;
		this.config = (Read_Ini) config;
		this.log = (Log) log;
	}
	
	
}
