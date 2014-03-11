/**
 * 
 */
package pathfinding.SearchSpace;

import java.util.ArrayList;
import java.util.Random;

import smartMath.IntPair;
import smartMath.Vec2;
/**
 * @author Marsya
 *	La classe espace de recherche
 *  Pour le robot, ce sera concr�tement la table
 */
public class Grid2DSpace 
{

	private ArrayList<ArrayList<Boolean>> datas;
	private float surface;
	private int sizeX;
	private int sizeY;
	
	public Grid2DSpace(Vec2 size)
	{

		// initialise le terrain
		surface = size.x * size.y;
		sizeX = (int)Math.round(size.x);
		sizeY = (int)Math.round(size.y);
		System.out.println("Creating random Grid2DSpace, size :" + sizeX + "x" + sizeY);
		
		datas = new ArrayList<ArrayList<Boolean>>();
		for (int  i = 0; i < sizeX; ++i)
		{
			datas.add(new ArrayList<Boolean>(sizeY));
			for (int  j = 0; j < sizeY; ++j)
				datas.get(i).add(true);
		}
		

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
					datas.get(j).set(k, false);			// au dessus du seuil, on ne passe pas
				else
					datas.get(j).set(k, true);			// en dessous, ca roule
					
					
				
		
	}
	

	
	// WARING WARING : size have to be consistant with originalDatas
	public Grid2DSpace(Vec2 size, ArrayList<ArrayList<Boolean>> originalDatas)
	{
		surface = size.x * size.y;
		sizeX = (int)Math.round(size.x);
		sizeY = (int)Math.round(size.y);


		datas = new ArrayList<ArrayList<Boolean>>();
		for (int  i = 0; i < sizeX; ++i)
		{
			datas.add(new ArrayList<Boolean>(sizeY));
			for (int  j = 0; j < sizeY; ++j)
				datas.get(i).add(originalDatas.get(i).get(j));
		}
					
	}
	
	public Grid2DSpace makeCopy()
	{
		return new Grid2DSpace(new Vec2(sizeX, sizeY), datas);
	}

	// create an approximation of the current Grid2DSpace
	// 0 degree is the same size
	// 1 is half size
	// 2 quarter, etc...
	public Grid2DSpace makeSmallerCopy(int degree)
	{
		if (degree == 0)
			return makeCopy();
		else
		{
			//Abwabwa
			Grid2DSpace smaller = new Grid2DSpace(new Vec2(sizeX, sizeY), datas);
			return new Grid2DSpace(new Vec2(sizeX, sizeY), datas);
			
		}

	}
	
	// renvois true si le tarrain est franchissable � la position donn�e, faux sinon
	// x est de droite a gauche et y de bas en haut
	public boolean canCross(int x, int y)
	{
		// anti segfault
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY)
			return false;
		
		return datas.get(x).get(y);
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
				if(datas.get(j).get(k) == true)
					out += '.';
				else
					out += 'X';	
			
			out +='\n';
		}
		return out;
	}

}
