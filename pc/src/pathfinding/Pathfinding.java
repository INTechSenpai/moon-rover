package pathfinding;

import java.util.ArrayList;

import pathfinding.SearchSpace.Grid2DSpace;
import smartMath.Vec2;
import table.ObstacleCirculaire;
import table.Table;
import utils.DataSaver;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import exception.ConfigException;
import exception.PathfindingException;


/**
 * Service de recherche de chemin
 * @author Marsu, pf
 * 
 * 
 * Permet de traiter les problèmes de chemins : longueur d'un parcourt d'un point à un autre de la map,
 * 												 trajet d'un point a un autre de la map
 * 	Le tout indépendamment de l'algorithme (pour l'instant que weithed A*)
 *
 */

public class Pathfinding implements Service
{
	// Dépendances
	private final Table table;	// Final: protection contre le changement de référence.
								// Si la référence change, tout le memorymanager foire.
	private int[] hashTableSaved;	// Permet, à l'update, de ne recalculer map que si la table a effectivement changé.
	private static Read_Ini config;
	private static Log log;
	private int table_x = 3000; // écrasé par la config
	private int code_torches_actuel = -1;

	
	private Grid2DSpace[] map;

	/* Quatre Grid2DSpace qui sont la base, avec les obstacles fixes. On applique des pochoirs dessus.
	 * Quatre parce qu'il y a la table initiale, la table à laquelle il manque une torche fixe et la table sans torche fixe.
	 * Ces quatre cas permettront de n'ajouter au final que les obstacles mobiles.
	 */
	private static Grid2DSpace[] map_obstacles_fixes = new Grid2DSpace[10];
	
	/* Le caches des distances
	 * Sera rechargé en match (au plus 4 fois), car prend trop de mémoire sinon
	 */
	private static CacheHolder distance_cache;

	private int degree;

	private AStar solver;
	private ArrayList<Vec2> result;
	private ArrayList<Vec2> output;
	
	/**
	 * Constructeur appelé rarement.
	 * @param requestedtable
	 * @param requestedConfig
	 * @param requestedLog
	 * @param requestedMillimetresParCases
	 */
	public Pathfinding(Table requestedtable, Read_Ini requestedConfig, Log requestedLog, int degree)
	{
		table = requestedtable;
		config = requestedConfig;
		log = requestedLog;
		maj_config();
		hashTableSaved = new int[10];

		Grid2DSpace.set_static_variables(config, log);
		map = new Grid2DSpace[10];

		int reductionFactor = 1;
		for(int i = 0; i < 10; i++)
		{
			hashTableSaved[i] = -1;
			map[i] = new Grid2DSpace(reductionFactor);
			reductionFactor <<= 1;
			this.degree = i; // modification temporaire de degree afin d'updater toutes les maps
			update(); 	// initialisation des map
		}

		this.degree = degree;

		solver = new AStar(map[degree], new Vec2(0,0), new Vec2(0,0));
		output = new ArrayList<Vec2>();
		result = new ArrayList<Vec2>();
	}
	
