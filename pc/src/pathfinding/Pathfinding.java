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
		map = new Grid2DSpace(new IntPair((int)((float)300.0f/centimetresParCases), (int)((float)220.0f/centimetresParCases)), table, 200);
		solver = new AStar(map, new IntPair(0,0), new IntPair(0,0));
		output = new ArrayList<Vec2>();
		result = new ArrayList<IntPair>();
		
	};
	
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
	 * @param depart, exprimé en milimètres, avec comme origine le point en face du centre des mamouths
	 * @param arrivee système de coords IDEM
	 * @return l'itinéraire, exprimé comme des vecteurs de déplacement, et non des positions absolues, et en millimètres
	 * 			Si l'itinéraire est non trouvable, null est retourné.
	 */
	
	// TODO : deal with precision issue (divide by centimetresParCases)
	public ArrayList<Vec2> chemin(Vec2 depart, Vec2 arrivee)
	{
		

		solver.setDepart(new IntPair((int)((float)(depart.x + 1500) / centimetresParCases /10), (int)((float)(depart.y) / centimetresParCases /10)));
		solver.setArrivee(new IntPair((int)((float)(arrivee.x + 1500) / centimetresParCases /10), (int)((float)(arrivee.y) / centimetresParCases /10)));
/*		// Change de système de coordonnées
		solver.setDepart(new IntPair((int)Math.round(depart.x/10 + 150)/centimetresParCases, (int)Math.round(depart.y/10)/centimetresParCases));
		solver.setArrivee(new IntPair((int)Math.round(arrivee.x/10 + 150)/centimetresParCases, (int)Math.round(arrivee.y/10)/centimetresParCases));

		System.out.println("solver.depart : " + solver.getDepart().x + "   " + solver.getDepart().y);
		System.out.println("solver.arrivee : " + solver.getArrivee().x + "   " + solver.getArrivee().y);
		System.out.println("solver.espace.size : " + solver.getEspace().getSizeX() + "   " + solver.getEspace().getSizeY());
		*/
		// calcule le chemin
		solver.process();
		if (!solver.isValid())	// null si A* dit que pas possib'
			return null;
		result = lissage(solver.getChemin(), map);
		
/*
		System.out.println("=======================================================\n PostLissage dump\n=============================");
		
		

		String out = "";
		Integer ptCount = 0;
		for (int  j = 0; j < map.getSizeX(); ++j)
		{
			for (int  k = map.getSizeY() - 1; k >= 0; --k)
			{
				IntPair pos = new IntPair(j,k);
				if (depart.x ==j && depart.y ==k)
					out += "D ";
				else if (arrivee.x ==j && arrivee.y ==k)
					out += "A ";
				else if (result.contains(pos))
				{
					ptCount ++;
					out += ptCount.toString();
				}
				else if(map.canCross(j, k))
					out += ". ";
				else
					out += "X ";	
			}
			
			out +='\n';
		}
		System.out.println(out);
		System.out.println("=======================================================\nEnd of dump\n=============================");
		
		*/
		
		
		// affiche la liste des positions
		output.clear();
		for (int i = 0; i < result.size(); ++i)
			output.add(new Vec2((float)(result.get(i).x)* 10*centimetresParCases -1500, (float)(result.get(i).y)* 10*centimetresParCases));
		System.out.println("Chemin : " + output);
		

		return output;
		
		// convertit la sortie de l'AStar en suite de Vec2 dans le système de coords d'entrée.
		/*
		output.clear();
		for (int i = 1; i < result.size(); ++i)
			output.add(new Vec2((float)(result.get(i).x - result.get(i-1).x)* 10*centimetresParCases, (float)(result.get(i).y - result.get(i-1).y)* 10*centimetresParCases));
		
		
		
		return output;*/
		
	}

	/**
	 * Renvoie la distance entre départ et arrivée, en utilisant le cache ou non.
	 * @param depart
	 * @param arrivee
	 * @param use_cache :  si le chemin doit être précisément calculé, ou si on peut utiliser un calcul préfait.
	 * @return la longeur du parsours, exprimée en milimèrtes. Le parsours calculé est diponible via getResult. -1 est retourné quand le chemin est pas trouvable
	 */
	public int distance(Vec2 depart, Vec2 arrivee, boolean use_cache)
	{
		if(!use_cache)
		{
			// Change de système de coordonnées
			solver.setDepart(new IntPair((int)Math.round(depart.x/10 + 150)/centimetresParCases, (int)Math.round(depart.y/10)/centimetresParCases));
			solver.setArrivee(new IntPair((int)Math.round(arrivee.x/10 + 150)/centimetresParCases, (int)Math.round(arrivee.y/10)/centimetresParCases));
			
			// calcule le chemin
			solver.process();
			if (!solver.isValid())	// null si A* dit que pas possib'
				return -1;
			result = lissage(solver.getChemin(), map);
			
			// convertit la sortie de l'AStar en suite de Vec2
			int out = 0;
			for (int i = 1; i < result.size(); ++i)
				out +=  Math.sqrt(	(result.get(i).x - result.get(i-1).x) * (result.get(i).x - result.get(i-1).x) +
									(result.get(i).y - result.get(i-1).y) * (result.get(i).y - result.get(i-1).y));
			
			return out*10*centimetresParCases;	// x 10 pour la distance en mm
		}
		else
		{
			// système de cache inside
			return 0;
		}
	}
	
	


	/**
	 * Transforme un chemin ou chaque pas est spécifié en un chemin lissé ou il ne reste que très peu de sommets
	 * ch
			// calcule le chemin
			solver.setDepart(new IntPair((int)Math.round(depart.x),(int)Math.round(depart.y)));
			solver.setArrivee(new IntPair((int)Math.round(arrivee.x),(int)Math.round(arrivee.y)));
			solver.process();
			result = lissage(solver.getChemin(), map);acun de ses sommets est séparé par une ligne droite sans obstacle
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
		ArrayList<IntPair> out = new ArrayList<IntPair>();
		
		// saute les 2 derniers points, comme on ne pourra rien simplifier entre.
		for (int i = 0; i < chemin.size(); ++i)	
		{
			// regardes si un point plus loin peut �tre rejoint en ligne droite
			for (int j = chemin.size()-1; j > i; --j)
			{
				if (map.canCrossLine(chemin.get(i).x, chemin.get(i).y, chemin.get(j).x, chemin.get(j).y))
				{
					//System.out.println("Lissage loops parameters :  i = " + i + ";  j = " + j);
					//map.drawLine(chemin.get(i).x, chemin.get(i).y, chemin.get(j).x, chemin.get(j).y);
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
	

	public ArrayList<IntPair> getResult() 
	{
		return result;
	}
	public IntPair getDepart() 
	{
		return solver.getDepart();
	}
	public IntPair getArrivee() 
	{
		return solver.getArrivee();
	}
	/**
	 * @return the map
	 */
	public Grid2DSpace getMap()
	{
		return map;
	}
}
