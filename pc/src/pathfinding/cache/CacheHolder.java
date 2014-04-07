/**
 * 
 */
package pathfinding.cache;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import pathfinding.Pathfinding;
import smartMath.Vec2;
import table.Table;
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
		String filename = "distance-"+i+".cache";
		// retourne l'instance du cache si correctemnt chargé, lève une exception sinon
		CacheHolder out = null;
	    FileInputStream fileIn = new FileInputStream(filename);
	    ObjectInputStream in = new ObjectInputStream(fileIn);
	    out = (CacheHolder) in.readObject();
	    in.close();
	    fileIn.close();
		return out;
	}
	
	/**
	 * Génère le cache des distances pour la table demandée
	 * @param config
	 * @param log
	 * @param table: la table pour laquelle le cache est demandé
	 * @param filename: nom de fichier dans lequel sera enregistré le cache
	 */
	public static void cache_file_generate(Read_Ini config, Log log, Table table, String filename)
	{
		log.special("CacheFileGenerator initialisation");
		
		// Taille de la map. Précision maximale pour ce cache.
		int sizeX = 1500;
		int sizeY = 2000;
		
		Pathfinding pathfinder = new Pathfinding(table, config, log, 1);
		Vec2 	depart 	= new Vec2(0,0),
				arrivee = new Vec2(0,0);
		
		CacheHolder output = new CacheHolder(2*sizeX, sizeY);
		
		log.special("CacheFileGenerator starting calculations");
		// grosse boucle de remplissage du cache
		for (int i = -sizeX; i < sizeX; ++i)											// depart.x		== i
			for (int j = 0; j < sizeY; ++j)											// depart.y		== j
				for (int k = -sizeX; k < sizeX; ++k)									// arrivee.x	== k
					for (int l = 0; l < sizeY; ++l)								// arrivee.y	== l
					{
						depart.x = i;
						depart.y = j;
						arrivee.x = k;
						arrivee.y = l;
						
						// calcul de la distance, et stockage dans output
						output.data[i+1500][j][k+1500][l] = pathfinder.distance(depart, arrivee, false);
					}

		log.special("CacheFileGenerator calculations done !");
		

		log.special("CacheFileGenerator serializing");
		// Sauvegarde du fichier de cache à partir de l'instance output de CacheHolder
		  try
		  {
		     FileOutputStream fileOut = new FileOutputStream(filename);
			 ObjectOutputStream out = new ObjectOutputStream(fileOut);
			 out.writeObject(output);
			 out.close();
			 fileOut.close();
			 log.special("Serialized data is saved in "+filename);
		  }
		  catch(IOException i)
		  {
		      i.printStackTrace();
		  }
	}

}
