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
 * Classe utilisée à l'initialisation du robot pour charger en RAM le fichier de cache.
 * @author karton
 *
 */

public class CacheLoader 
{
	
	// retourne l'instance du cache si correctemnt chargé, null sinon.
	public CacheHolder loadCacheFile(String filename)
	{
		CacheHolder output = null;
		try
		{
		   FileInputStream fileIn = new FileInputStream(filename);
		   ObjectInputStream in = new ObjectInputStream(fileIn);
		   output = (CacheHolder) in.readObject();
		   in.close();
		   fileIn.close();
		}
		catch(IOException i)
		{
		   i.printStackTrace();
		   return null;
		}
		catch(ClassNotFoundException c)
		{
		   System.out.println("CacheHolder class not found");
		   c.printStackTrace();
		   return null;
		}

		return output;
	}
}
