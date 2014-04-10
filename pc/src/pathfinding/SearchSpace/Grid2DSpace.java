/**
 * 
 */
package pathfinding.SearchSpace;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import exception.ConfigException;
import smartMath.IntPair;
import smartMath.LibMath;
import smartMath.Vec2;
import table.ObstacleRectangulaire;
import table.ObstacleCirculaire;
import table.Table;
import table.Obstacle;
import utils.DataSaver;
import utils.Log;
import utils.Read_Ini;

/**
 * @author Marsya, Krissprolls, pf
 *	La classe espace de recherche
 *  Pour le robot, ce sera concr�tement la table
 */

public class Grid2DSpace implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Log log;

	private boolean[][] datas;
	private static Grid2DPochoir[] pochoirs = null; // Non sérialisé car static
	private int surface;
	
	// Taille de datas
	private int sizeX;
	private int sizeY;
	
	// Taille "normale" de la table
	private int table_x = 3000;
	private int table_y = 2000;
	
	private float reductionFactor; // facteur de reduction par rapport à 1case/mm Exemple : 1500x1000 a un rapport de 0.5
	private int robotRadius;
	private int rayon_robot_adverse = 200;

	public Grid2DSpace(IntPair size, Table requestedTable, int requestedrobotRadius, Log log, Read_Ini config)
	{
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
		try {
			rayon_robot_adverse = Integer.parseInt(config.get("rayon_robot_adverse"));
		} catch (NumberFormatException | ConfigException e) {
			e.printStackTrace();
		}

		if(pochoirs == null)
		{
			pochoirs = new Grid2DPochoir[10];
			for(int degree = 0; degree < 10; degree++)
			{
				System.out.println((degree*10+1)+"%");
				int radius  = rayon_robot_adverse/LibMath.exponentiation(2, degree)+1;
				pochoirs[degree] = new Grid2DPochoir(radius);
			}
		}

		Grid2DSpace.log = log;
		robotRadius = requestedrobotRadius; 
		//Création du terrain avec obstacles fixes
		Table table = requestedTable;
		surface = size.x * size.y;
		sizeX = size.x;
		sizeY = size.y;	
		float ratio = ((float)sizeX) / ((float)sizeY);
		if(ratio != ((float)table_x)/((float)table_y))
		{
			log.warning("Grid2DSpace construction warning : given size of " + sizeX + "x" + sizeY + " is not of ratio 3/2", this);
			sizeY = (int)(((float)sizeX) / ratio)+1;
		}
		log.debug("Creating Grid2DSpace from table with a size of : " + sizeX + "x" + sizeY, this);
		
		reductionFactor = ((float)(sizeX))/3000f;
		
		log.debug("reductionFactor : " + reductionFactor, this);
		
		datas = new boolean[sizeX][sizeY];
		ArrayList<Obstacle> l_fixes = table.getListObstaclesFixes();
		
		// construit une map de sizeX * sizeY vide
		for(int i=0; i<sizeX; i++)
			for(int j=0; j<sizeY;j++)
				datas[i][j] = true;
				
		// peuple les obstacles fixes
		for(int k=0; k<l_fixes.size(); k++)
		{
			if(l_fixes.get(k) instanceof ObstacleRectangulaire)
				appendObstacle((ObstacleRectangulaire)l_fixes.get(k));
			else if(l_fixes.get(k) instanceof ObstacleCirculaire)
				appendObstacle((ObstacleCirculaire)l_fixes.get(k));
			else
				log.critical("Obstacle non géré", this);
		}
		
		// les bords de la map sont non acessibles
		appendObstacle( new ObstacleRectangulaire(new Vec2(0,0), (int)(robotRadius*1.1f), 3000));
		appendObstacle( new ObstacleRectangulaire(new Vec2(0,2000), (int)(robotRadius*1.1f), 3000));
		appendObstacle( new ObstacleRectangulaire(new Vec2(1500,1000), 2000, (int)(robotRadius*1.1f)));
		appendObstacle( new ObstacleRectangulaire(new Vec2(-1500,1000), 2000, (int)(robotRadius*1.1f)));		
		
	}

	/**
	 * Ajoute un obstacle rectangulaire au Grid2DSpace
	 * L'ajout peut être lent car il n'est jamais fait en match
	 * En effet, les obstacles temporaires sont toujours circulaires.
	 * @param obs
	 */
	public void appendObstacle(ObstacleRectangulaire obs)
	{
		// Asumptions :  	obs.getPosition() returns the top left corner of the rectangle
		//					also, rectangle is Axis Aligned...

		int marge = 20;
		
		for(int i = (int) (obs.getPosition().x - robotRadius - marge); i < (int) (obs.getPosition().x +obs.getLargeur() + robotRadius + marge); i++)	
			for(int j = (int) (obs.getPosition().y - robotRadius - marge); j < (int) (obs.getPosition().y +obs.getLongueur() + robotRadius + marge); i++)	
				if(obs.SquaredDistance(new Vec2(i,j)) < robotRadius + marge)
				{
					Vec2 posGrid = conversionTable2Grid(new Vec2(i,j));
					datas[(int)posGrid.x][(int)posGrid.y] = false;
				}
	}
	
	public void appendObstacle(ObstacleCirculaire obs)
	{
		// Asumptions :  	obs.getPosition() returns the center of the circle (pretty obvious, but still...)
		
		Vec2 posPochoir = conversionTable2Grid(obs.getPosition());
		System.out.println(posPochoir);
		int radius = (int)conversionTable2Grid(obs.getRadius());
		
		System.out.println(radius);
		
		Grid2DPochoir pochoir;
		try {
			 pochoir = pochoirs[2*radius];
				System.out.println(pochoir);
		}
		catch(Exception e)
		{
			// Si le pochoir n'existe pas, c'est que le diamètre demandé est a priori trop grand.
			// Pour ne pas faire planter le programme, on lui donne le plus grand pochoir disponible.
			e.printStackTrace();
			pochoir = pochoirs[pochoirs.length-1];
		}

		// Recopie le pochoir
		for(int i = (int)posPochoir.x - radius; i < (int)posPochoir.x + radius; i++)
			for(int j = (int)posPochoir.y - radius; j < (int)posPochoir.y + radius; j++)
				if( i >= 0 && i < sizeX && j >=0 && j < sizeY)
				{
					datas[i][j] = datas[i][j] && pochoir.datas[i-(int)posPochoir.x+radius][j-(int)posPochoir.y + radius];
					System.out.println(i+" "+j+" "+(i-(int)posPochoir.x+radius)+" "+(j-(int)posPochoir.y + radius));
				}
	}
	
	// Random map generation, debugging purpose.
	public Grid2DSpace(Vec2 size)
	{
		// initialise le terrain
		surface = (int) (size.x * size.y);
		sizeX = (int)Math.round(size.x);
		sizeY = (int)Math.round(size.y);
		log.debug("Creating random Grid2DSpace, size :" + sizeX + "x" + sizeY, this);
		
		datas = new boolean[sizeX][sizeY];
		for (int  i = 0; i < sizeX; ++i)
			for (int  j = 0; j < sizeY; ++j)
				datas[i][j] = true;
		

		ArrayList<ArrayList<Float>> floatdatas = new ArrayList<ArrayList<Float>>();
		for (int  i = 0; i < sizeX; ++i)
		{
			floatdatas.add(new ArrayList<Float>(sizeY));
			for (int  j = 0; j < sizeY; ++j)
				floatdatas.get(i).add(50.0f);
		}
		// Fais un bruit coh�rant pour la map, et apr�s on fera un seuil
		// Pour une map de 300*200 = 60 000 de surface, on veut 5-6 ilots non traversables
		// Donc un ilot par tranche de 10 000 de surface
		int nbVal = (int)Math.round(surface / 1000);	// nombre de points dont on fixe la valeur sur la map
	    Random randomGenerator = new Random();
		for (int  i = 0; i < nbVal; ++i)
		{
			// on prend un point au hasard sur la map, d'une valeur entre 0 et 100 de moyenne 50
			Vec2 ptPos = new Vec2(randomGenerator.nextInt(sizeX), randomGenerator.nextInt(sizeY));
			int ptVal = 50 + randomGenerator.nextInt(50);
			// on ajoute a chaque point de la map la division de la valeur du point au hasard par le carr� de la distance au point courant
			for (int  j = 0; j < sizeX; ++j)
				for (int  k = 0; k < sizeY; ++k)
					floatdatas.get(j).set	(	k, 
												(float) (floatdatas.get(j).get(k) + ptVal*100 / ptPos.SquaredDistance(new Vec2(j,k)))
											 );
							
		}
		// Le bruit coh�rant est fini, on met un seuil pour d�terminer si telle ou telle case est franchissable ou non
		//System.out.println(floatdatas.toString());
		int seuil = 200; 
		for (int  j = 0; j < sizeX; ++j)
			for (int  k = 0; k < sizeY; ++k)
				if((int)Math.round(floatdatas.get(j).get(k)) > seuil)
					datas[j][k] = false;			// au dessus du seuil, on ne passe pas
				else
					datas[j][k] = true;				// en dessous, ca roule
					
					
				
		
	}
	

	
	// WARNING WARNING : size have to be consistant with originalDatas
	public Grid2DSpace(Vec2 size, boolean[][] originalDatas)
	{
		surface = (int)(size.x * size.y);
		sizeX = (int)Math.round(size.x);
		sizeY = (int)Math.round(size.y);
		
		datas = new boolean[sizeX][sizeY];
		for (int  i = 0; i < sizeX; ++i)
			for (int  j = 0; j < sizeY; ++j)
				datas[i][j] = originalDatas[i][j];
					
	}
	
	public Grid2DSpace makeCopy()
	{
		return new Grid2DSpace(new Vec2(sizeX, sizeY), datas.clone());
	}

	// create an approximation of the current Grid2DSpace
	// 0 degree is the same size
	// 1 is half size
	// 2 quarter, etc...
	public Grid2DSpace makeSmallerCopy(int degree)
	{
		if (degree == 0)
			return makeCopy();
		
		int rapport = LibMath.exponentiation(2,degree);
		int new_x = (int)((float)sizeX/((float)rapport));
		int new_y = (int)((float)sizeY/((float)rapport));
		
		boolean[][] new_datas = new boolean[new_x][new_y];
		Grid2DSpace smaller = new Grid2DSpace(new Vec2(new_x, new_y), new_datas);
		
		for(int i = 0; i < new_x; i++)
			for(int j = 0; j < new_y; j++)
			{
				// On applique un ET logique
				smaller.datas[i][j] = true;				
				for(int a = 0; a < rapport; a++)
					for(int b = 0; b < rapport; b++)
						if(!datas[rapport*i+a][rapport*j+b])
							smaller.datas[i][j] = false;
			}
		
		return smaller;
	}
			
	// renvois true si le tarrain est franchissable � la position donn�e, faux sinon
	// x est de droite a gauche et y de bas en haut
	public boolean canCross(int x, int y)
	{
		// anti segfault
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY)
			return false;
		
		return datas[x][y];
	}

	
	// renvois true si le tarrain est franchissable � en ligne droite entre les 2 positions donn�es, faux sinon
	public boolean canCrossLine(int x0, int y0, int x1, int y1)
	{
		 // Bresenham's line algorithm
		
		  int dx = Math.abs(x1-x0), sx = x0<x1 ? 1 : -1;
		  int dy = Math.abs(y1-y0), sy = y0<y1 ? 1 : -1; 
		  int err = (dx>dy ? dx : -dy)/2;
		  int e2;
		 
		  while(true)
		  {
			    if (!canCross(x0,y0))
			    	return false;

				if (x0==x1 && y0==y1) break;
				
				e2 = err;
				if (e2 >-dx) { err -= dy; x0 += sx; }
				if (e2 < dy) { err += dx; y0 += sy; }
		  }
		 return true;
	}
	
	// debug purpose
	public boolean drawLine(int x0, int y0, int x1, int y1)
	{
		

		ArrayList<IntPair> ligne = new ArrayList<IntPair>();

		  int dx = Math.abs(x1-x0), sx = x0<x1 ? 1 : -1;
		  int dy = Math.abs(y1-y0), sy = y0<y1 ? 1 : -1; 
		  int err = (dx>dy ? dx : -dy)/2;
		  int e2;
		 
		  while(true)
		  {
			  ligne.add(new IntPair(x0,y0));
	
	
				if (x0==x1 && y0==y1) break;
				e2 = err;
				if (e2 >-dx) { err -= dy; x0 += sx; }
				if (e2 < dy) { err += dx; y0 += sy; }
		  }
		  
		  
		  
	
			String out = "";
			for (int  j = 0; j < sizeX; ++j)
			{
				for (int  k = sizeY - 1; k >= 0; --k)
				{
					IntPair pos = new IntPair(j,k);
					if (ligne.contains(pos))
						out += '�';
					else if(canCross(j, k))
						out += '.';
					else
						out += 'X';	
				}
				
				out +='\n';
			}
			System.out.println(out);
			return true;
	}

	public float getSurface() 
	{
		return surface;
	}

	public int getSizeX() 
	{
		return sizeX;
	}

	public int getSizeY() 
	{
		return sizeY;
	}
	
	public String stringForm()
	{
		String out = "";
		for (int  j = 0; j < sizeX; ++j)
		{
			for (int  k = sizeY - 1; k >= 0; --k)
				if(datas[j][k])
					out += '.';
				else
					out += 'X';	
			
			out +='\n';
		}
		return out;
	}

	/**
	 * Clone "this" into "other". "this" is not modified.
	 * @param other
	 */
	public void clone(Grid2DSpace other)
	{
		for (int i = 0; i < datas.length; i++) {
		    System.arraycopy(datas[i], 0, other.datas[i], 0, datas[0].length);
		}
	}
	
	/**
	 * Retourne un objet Grid2DSpace sauvegardé.
	 * Les objets sauvegardés seront les 4 map de base, avec les obstacles fixes
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static Grid2DSpace load(int millimetresParCases, int nb_table) throws IOException, ClassNotFoundException
	{
		return (Grid2DSpace) DataSaver.charger("cache/map-"+millimetresParCases+"-"+nb_table+".cache");
	}
	
	public static void cache_file_generate(Log log, Read_Ini config, Table table, String filename, int millimetresParCases, int robotRadius)
	{
		IntPair size = new IntPair(3000/millimetresParCases, 2000/millimetresParCases);
		boolean[][] originalDatas = new boolean[size.x][size.y];
		for(int i = 0; i < size.x; i++)
			for(int j = 0; j < size.y; j++)
				originalDatas[i][j] = true;
		
		ArrayList<Obstacle> obstacles_fixes = table.getListObstaclesFixes();
		Grid2DSpace output = new Grid2DSpace(size, table, robotRadius, log, config);
		for(Obstacle o: obstacles_fixes)
		{
			if(o instanceof ObstacleCirculaire)
				output.appendObstacle((ObstacleCirculaire)o);
			else if(o instanceof ObstacleRectangulaire)
				output.appendObstacle((ObstacleRectangulaire)o);
			else
				log.appel_static("Erreur: type d'obstacle inconnu");
		}
		log.appel_static("Grid2DSpace serializing");
		// Sauvegarde du fichier de cache à partir de l'instance output de CacheHolder
		DataSaver.sauvegarder(output, filename);
	}
	
	/**
	 * Convertit une longueur depuis les unités de la table dans les unités de la grille
	 * @param nb
	 * @return
	 */
	public int conversionTable2Grid(int nb)
	{
		return (int)Math.round(((float)nb) * reductionFactor);
	}

	/**
	 * Convertit une longueur depuis les unités de la table dans les unités de la grille
	 * @param nb
	 * @return
	 */
	public float conversionTable2Grid(float nb)
	{
		return Math.round(nb * reductionFactor);
	}
	
	/**
	 * Convertit un point depuis les unités de la table dans les unités de la grille
	 * @return
	 */
	public Vec2 conversionTable2Grid(Vec2 pos)
	{
		return new Vec2(conversionTable2Grid(pos.x + table_x/2),
						conversionTable2Grid(pos.y));
	}
	
}
