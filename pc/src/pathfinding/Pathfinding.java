package pathfinding;

import java.util.ArrayList;

import pathfinding.SearchSpace.Grid2DSpace;
import smartMath.IntPair;
import smartMath.Vec2;
import table.ObstacleCirculaire;
import table.ObstacleRectangulaire;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Service de recherche de chemin
 * @author Marsu
 * 
 * 
 * Permet de traiter les problèmes de chemins : longueur d'un parcourt d'un point à un autre de la map,
 * 												 trajet d'un point a un autre de la map
 * 	Le tout indépendamment de l'algorithme (pour l'insantant que weithed A*
 *
 */

public class Pathfinding implements Service
{
	// Dépendances
	private Table table;
	private Read_Ini config;
	private Log log;
	public Grid2DSpace map;
	private int centimetresParCases;
	AStar solver;
	ArrayList<IntPair> result;
	ArrayList<Vec2> output;
	
	public Pathfinding(Table requestedtable, Read_Ini requestedConfig, Log requestedLog, int requestedCentimetresParCases)
	{
		table = requestedtable;
		config = requestedConfig;
		log = requestedLog;
		centimetresParCases = requestedCentimetresParCases;
		map = new Grid2DSpace(new IntPair(300/centimetresParCases, 200/centimetresParCases), table);
		solver = new AStar(map, new IntPair(0,0), new IntPair(0,0));
	}
	
	/**
	 * Méthode appelée par le thread de capteur. Met à jour les obstacles de la recherche de chemin en les demandant à table
	 * @param newtable : le nouvel état du jeu a prendre en compte
	 */
	public void update(Table newtable)
	{
		// TODO : clear map to initial state
		// also figure out if a check can be founded to skip the whole process if newtable = map
		
		for (int i = 0; i < newtable.getListObstacles().size(); ++i)
		{
			if (newtable.getListObstacles().get(i) instanceof ObstacleRectangulaire)
				map.appendObstacle((ObstacleRectangulaire)newtable.getListObstacles().get(i));
			else
				map.appendObstacle((ObstacleCirculaire)newtable.getListObstacles().get(i));
		}
	}

	/**
	 * Retourne l'itinéraire pour aller d'un point de départ à un point d'arrivée
	 * @param depart
	 * @param arrivee
	 * @return l'ittinéraire, exprimé comme des vecteurs de déplacement, et non des positions absolues
	 */
	public ArrayList<Vec2> chemin(Vec2 depart, Vec2 arrivee)
	{
		// calcule le chemin
		solver.setDepart(new IntPair((int)Math.round(depart.x),(int)Math.round(depart.y)));
		solver.setArrivee(new IntPair((int)Math.round(arrivee.x),(int)Math.round(arrivee.y)));
		solver.process();
		result = lissage(solver.getChemin(), map);
		
		// convertit la sortie de l'AStar en suite de Vec2
		output.clear();
		output.add(new Vec2((float)(result.get(0).x - solver.getDepart().x), (float)(result.get(0).y - solver.getDepart().y)));
		for (int i = 1; i < result.size(); ++i)
			output.add(new Vec2((float)(result.get(i).x - result.get(i-1).x), (float)(result.get(i).y - result.get(i-1).y)));
		output.add(new Vec2((float)(solver.getArrivee().x - result.get(result.size()).x), (float)(solver.getArrivee().y - result.get(result.size()).y)));
		
		return output;
		
	}

	/**
	 * Renvoie la distance entre départ et arrivée, en utilisant le cache ou non.
	 * @param depart
	 * @param arrivee
	 * @param use_cache :  si le chemin doit être précisément calculé, ou si on peut utiliser un calcul préfait.
	 * @return
	 */
	public int distance(Vec2 depart, Vec2 arrivee, boolean use_cache)
	{
		if(!use_cache)
		{
			// calcule le chemin
			solver.setDepart(new IntPair((int)Math.round(depart.x),(int)Math.round(depart.y)));
			solver.setArrivee(new IntPair((int)Math.round(arrivee.x),(int)Math.round(arrivee.y)));
			solver.process();
			result = lissage(solver.getChemin(), map);
			
			// convertit la sortie de l'AStar en suite de Vec2
			int out = 0;
			out +=  Math.sqrt(	(result.get(0).x - solver.getDepart().x) * (result.get(0).x - solver.getDepart().x) +
								(result.get(0).y - solver.getDepart().y) * (result.get(0).y - solver.getDepart().y));
			for (int i = 1; i < result.size(); ++i)
				out +=  Math.sqrt(	(result.get(i).x - result.get(i-1).x) * (result.get(i).x - result.get(i-1).x) +
									(result.get(i).y - result.get(i-1).y) * (result.get(i).y - result.get(i-1).y));

			out +=  Math.sqrt(	(solver.getArrivee().x - result.get(result.size()).x) * (solver.getArrivee().x - result.get(result.size()).x) + 
								(solver.getArrivee().y - result.get(result.size()).y) * (solver.getArrivee().y - result.get(result.size()).y));
			
			return out;
		}
		else
		{
			// système de cache inside
			return 0;
		}
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
		
}
