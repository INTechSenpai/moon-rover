package pathfinding.SearchSpace;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import tests.JUnit_Test;



/**
 * Classe générant le code de la classe singleton initialisant les pochoirs en mémoire.
 * Oh year.
 * 
 * Penser a désctiver dans eclipse la limitation de la taille du buffer de la console
 * Attention, lorsque l'on commence à arriver dans un rayon max de 50, la sortie de codee exède les 50 000 lignes
 * 		Du coup faites super gaffe, eclipse peut facilement péter un câble
 * @author karton
 *
 */


public class Grid2DPochoirManagerGenerator  extends JUnit_Test 
{
	
	Pochoir[] pochoirList;
	
	public void generate()
	{
		int maxDiameter = 60;
		pochoirList = new Pochoir[maxDiameter+2];
		
		
		for (int i = 0; i < maxDiameter+1; i++)
		{
			log.special("Generating pochoir with radius of : " + i);
			pochoirList[i].data = new boolean[i][i];
			for (int j = 0; j < i; j++)
				for (int k = 0; k < i; k++)
				{
					if(Math.sqrt((k-i/2)*(k-i/2) + (j-i/2)*(j-i/2))<i/2)
						pochoirList[i].data[j][k] = false;
					else
						pochoirList[i].data[j][k] = true;
				}
		}
		

		// Sauvegarde du fichier de cache à partir de l'instance output de CacheHolder

		for (int i = 0; i < maxDiameter+1; i++)
		{
			  try
			  {
			     FileOutputStream fileOut = new FileOutputStream("Pochoir" + i + ".cache");
				 ObjectOutputStream out = new ObjectOutputStream(fileOut);
				 out.writeObject(pochoirList[i]);
				 out.close();
				 fileOut.close();
				 log.special("Serialized data is saved in "+"Pochoir" + i + ".cache");
			  }
			  catch(IOException io)
			  {
			      io.getStackTrace();
			  }
		}
	}
}
