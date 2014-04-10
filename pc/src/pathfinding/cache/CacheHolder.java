/**
 * 
 */
package pathfinding.cache;

import java.io.IOException;
import java.io.Serializable;

import exception.ConfigException;
import exception.PathfindingException;
import pathfinding.Pathfinding;
import smartMath.Vec2;
import table.Table;
import utils.DataSaver;
import utils.Log;
import utils.Read_Ini;

/**
 * Classe contenant l'ensemble des données brutes du cache
 * C'est elle qui sera très grosse en mémoire.
 * 
 * Le cache contient toutes les distances entre tout les couples de points de la table avec obstacles fixes
 * On stocke les distances de mannathan, en int.
 * Il y a besoin de 2 parmètres pour le point de départ, et 2 paramètres pour le point d'arrivée.
 * Du coup on bosses sur une classe qui sera grosso modo un gros tableau d'int à 4 dimentions.
 * @author Marsu, pf
 *
 */
public class CacheHolder implements Serializable
{
	private static final long serialVersionUID = 1L;

	public int[][][][] data;
	
	public CacheHolder(int sizeX, int sizeY)
	{
		data = new int[sizeX][sizeY][sizeX][sizeY];
	}
	
	public static CacheHolder load(int i) throws IOException, ClassNotFoundException
	{
		return (CacheHolder) DataSaver.charger("distance-"+i+".cache");
	}
	
	/**
	 * Génère le cache des distances pour la table demandée
	 * @param config
	 * @param log
	 * @param table: la table pour laquelle le cache est demandé
	 * @param filename: nom de fichier dans lequel sera enregistré le cache
	 * @throws PathfindingException 
	 */
	public static void cache_file_generate(Read_Ini config, Log log, Table table, String filename) throws PathfindingException
	{
		log.appel_static("CacheFileGenerator initialisation");
		int table_x = 3000;
		int table_y = 2000;
		
		// Taille de la map. Précision maximale pour ce cache.
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
		
		Pathfinding pathfinder = new Pathfinding(table, config, log, 1);
		Vec2 	depart 	= new Vec2(0,0),
				arrivee = new Vec2(0,0);
		
		CacheHolder output = new CacheHolder(table_x, table_y);
		
		log.appel_static("CacheFileGenerator starting calculations");
		// grosse boucle de remplissage du cache
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

		log.appel_static("CacheFileGenerator calculations done !");
		

		log.appel_static("CacheFileGenerator serializing");
		// Sauvegarde du fichier de cache à partir de l'instance output de CacheHolder
		DataSaver.sauvegarder(output, filename);
	}

}
