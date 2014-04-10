package pathfinding;

import pathfinding.SearchSpace.Grid2DSpace;
import robot.Cote;
import smartMath.Vec2;
import table.Obstacle;
import table.Table;
import utils.DataSaver;
import utils.Log;
import utils.Read_Ini;
import container.Container;
import exception.ConfigException;
import exception.PathfindingException;

/**
 * Générateur des fichiers caches
 * @author pf
 *
 */

public class Generators {

	private static Container container;
	private static Read_Ini config;
	private static Log log;
	private static Table table;
	private static int table_x;
	private static int table_y;
	
	public static void main(String[] args)
	{
		try {
			container = new Container();
			config = (Read_Ini) container.getService("Read_Ini");
			log = (Log) container.getService("Log");
			table = (Table)container.getService("Table");
			try {
				table_x = Integer.parseInt(config.get("table_x"));
			} catch (NumberFormatException | ConfigException e) {
				e.printStackTrace();
			}
			try {
				table_y = Integer.parseInt(config.get("table_y"));
			} catch (NumberFormatException | ConfigException e) {
				e.printStackTrace();
			}
		
			table.initialise();
			generate_map();
			generate_distance();
			table.initialise();
			table.torche_disparue(Cote.GAUCHE);
			generate_map();
			generate_distance();
			table.initialise();
			table.torche_disparue(Cote.DROIT);
			generate_map();
			generate_distance();
			table.initialise();
			table.torche_disparue(Cote.GAUCHE);
			table.torche_disparue(Cote.DROIT);
			generate_map();
			generate_distance();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void generate_map()
	{
		Grid2DSpace.set_static_variables(config, log);

		int reductionFactor = 1;
		for(int i = 0; i < 10; i++)
		{
			System.out.println(reductionFactor);
			Grid2DSpace map = new Grid2DSpace(reductionFactor);
			reductionFactor <<= 1;
			for(Obstacle obs: table.getListObstaclesFixes())
				map.appendObstacleFixe(obs);
			DataSaver.sauvegarder(map, "cache/map-"+reductionFactor+"-"+table.codeTorches()+".cache");
		}
		
	}
	
	public static void generate_distance() throws PathfindingException
	{		
		Pathfinding pathfinder = new Pathfinding(table, config, log, 0);
		Vec2 	depart 	= new Vec2(0,0),
				arrivee = new Vec2(0,0);
		
		CacheHolder output = new CacheHolder(table_x, table_y);
		
		for (int i = -table_x/2; i < (table_x/2); ++i)											// depart.x		== i
			for (int j = 0; j < table_y; ++j)											// depart.y		== j
				for (int k = -table_x/2; k < (table_x/2); ++k)									// arrivee.x	== k
					for (int l = 0; l < table_y; ++l)								// arrivee.y	== l
					{
						depart.x = i;
						depart.y = j;
						arrivee.x = k;
						arrivee.y = l;
						
						// calcul de la distance, et stockage dans output
						output.data[i+table_x/2][j][k+table_x/2][l] = pathfinder.distance(depart, arrivee, false);
					}
		DataSaver.sauvegarder(output, "distance-"+table.codeTorches()+".cache");
	}



}
