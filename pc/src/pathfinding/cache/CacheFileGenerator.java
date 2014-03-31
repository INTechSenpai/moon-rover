/**
 * 
 */
package pathfinding.cache;


import pathfinding.Pathfinding;
import smartMath.Vec2;
import table.Table;
import java.util.ArrayList;

import java.io.*;

/**
 * 	Classe chargée du calcul du fichier de cache, chargé par le loader.
 * La classe n'a pas besoin d'être instanciée pour de l'utilisation du cache, mais elle est centrale pour la génération du cache 
 * @author karton
 *
 */
public class CacheFileGenerator 
{
	private Pathfinding pathfinder;
	private int centimetresParCase;
	private CacheHolder output;
	
	private int sizeX;
	private int sizeY;
	
	public CacheFileGenerator(int requestedcentimetresParCase, Table table)
	{
		System.out.println("CacheFileGenerator initialisation");
		
		// Taille de la map
		centimetresParCase = requestedcentimetresParCase;
		sizeX = 300 / centimetresParCase;
		sizeY = 200 / centimetresParCase;
		
		pathfinder = new Pathfinding(table, null, null, centimetresParCase);
		Vec2 	depart 	= new Vec2(0,0),
				arrivee = new Vec2(0,0);
		
		
		System.out.println("CacheFileGenerator starting calculations");
		// grosse boucle de remplissage du cache
		for (int i = 0; i < sizeX; ++i)											// depart.x		== i
		{
			output.data.add(new ArrayList<ArrayList<ArrayList<Integer>>>());
			for (int j = 0; j < sizeY; ++j)										// depart.y		== j
			{
				output.data.get(i).add(new ArrayList<ArrayList<Integer>>());
				for (int k = 0; k < sizeX; ++k)									// arrivee.x	== k
				{
					output.data.get(i).get(j).add(new ArrayList<Integer>());
					for (int l = 0; l < sizeY; ++l)								// arrivee.y	== l
					{
						depart.x = i;
						depart.y = j;
						arrivee.x = k;
						arrivee.y = l;
						
						// calcul de la distance, et stockage dans output
						output.data.get(i).get(j).get(k).add(pathfinder.distance(depart, arrivee, false));
					}
				}
			}
		}
		
		
		// Sauvegarde du fichier de cache à partir de l'instance output de CacheHolder
		  try
		  {
		     FileOutputStream fileOut = new FileOutputStream("Pathfinding.cache");
			 ObjectOutputStream out = new ObjectOutputStream(fileOut);
			 out.writeObject(output);
			 out.close();
			 fileOut.close();
			 System.out.printf("Serialized data is saved in Pathfinding.cache");
		  }
		  catch(IOException i)
		  {
		      i.printStackTrace();
		  }
	}
}
