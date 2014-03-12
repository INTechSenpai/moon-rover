package pathfinding;

import java.util.ArrayList;

import pathfinding.SearchSpace.Grid2DSpace;
import smartMath.IntPair;
import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Read_Ini;
/*
import table.Table;
import utils.Log;
import utils.Read_Ini;
*/
import container.Service;

/**
 * Service de recherche de chemin
 * @author Abwabwa
 *
 */

public class Pathfinding implements Service
{
	// Dépendances
	private Table table;
	private Read_Ini config;
	private Log log;
	
	public Pathfinding(Table table, Read_Ini config, Log log)
	{
		this.table = table;
		this.config = config;
		this.log = log;
	}
	
	/**
	 * Méthode appelée par le thread de capteur. Met à jour les obstacles de la recherche de chemin en les demandant à table
	 */
	public void update()
	{
		
	}

	/**
	 * Retourne l'itinéraire pour aller d'un point de départ à un point d'arrivée
	 * @param depart
	 * @param arrivee
	 * @return l'itinéraire
	 */
	public ArrayList<Vec2> chemin(Vec2 depart, Vec2 arrivee)
	{
		ArrayList<Vec2> chemin = new ArrayList<Vec2>();
		chemin.add(arrivee);
		return chemin;
	}

	/**
	 * Renvoie la distance entre départ et arrivée. Utilise le cache ou non, selon le mode en cours.
	 * @param depart
	 * @param arrivee
	 * @return
	 */
	public int distance(Vec2 depart, Vec2 arrivee)
	{
		return 0;
	}
	
	


	/**
	 * Transforme un chemin ou chaque pas est spécifié en un chemin lissé ou il ne reste que très peu de sommets
	 * chacun de ses sommets est séparé par une ligne droite sans obstacle
	 * @param le chemin non lissé (avec tout les pas)
	 * @return le chemin liss (avec typiquement une disaine de sommets grand maximum)
	 */
	public ArrayList<IntPair> lissage(ArrayList<IntPair> cheminFull, Grid2DSpace map)
	{
		if (cheminFull.size() < 2)
			return cheminFull;
		// Nettoie le chemin
		ArrayList<IntPair> chemin = new ArrayList<IntPair>();
		int 	lastXDelta = 0,
				lastYDelta = 0,
				xDelta = 0,
				yDelta = 0;
		
		// On doit rentrer les 2 premiers points du parcours
		chemin.add(cheminFull.get(cheminFull.size()-1)); // ajoute la fin
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
		chemin.remove(1);
		
		
		// supprimes les points non nécéssaire.
		ArrayList<IntPair> out = new ArrayList<IntPair>();
		// 	on ajoute le point de départ au chemin final
		out.add(chemin.get(chemin.size()-1));
		
		// saute les 2 derniers points, comme on ne pourra rien simplifier entre.
		for (int i = 0; i < chemin.size(); ++i)	
		{
			// regardes si un point plus loin peut �tre rejoint en ligne droite
			for (int j = chemin.size()-1; j > i; --j)
			{
				if (map.canCrossLine(chemin.get(i).x, chemin.get(i).y, chemin.get(j).x, chemin.get(j).y))
				{
					//map.drawLine(chemin.get(i).x, chemin.get(i).y, chemin.get(j).x, chemin.get(j).y);
					// on a trouvé le point le plus loin que l'on peut rejoindre en ligne droite
					out.add(chemin.get(i));
					i = j-1;	// on continuras la recherche a partir de ce point.
					break;
				}
			}
		}
		
		return out;
	}
	
	
	
	
	
	/**
	 * Modifie le mode de pathfinding: utilisation du cache ou calcul de recherche de chemin
	 * @param use_cache
	 */
	public void setUseCache(boolean use_cache)
	{
		
	}

	/**
	 * Retourne le mode actuel
	 * @return
	 */
	public boolean getUseCache()
	{
		return true;
	}
	
}
