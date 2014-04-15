/**
 * 
 */
package pathfinding.SearchSpace;

import java.io.Serializable;
import java.util.ArrayList;

import exception.ConfigException;
import smartMath.Vec2;
import table.Obstacle;
import table.ObstacleRectangulaire;
import table.ObstacleCirculaire;
import utils.Log;
import utils.Read_Ini;

/**
 * @author Marsya, Krissprolls, pf
 *	La classe espace de recherche
 *  Pour le robot, ce sera concr�tement la table
 *  Toutes les méthodes sont appelées dans les coordonnées de la grille (sauf les conversions)
 */

public class Grid2DSpace implements Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean[][] datas;
	private static Grid2DPochoir[] pochoirs = null; // Non sérialisé car static
//	private int surface;
	
	// Taille de datas
	private int sizeX;
	private int sizeY;
	
	// Taille "normale" de la table
	private static int table_x = 3000;
	private static int table_y = 2000;
	private static int marge = 20;
	
	private int num_pochoir; // 2^num_pochoir = reductionFactor
	private static int robotRadius;
	private static int rayon_robot_adverse = 200;
	
	
	/**
	 * Utilisé très rarement.
	 * @param config
	 * @param log
	 */
	public static void set_static_variables(Read_Ini config, Log log)
	{
		if(pochoirs == null)
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
			try {
				marge = Integer.parseInt(config.get("marge"));
			} catch (NumberFormatException | ConfigException e) {
				e.printStackTrace();
			}
	
			pochoirs = new Grid2DPochoir[10];
			for(int i = 0; i < 10; i++)
				pochoirs[i] = new Grid2DPochoir(rayon_robot_adverse >> i);
		}
	}
	
	/**
	 * Ce constructeur est utilisé uniquement pour générer les caches.
	 * Construit un Grid2DSpace vide.
	 * @param reductionFactor
	 */
	public Grid2DSpace(int num_pochoir)
	{
		//Création du terrain avec obstacles fixes
		sizeX = table_x >> num_pochoir;
		sizeY = table_y >> num_pochoir;
		this.num_pochoir = num_pochoir;
//		surface = sizeX * sizeY;
		
		datas = new boolean[sizeX+1][sizeY+1];
		// construit une map de sizeX * sizeY vide
		for(int i=0; i<sizeX; i++)
			for(int j=0; j<sizeY;j++)
				datas[i][j] = true;		
	}
	

	
	/**
	 * Transforme un chemin ou chaque pas est spécifié en un chemin lissé ou il ne reste que très peu de sommets
	 * @param le chemin non lissé (avec tout les pas)
	 * @return le chemin liss (avec typiquement une disaine de sommets grand maximum)
	 */
	public ArrayList<Vec2> lissage(ArrayList<Vec2> cheminFull)
	{
		if (cheminFull.size() < 2)
			return cheminFull;
		// Nettoie le chemin
		ArrayList<Vec2> chemin = new ArrayList<Vec2>();
		int 	lastXDelta = 0,
				lastYDelta = 0,
				xDelta = 0,
				yDelta = 0;
		
		// On doit rentrer les 2 premiers points du parcours
		//chemin.add(cheminFull.get(cheminFull.size()-1)); // ajoute la fin
		chemin.add(cheminFull.get(0));
		chemin.add(cheminFull.get(1));
		
		xDelta = (int)(cheminFull.get(1).x - cheminFull.get(0).x);
		yDelta = (int)(cheminFull.get(1).y - cheminFull.get(0).y);
		for (int i = 2; i < cheminFull.size(); ++i)	
		{
			lastXDelta = xDelta;
			lastYDelta = yDelta;
			xDelta = (int)(cheminFull.get(i).x - cheminFull.get(i-1).x);
			yDelta = (int)(cheminFull.get(i).y - cheminFull.get(i-1).y);
			
			if (xDelta != lastXDelta && yDelta != lastYDelta)	// Si virage, on garde le point, sinon non.
				chemin.add(cheminFull.get(i-1));
		}
		chemin.remove(1); // retire l'intermédiare de calcul
		chemin.add(cheminFull.get(cheminFull.size()-1)); // ajoute la fin
		
		
		// supprimes les points non nécéssaire.
		ArrayList<Vec2> out = new ArrayList<Vec2>();
		
		// saute les 2 derniers points, comme on ne pourra rien simplifier entre.
		for (int i = 0; i < chemin.size(); ++i)	
		{
			// regardes si un point plus loin peut �tre rejoint en ligne droite
			for (int j = chemin.size()-1; j > i; --j)
			{
				if (canCrossLine(chemin.get(i).x, chemin.get(i).y, chemin.get(j).x, chemin.get(j).y))
				{
					//System.out.println("Lissage loops parameters :  i = " + i + ";  j = " + j);
					//drawLine(chemin.get(i).x, chemin.get(i).y, chemin.get(j).x, chemin.get(j).y);
					// on a trouvé le point le plus loin que l'on peut rejoindre en ligne droite
					out.add(chemin.get(i));
					i = j-1;	// on continuras la recherche a partir de ce point.
					break;
				}
			}
		}
		// 	on ajoute le point d'arrivée au chemin final
		out.add(chemin.get(chemin.size()-1));
		
		return out;
	}
	
	

	/**
	 * Surcouche user-friendly d'ajout d'obstacle fixe
	 * @param obs
	 */
	public void appendObstacleFixe(Obstacle obs)
	{
		if(obs instanceof ObstacleRectangulaire)
			appendObstacleFixe((ObstacleRectangulaire)obs);
		else if(obs instanceof ObstacleCirculaire)
			appendObstacleFixe((ObstacleCirculaire)obs);
	}
	
	/**
	 * Ajout long d'un obstacle rectangulaire.
	 * Exécuté seulement lors de la génération du cache.
	 * @param obs
	 */
	private void appendObstacleFixe(ObstacleRectangulaire obs)
	{
		// Asumptions :  	obs.getPosition() returns the top left corner of the rectangle
		//					also, rectangle is Axis Aligned...
		for(int i = obs.getPosition().x - robotRadius - marge; i < obs.getPosition().x + obs.getLongueur_en_x() + robotRadius + marge; i++)
			for(int j = obs.getPosition().y - robotRadius - marge - obs.getLongueur_en_y(); j < obs.getPosition().y + robotRadius + marge; j++)
				if(i >= -table_x/2 && i < table_x/2 && j >= 0 && j < table_y && obs.distance(new Vec2(i,j)) < robotRadius + marge)
				{
					Vec2 posGrid = conversionTable2Grid(new Vec2(i,j));
					datas[posGrid.x][posGrid.y] = false;
				}
	}

	/**
	 * Ajout long d'un obstacle circulaire.
	 * Exécuté seulement lors de la génération du cache.
	 * @param obs
	 */
	private void appendObstacleFixe(ObstacleCirculaire obs)
	{
		int radius = obs.getRadius();
		for(int i = obs.getPosition().x - robotRadius - marge - radius; i < obs.getPosition().x + radius + robotRadius + marge; i++)	
			for(int j = obs.getPosition().y - robotRadius - marge - radius; j < obs.getPosition().y + radius + robotRadius + marge; j++)
				if(i >= -table_x/2 && i < table_x/2 && j >= 0 && j < table_y && obs.getPosition().distance(new Vec2(i,j)) < radius + robotRadius + marge)
				{
					Vec2 posGrid = conversionTable2Grid(new Vec2(i,j));
//					System.out.println("ij:"+ new Vec2(i,j));
//					System.out.println("posGrid:"+ posGrid);
					datas[posGrid.x][posGrid.y] = false;
				}
	}

	
	/**
	 * Ajout optimisé d'obstacle temporaires, de taille fixe.
	 * Utilise les pochoirs.
	 * Usage très courant.
	 * @param obs
	 */
	public void appendObstacleTemporaire(ObstacleCirculaire obs)
	{
		// Asumptions :  	obs.getPosition() returns the center of the circle (pretty obvious, but still...)
		
		Vec2 posPochoir = conversionTable2Grid(obs.getPosition());
		
		Grid2DPochoir pochoir = pochoirs[num_pochoir];

		int radius = pochoir.radius;

		// Recopie le pochoir
		for(int i = posPochoir.x - radius; i < posPochoir.x + radius; i++)
			for(int j = posPochoir.y - radius; j < posPochoir.y + radius; j++)
				if( i >= 0 && i < sizeX && j >=0 && j < sizeY)
					datas[i][j] = pochoir.datas[i-posPochoir.x+radius][j-posPochoir.y + radius] && datas[i][j];
					// Evaluation paresseuse: a priori, puisqu'on applique le pochoir, il y a plus de chances qu'il soit à false que datas
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
	/**
	 * Constructeur privé de Grid2DSpace
	 * Utilisé seulement par makecopy
	 */
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
	
	/**
	 * Implémentation user-friendly de canCross
	 * @param pos
	 * @return
	 */
	public boolean canCross(Vec2 pos)
	{
		// anti segfault
		if(pos.x < 0 || pos.x >= sizeX || pos.y < 0 || pos.y >= sizeY)
			return false;
		
		return datas[pos.x][pos.y];
	}

	
	/**
	 * renvois true si le terrain est franchissable � en ligne droite entre les 2 positions donn�es, faux sinon
	 * @param a un point
	 * @param b un autre point
	 * @return
	 */
	public boolean canCrossLine(Vec2 a, Vec2 b)
	{
		 // Bresenham's line algorithm
		
		  int dx = Math.abs(b.x-a.x), sx = a.x<b.x ? 1 : -1;
		  int dy = Math.abs(b.y-a.y), sy = a.y<b.y ? 1 : -1; 
		  int err = (dx>dy ? dx : -dy)/2;
		  int e2;
		 
		  while(true)
		  {
			    if (!canCross(a.x,a.y))
			    	return false;

				if (a.x==b.x && a.y==b.y) break;
				
				e2 = err;
				if (e2 >-dx) { err -= dy; a.x += sx; }
				if (e2 < dy) { err += dx; a.y += sy; }
		  }
		 return true;
	}

	/**
	 * Implémentation user-friendly de canCrossLine
	 * renvois true si le terrain est franchissable � en ligne droite entre les 2 positions donn�es, faux sinon
	 * @param a un point
	 * @param b un autre point
	 * @return
	 */
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
		other.datas = new boolean[sizeX+1][sizeY+1];
		for (int i = 0; i < datas.length; i++) {
		    System.arraycopy(datas[i], 0, other.datas[i], 0, datas[0].length);
		}
//		other.surface = surface;
		other.sizeX = sizeX;
		other.sizeY = sizeY;
		other.num_pochoir = num_pochoir;
	}
	
	/**
	 * Convertit une longueur depuis les unités de la table dans les unités de la grille
	 * @param nb
	 * @return
	 */
	private int conversionTable2Grid(int nb)
	{
		return nb >> num_pochoir;
	}

	/**
	 * Convertit une longueur depuis les unités de la grille dans les unités de la tabme
	 * @param nb
	 * @return
	 */
	private int conversionGrid2Table(int nb)
	{
		return nb << num_pochoir;
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
	
	/**
	 * Convertit un point depuis les unités de la grille dans les unités de la table
	 * @return
	 */
	public Vec2 conversionGrid2Table(Vec2 pos)
	{
		return new Vec2(conversionGrid2Table(pos.x)-table_x/2,
						conversionGrid2Table(pos.y));
	}
	
	/**
	 * Surchage de la méthode toString de Object
	 * Permet de faire System.out.println(map) ou log.debug(map, this) pour afficher un Grid2DSpace
	 */
	public String toString()
	{
		String s = new String();
		for(int i = sizeY-1; i >= 0; i--)
		{
			for(int j = 0; j < sizeX; j++)
			{
				if(datas[j][i])
					s+=".";
				else
					s+="X";
			}
			s+="\n";
		}
		return s;
	}

	
	
}
