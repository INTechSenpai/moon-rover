/**
 * 
 */
package pathfinding.SearchSpace;

import java.io.Serializable;

import exception.ConfigException;
import smartMath.Vec2;
import table.ObstacleRectangulaire;
import table.ObstacleCirculaire;
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

	private boolean[][] datas;
	private static Grid2DPochoir[] pochoirs; // Non sérialisé car static
	private int surface;
	
	// Taille de datas
	private int sizeX;
	private int sizeY;
	
	// Taille "normale" de la table
	private static int table_x = 3000;
	private static int table_y = 2000;
	
	private int reductionFactor; // facteur de reduction par rapport à 1case/mm Exemple : 1500x1000 a un rapport de 2
	private int num_pochoir; // 2^num_pochoir = reductionFactor
	private static int robotRadius;
	private static int rayon_robot_adverse = 200;
	
	
	public static void set_static_variables(Read_Ini config, Log log)
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
		try {
			robotRadius = Integer.parseInt(config.get("rayon_robot"));
		} catch (NumberFormatException | ConfigException e) {
			e.printStackTrace();
		}

		log.appel_static("Début génération pochoirs");
		pochoirs = new Grid2DPochoir[10];
		int reduction = 1;
		for(int i = 0; i < 10; i++)
		{
			pochoirs[i] = new Grid2DPochoir(rayon_robot_adverse/reduction);
			reduction <<= 1;
		}
		log.appel_static("Pochoirs générés");
		
	}
	
	/**
	 * Ce constructeur est utilisé uniquement pour générer les caches.
	 * Construit un Grid2DSpace vide.
	 * @param reductionFactor
	 */
	public Grid2DSpace(int reductionFactor)
	{
		//Création du terrain avec obstacles fixes
		sizeX = table_x/reductionFactor;
		sizeY = table_y/reductionFactor;
		surface = sizeX * sizeY;
		reductionFactor = table_x/sizeX;
		
		for(int num_pochoir = 0; num_pochoir < 10; num_pochoir++)
		{
			if(reductionFactor == 1)
				break;
			reductionFactor >>= 1;
		}		
		
		datas = new boolean[sizeX+1][sizeY+1];
		// construit une map de sizeX * sizeY vide
		for(int i=0; i<sizeX; i++)
			for(int j=0; j<sizeY;j++)
				datas[i][j] = true;		
	}

	/**
	 * Ajout long d'un obstacle rectangulaire.
	 * Exécuté seulement lors de la génération du cache.
	 * @param obs
	 */
	public void appendObstacle(ObstacleRectangulaire obs)
	{
		// Asumptions :  	obs.getPosition() returns the top left corner of the rectangle
		//					also, rectangle is Axis Aligned...
		int marge = 20;
		for(int i = (int) (obs.getPosition().x - robotRadius - marge); i < (int) (obs.getPosition().x +obs.getLargeur() + robotRadius + marge + 1); i++)	
			for(int j = (int) (obs.getPosition().y - robotRadius - marge); j < (int) (obs.getPosition().y +obs.getLongueur() + robotRadius + marge + 1); j++)	
				if(i >= -table_x/2 && i < table_x/2 && j >= 0 && j < table_y && obs.distance(new Vec2(i,j)) < robotRadius + marge)
				{
					Vec2 posGrid = conversionTable2Grid(new Vec2(i,j));
					datas[(int)posGrid.x][(int)posGrid.y] = false;
				}
	}

	/**
	 * Ajout long d'un obstacle circulaire.
	 * Exécuté seulement lors de la génération du cache.
	 * @param obs
	 */
	public void appendObstacle(ObstacleCirculaire obs)
	{
		int marge = 20;
		int radius = (int) obs.getRadius();
		for(int i = (int) (obs.getPosition().x - robotRadius - marge - radius); i < (int) (obs.getPosition().x + radius + robotRadius + marge + 1); i++)	
			for(int j = (int) (obs.getPosition().y - robotRadius - marge - radius); j < (int) (obs.getPosition().y + radius + robotRadius + marge + 1); j++)
				if(i >= -table_x/2 && i < table_x/2 && j >= 0 && j < table_y && obs.getPosition().distance(new Vec2(i,j)) < radius + robotRadius + marge)
				{
					Vec2 posGrid = conversionTable2Grid(new Vec2(i,j));
					datas[(int)posGrid.x][(int)posGrid.y] = false;
				}
	}

	
	/**
	 * Ajout optimisé d'obstacle temporaires, de taille fixe.
	 * Utilise les pochoirs.
	 * @param obs
	 */
	public void appendObstacleTemporaire(ObstacleCirculaire obs)
	{
		// Asumptions :  	obs.getPosition() returns the center of the circle (pretty obvious, but still...)
		
		Vec2 posPochoir = conversionTable2Grid(obs.getPosition());
		
		Grid2DPochoir pochoir = pochoirs[num_pochoir];

		int radius = pochoir.datas[0].length/2;

		// Recopie le pochoir
		for(int i = (int)posPochoir.x - radius; i < (int)posPochoir.x + radius; i++)
			for(int j = (int)posPochoir.y - radius; j < (int)posPochoir.y + radius; j++)
				if( i >= 0 && i < sizeX && j >=0 && j < sizeY)
				{
					datas[i][j] = datas[i][j] && pochoir.datas[i-(int)posPochoir.x+radius][j-(int)posPochoir.y + radius];
//					System.out.println(i+" "+j+" "+(i-(int)posPochoir.x+radius)+" "+(j-(int)posPochoir.y + radius));
				}
	}
	
