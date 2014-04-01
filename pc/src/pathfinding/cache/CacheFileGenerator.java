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
		sizeX = 1500;
		sizeY = 2000;
		
		pathfinder = new Pathfinding(table, null, null, centimetresParCase);
		Vec2 	depart 	= new Vec2(0,0),
				arrivee = new Vec2(0,0);
		
		output = new CacheHolder();
		output.data = new ArrayList<ArrayList<ArrayList<ArrayList<Integer>>>>(); 
		
		System.out.println("CacheFileGenerator starting calculations");
		// grosse boucle de remplissage du cache
		for (int i = -sizeX; i < sizeX; ++i)											// depart.x		== i
		{
			System.out.println(" i = " + i);
			output.data.add(new ArrayList<ArrayList<ArrayList<Integer>>>());
			for (int j = 0; j < sizeY; ++j)											// depart.y		== j
			{
				//System.out.println(" j = " + j);
				output.data.get(i+1500).add(new ArrayList<ArrayList<Integer>>());
				for (int k = -sizeX; k < sizeX; ++k)									// arrivee.x	== k
				{
					//System.out.println(" k = " + k);
					output.data.get(i+1500).get(j).add(new ArrayList<Integer>());
					for (int l = 0; l < sizeY; ++l)								// arrivee.y	== l
					{

						//System.out.println(" l = " + l);
						depart.x = i;
						depart.y = j;
						arrivee.x = k;
						arrivee.y = l;
						
						// calcul de la distance, et stockage dans output
						output.data.get(i+1500).get(j).get(k+1500).add(pathfinder.distance(depart, arrivee, false));
					}
				}
			}
		}

		System.out.println("CacheFileGenerator calculations done !");
		

		System.out.println("CacheFileGenerator serializing");
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
