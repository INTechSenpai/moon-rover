package generators;

import pathfinding.SearchSpace.Grid2DSpace;
import robot.Cote;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Container;
import exception.ConfigException;

public class MapCacheGenerator {

	private static Container container;
	private static Read_Ini config;
	private static Log log;
	private static Table table;
	private static int rayon_robot = 200;
	
	public static void main(String[] args)
	{
		try {
			container = new Container();
			config = (Read_Ini) container.getService("Read_Ini");
			log = (Log) container.getService("Log");
			table = (Table)container.getService("Table");
			try {
				rayon_robot = Integer.parseInt(config.get("rayon_robot"));
			} catch (NumberFormatException | ConfigException e) {
				e.printStackTrace();
			}

			log.appel_static("\n\n ====== Generation cache map for pathfinding =====");
			table.initialise();
			generate(false, false);
			table.initialise();
			generate(false, true);
			table.initialise();
			generate(true, false);
			table.initialise();
			generate(true, true);
			log.appel_static("\n\n ====== Generation ended =====");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void generate(boolean droite, boolean gauche) throws Exception
	{
		if(droite)
			table.torche_disparue(Cote.DROIT);
		if(gauche)
			table.torche_disparue(Cote.GAUCHE);
		int reductionFactor = 1;
		for(int i = 0; i < 10; i++)
		{
			Grid2DSpace.cache_file_generate(log, config, table, "cache/map-"+reductionFactor+"-"+table.codeTorches()+".cache", reductionFactor, rayon_robot);
			reductionFactor <<= 1;
		}
	}
		
}