/*	// Random map generation, debugging purpose.
	public Grid2DSpace(Vec2 size)
	{
		// initialise le terrain
		surface = (int) (size.x * size.y);
		sizeX = (int)Math.round(size.x);
		sizeY = (int)Math.round(size.y);
		log.debug("Creating random Grid2DSpace, size :" + sizeX + "x" + sizeY, this);
		
		datas = new boolean[sizeX+1][sizeY+1]; // petite marge pour les problèmes de divisions entières
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
	*/

/*	
	// WARNING WARNING : size have to be consistant with originalDatas
	public Grid2DSpace(Vec2 size, boolean[][] originalDatas)
	{
		surface = (int)(size.x * size.y);
		sizeX = (int)size.x;
		sizeY = (int)size.y;
		
		datas = new boolean[sizeX+1][sizeY+1];
		for (int  i = 0; i < sizeX; ++i)
			for (int  j = 0; j < sizeY; ++j)
				datas[i][j] = originalDatas[i][j];
					
	}
	*/
	// Utilisé seulement par makecopy
	private Grid2DSpace()
	{
	}
	
	/**
	 * Génère une copie. Utilise la méthode clone.
	 * @return
	 */
	public Grid2DSpace makeCopy()
	{
		Grid2DSpace output = new Grid2DSpace();
		clone(output);
		return output;
	}
	
	/**
	 * A priori non utilisé
	 *  Create an approximation of the current Grid2DSpace
	 *  Réduction = 1: pleine taille
	 *  Réduction = 2: demi-taille
	 *  Réduction = 3: tiers de la taille
	 *  Etc.
	 * @param reduction
	 * @return
	 */
/*	public Grid2DSpace makeSmallerCopy(int reduction)
	{
		if (reduction == 1)
			return makeCopy();
		
		int new_x = sizeX/reduction;
		int new_y = sizeY/reduction;
		
		boolean[][] new_datas = new boolean[new_x+1][new_y+1];
		Grid2DSpace smaller = new Grid2DSpace(new Vec2(new_x, new_y), new_datas);
		
		for(int i = 0; i < new_x; i++)
			for(int j = 0; j < new_y; j++)
			{
				// On applique un ET logique
				smaller.datas[i][j] = true;
				for(int a = 0; a < reduction; a++)
				{
					for(int b = 0; b < reduction; b++)
						if(!datas[reduction*i+a][reduction*j+b])
						{
							smaller.datas[i][j] = false;
							break; // évaluation paresseuse
						}
					if(!smaller.datas[i][j])
						break;
				}
			}
		
		return smaller;
	}
*/			
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
	
/*	// debug purpose
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
*/
	/**
	 * Clone "this" into "other". "this" is not modified.
	 * @param other
	 */
	public void clone(Grid2DSpace other)
	{
		for (int i = 0; i < datas.length; i++) {
		    System.arraycopy(datas[i], 0, other.datas[i], 0, datas[0].length);
		}
		other.surface = surface;
		other.sizeX = sizeX;
		other.sizeY = sizeY;
		other.reductionFactor = reductionFactor;
		other.num_pochoir = num_pochoir;
	}
	
	/**
	 * Convertit une longueur depuis les unités de la table dans les unités de la grille
	 * @param nb
	 * @return
	 */
	private float conversionTable2Grid(float nb)
	{
		return nb / reductionFactor;
	}
	
	/**
	 * Convertit un point depuis les unités de la table dans les unités de la grille
	 * @return
	 */
	private Vec2 conversionTable2Grid(Vec2 pos)
	{
		return new Vec2(conversionTable2Grid(pos.x + table_x/2),
						conversionTable2Grid(pos.y));
	}
	
	public int getReductionFactor()
	{
		return reductionFactor;
	}
	
}
