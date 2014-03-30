package pathfinding.SearchSpace;



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


public class Grid2DPochoirManagerGenerator 
{
	// envois du lourd
	public void generate()
	{
		int maxDiameter = 60;
		boolean generateDebugPrint = false;
		System.out.println("//== Generation Output begin ==");
		System.out.println("//Génération du code...");
		
		// class header :
		
		
		// declaration des imports
		String out = "package pathfinding.SearchSpace;\n";
		
		out +="import java.util.ArrayList;\n";
		//out +="import java.util.Random;\n";
		//out +="import smartMath.IntPair;\n";
		//out +="import smartMath.Vec2;\n";
		//out +="import table.ObstacleRectangulaire;\n";
		//out +="import table.ObstacleCirculaire;\n";
		//out +="import table.Table;\n";
		//out +="import table.Obstacle;\n";
		out +="\n";
		
		// commentaire en début de classe
		out +="/**\n";
		out +="* @author Le programme de martial\n";
		out +="*	Rien a lire dans le code, la classe est un singleton qui à l'instanciation initialisera tout les pochoirs en mémoire\n";
		out +="* Taille max actuelle des pochoirs : " + maxDiameter + " cases de diamètre\n";
		out +="*/\n";
		out +="\n";
		
		// déclaration de la classe
		out +="public class Grid2DPochoirManager \n";
		out +="{\n";
			
		// déclaration de datas
		out +="	public static ArrayList<ArrayList<ArrayList<Boolean>>> datas = new ArrayList<ArrayList<ArrayList<Boolean>>>();\n";
		out +="\n";

		// déclaration du constructeur
		out +="	public static void main(String[] args) \n";
		out +="	{\n";

		out +="		// initialise la liste des pochoirs\n";
		out +="		datas = new ArrayList<ArrayList<ArrayList<Boolean>>>();\n";
		out +="\n";
		

		out +="		// appelle toutes les méthodes des lignes de tout les tableaux\n";
		for (int i = 0; i < maxDiameter+1; i++)
		{
			// ajoute le tableau n°i
			out += "		// ajoute le tableau n°" + i + "\n";
			out += "		datas.add( new ArrayList<ArrayList<Boolean>>() );\n";
			out += "\n";
			
			for (int j = 0; j < i; j++)
				out +="		addTableau" + i + "ligne" + j + "();\n";
			out += "\n";
		}
		out +="\n";
		
		// print le résultat si demandé a la génération du code
		if (generateDebugPrint)
		{
			out +="		// print le résultat comme demandé lors de la génération du code\n";	
			out +="		String out = \"\";\n";
			out +="		for (int  j = 0; j < " + maxDiameter + "; ++j)\n";
			out +="		{\n";
			out +="			for (int  k = 0; k < " + maxDiameter + "; ++k)\n";
			out +="			{\n";
			out +="				if (datas.get(" + maxDiameter + ").get(j).get(k))\n";
			out +="					out += '.';\n";
			out +="				else \n";
			out +="					out += 'X';\n";
			out +="			}\n";
			out +="			\n";
			out +="			out += \"\\n\";\n";
			out +="		}\n";
			out +="		System.out.println(out);\n";
		}
		
		// fin du constructeur
		out +="	}\n";
		out += "\n";
			
			
			
		// génère le code de chaque méthode pour faire les tableaux
		for (int i = 0; i < maxDiameter+1; i++)
		{			
			System.out.println("//Génération du tableau n°" + i);
			for (int j = 0; j < i; j++)
			{

				// déclaration la jème méthode du ième tableau
				out +="	public static void addTableau" + i + "ligne" + j + "()\n";
				out +="	{\n";
				out += "\n";		
				
				// ajoute la ligne
				out += "		// ligne n°" + j + "\n";
				out += "		datas.get(" + i + ").add( new ArrayList<Boolean>() );\n";
				
				
				for (int k = 0; k < i; k++)
				{
					if(Math.sqrt((k-i/2)*(k-i/2) + (j-i/2)*(j-i/2))<i/2)
						out += "		datas.get(" + i + ").get(" + j + ").add(false);\n";
					else
						out += "		datas.get(" + i + ").get(" + j + ").add(true);\n";
						
				}
				out += "\n";
				
			// fin de la jème méthode du ième tableau
			out +="	}\n";
			out += "\n";
				
			}
			

		}
		
		// fin de la classe 

		out +="}\n";
			

		System.out.println("//== Generation Output end ==");
		System.out.println(out);
		
	}
}
