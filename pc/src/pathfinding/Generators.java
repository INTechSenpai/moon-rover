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
	private static Pathfinding pathfinder;
	private static int table_x;
	private static int table_y;
	
	public static void main(String[] args)
	{
		try {
			container = new Container();
			config = (Read_Ini) container.getService("Read_Ini");
			log = (Log) container.getService("Log");
			table = (Table)container.getService("Table");
			Grid2DSpace.set_static_variables(config, log);
			
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
			// Il faut instancier le pathfinding ici, car il a besoin des map générées
			pathfinder = new Pathfinding(table, config, log, 0);
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
		log.appel_static("Generation map...");
		int reductionFactor = 1;
		for(int i = 0; i < 10; i++)
		{
			Grid2DSpace map = new Grid2DSpace(reductionFactor);
			reductionFactor <<= 1;
			for(Obstacle obs: table.getListObstaclesFixes())
				map.appendObstacleFixe(obs);
			DataSaver.sauvegarder(map, "cache/map-"+i+"-"+table.codeTorches()+".cache");
		}
		log.appel_static("Generation map done.");
		
	}
	
	public static void generate_distance()
	{		
		log.appel_static("Generation distance...");
		Vec2 	depart 	= new Vec2(0,0),
				arrivee = new Vec2(0,0);
		
		int reduction = 32;
		CacheHolder output = new CacheHolder(table_x/reduction+1, table_y/reduction+1, reduction);
		
		for (int i = -table_x/2; i < (table_x/2); i+=reduction)											// depart.x		== i
		{
			System.out.println(100*((float)(i+table_x/2))/((float)table_x));
			for (int j = 0; j < table_y; j+=reduction)											// depart.y		== j
			{
				System.out.println("	"+100*((float)(j))/((float)table_y));
				for (int k = -table_x/2; k < (table_x/2); k+=reduction)									// arrivee.x	== k
					for (int l = 0; l < table_y; l+=reduction)								// arrivee.y	== l
					{
						depart.x = i;
						depart.y = j;
						arrivee.x = k;
						arrivee.y = l;
						
						// calcul de la distance, et stockage dans output
						try {
							output.data[(i+table_x/2)/reduction][j/reduction][(k+table_x/2)/reduction][l/reduction] = (short) pathfinder.distance(depart, arrivee, false);
						}
						catch(PathfindingException e)
						{
							output.data[(i+table_x/2)/reduction][j/reduction][(k+table_x/2)/reduction][l/reduction] = -1;
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
				}
			}
		}

		DataSaver.sauvegarder(output, "distance-"+table.codeTorches()+".cache");
		log.appel_static("Generation distance done.");

	}



}