	public void maj_config()
	{
		try {
			table_x = Integer.parseInt(config.get("table_x"));
		} catch (NumberFormatException | ConfigException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Méthode appelée par le thread de capteur. Met à jour les obstacles de la recherche de chemin en les demandant à table
	 * On consulte pour cela l'attribut table qui a été modifié de l'extérieur.
	 */
	public void update()
	{		
		/* La table est modifiée entre temps. Il faut donc modifier la map.
		 */		

		synchronized(table) // Mutex sur la table, afin qu'elle ne change pas pendant qu'on met à jour le pathfinding
		{
			// Si le hash actuel est égal au hash du dernier update, on annule la copie car la map n'a pas changé.
			if(table.hashTable() == hashTableSaved[degree])
				return;

			// On recharge les map_fixes quand le code des torches changent. Deux raisons:
			// 1) Ces codes changent rarement
			// 2) Mieux vaut économiser la mémoire
			// TODO: tester en vrai pour voir ce qu'il y a de plus performant (recharge ou tout en mémoire)
			if(table.codeTorches() != code_torches_actuel)
			{
				code_torches_actuel = table.codeTorches();
				try {
					for(int i = 0; i < 10; i++)
						map_obstacles_fixes[i] = (Grid2DSpace)DataSaver.charger("cache/map-"+i+"-"+table.codeTorches()+".cache");
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				distance_cache = (CacheHolder) DataSaver.charger("cache/distance-"+code_torches_actuel+".cache");

			}
			hashTableSaved[degree] = table.hashTable();
			
			// On recopie les obstacles fixes
			map_obstacles_fixes[degree].clone(map[degree]);

			// Puis les obstacles temporaires
			ArrayList<ObstacleCirculaire> obs = table.getListObstacles();
			for(ObstacleCirculaire o: obs)
				map[degree].appendObstacleTemporaire(o);
		}
	}

	/**
	 * Retourne l'itinéraire pour aller d'un point de départ à un point d'arrivée
	 * @param depart, exprimé en milimètres, avec comme origine le point en face du centre des mamouths
	 * @param arrivee système de coords IDEM
	 * @return l'itinéraire, exprimé comme des vecteurs de déplacement, et non des positions absolues, et en millimètres
	 * 			Si l'itinéraire est non trouvable, une exception est est retournée.
	 * @throws PathfindingException 
	 */
	public ArrayList<Vec2> chemin(Vec2 depart, Vec2 arrivee) throws PathfindingException
	{
		solver.setDepart(map[degree].conversionTable2Grid(depart));
		solver.setArrivee(map[degree].conversionTable2Grid(arrivee));

		// calcule le chemin
		solver.process();
		if (!solver.isValid())	// null si A* dit que pas possib'
			throw new PathfindingException();

		result = lissage(solver.getChemin(), map[degree]);
		
		// affiche la liste des positions
		output.clear();
		for (int i = 0; i < result.size()-1; ++i)
			output.add(map[degree].conversionGrid2Table(result.get(i)));
		output.add(arrivee);
		log.debug("Chemin : " + output, this);
		
		return output;
	}

	/**
	 * Renvoie la distance entre départ et arrivée, en utilisant le cache ou non.
	 * @param depart
	 * @param arrivee
	 * @param use_cache :  si le chemin doit être précisément calculé, ou si on peut utiliser un calcul préfait.
	 * @return la longeur du parsours, exprimée en milimèrtes. Le parsours calculé est diponible via getResult. -1 est retourné quand le chemin est pas trouvable
	 * @throws PathfindingException 
	 */
	public int distance(Vec2 depart, Vec2 arrivee, boolean use_cache) throws PathfindingException
	{
		if(!use_cache || distance_cache == null)
		{
			int millimetresParCases = exponentiation(2, degree);
			// Change de système de coordonnées
			solver.setDepart(map[degree].conversionTable2Grid(depart));
			solver.setArrivee(map[degree].conversionTable2Grid(arrivee));
			
			// calcule le chemin
			System.out.println("Boucle infinie?");
			solver.process();
			System.out.println("Pas de boucle infinie.");

			if (!solver.isValid())	// lève une exception si A* dit que pas possible
				throw new PathfindingException();
			result = lissage(solver.getChemin(), map[degree]);
			
			// convertit la sortie de l'AStar en suite de Vec2
			int out = 0;
			for (int i = 1; i < result.size(); ++i)
				out += result.get(i-1).distance(result.get(i));

			return out*millimetresParCases;
		}
		else
		{
			int cacheReduction = distance_cache.reduction;
			return distance_cache.data[(depart.x+table_x/2)/cacheReduction][depart.y/cacheReduction][(arrivee.x+table_x/2)/cacheReduction][arrivee.y/cacheReduction];
		}
	}
	
	


	/**
	 * Transforme un chemin ou chaque pas est spécifié en un chemin lissé ou il ne reste que très peu de sommets
	 * ch
			// calcule le chemin
			solver.setDepart(new Vec2((int)Math.round(depart.x),(int)Math.round(depart.y)));
			solver.setArrivee(new Vec2((int)Math.round(arrivee.x),(int)Math.round(arrivee.y)));
			solver.process();
			result = lissage(solver.getChemin(), map);acun de ses sommets est séparé par une ligne droite sans obstacle
	 * @param le chemin non lissé (avec tout les pas)
	 * @return le chemin liss (avec typiquement une disaine de sommets grand maximum)
	 */
	public ArrayList<Vec2> lissage(ArrayList<Vec2> cheminFull, Grid2DSpace map)
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
	

	public ArrayList<Vec2> getResult() 
	{
		return result;
	}
	public Vec2 getDepart() 
	{
		return solver.getDepart();
	}
	public Vec2 getArrivee() 
	{
		return solver.getArrivee();
	}
	
	public void setDegree(int degree) 
	{
		this.degree = degree;
	}
	
	/**
	 * Le pathfinding en argument deviendra la copie de this (this reste inchangé)
	 * @param cp (cloned_pathfinding)
	 */
	public void clone(Pathfinding cp)
	{
		table.clone(cp.table);  // clone de la table
		cp.update();		    // et update
	}

	public int exponentiation(int a, int b)
	{
		if(b == 0)
			return 1;
		else if(b == 1)
			return a;
		int c = exponentiation(a, b/2);
		if((b&1) == 0)	// si b est pair
			return c*c;
		else
			return c*c*a;
	}
	
}
