package generators;

import container.Container;
import pathfinding.CacheHolder;
import robot.Cote;
import table.Table;
import tests.JUnit_Test;
import utils.Log;
import utils.Read_Ini;

/**
 * Génère les fichiers de cache de distance du pathfinding
 * @author pf, Martial
 *
 */

class DistanceCacheGenerator extends JUnit_Test
{
	private static Container container;
	private static Read_Ini config;
	private static Log log;
	private static Table table;
	
	public static void main(String[] args)
	{
		try {
			container = new Container();
			config = (Read_Ini) container.getService("Read_Ini");
			log = (Log) container.getService("Log");
			table = (Table)container.getService("Table");
			table.initialise();
			generate(false, false);
			table.initialise();
			generate(false, true);
			table.initialise();
			generate(true, false);
			table.initialise();
			generate(true, true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void generate(boolean droite, boolean gauche) throws Exception
	{
		log.appel_static("\n\n ====== Generation cache distance for pathfinding =====");
		if(droite)
			table.torche_disparue(Cote.DROIT);
		if(gauche)
			table.torche_disparue(Cote.GAUCHE);
		CacheHolder.cache_file_generate(config, log, table, "cache/distance-"+table.codeTorches()+".cache");
	}
		
}
