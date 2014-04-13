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
 * Dispose de plusieurs solver A*, utilisé pour calculer un HPA*
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
	private int code_torches_actuel = -1;
	private static int nb_precisions;
	
	private AStar[] solvers;
	private AStar solver;

	/* Quatre Grid2DSpace qui sont la base, avec les obstacles fixes. On applique des pochoirs dessus.
	 * Quatre parce qu'il y a la table initiale, la table à laquelle il manque une torche fixe et la table sans torche fixe.
	 * Ces quatre cas permettront de n'ajouter au final que les obstacles mobiles.
	 */
	private static Grid2DSpace[][] map_obstacles_fixes = null;
	
	/* Le caches des distances
	 * Sera rechargé en match (au plus 4 fois), car prend trop de mémoire sinon
	 */
	private static CacheHolder distance_cache;

	private int degree;

	/**
	 * Constructeur appelé rarement.
	 * @param requestedtable
	 * @param requestedConfig
	 * @param requestedLog
	 * @param requestedMillimetresParCases
	 */
	public Pathfinding(Table requestedtable, Read_Ini requestedConfig, Log requestedLog)
	{
		table = requestedtable;
		config = requestedConfig;
		log = requestedLog;
		maj_config();
	}
	
	/**
	 * Régénère les objets, car ils dépendent de la précision utilisée.
	 */
	public void maj_config()
	{
		try {
			nb_precisions = Integer.parseInt(config.get("nb_precisions"));
		} catch (NumberFormatException | ConfigException e) {
			e.printStackTrace();
		}

		hashTableSaved = new int[nb_precisions];

		Grid2DSpace.set_static_variables(config, log);
		// On ne recharge les maps que si elles n'ont jamais été créées, ou avec une précision différente
		// TODO vérifier concordance nb_precisions et taille map_obstacles_fixes
		try {
			if(map_obstacles_fixes == null)
				map_obstacles_fixes = new Grid2DSpace[nb_precisions][4];
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		solvers = new AStar[nb_precisions];
		for(int i = 0; i < nb_precisions; i++)
		{
			hashTableSaved[i] = -1;
			solvers[i] = new AStar(new Grid2DSpace(i));
			setPrecision(i);
			update(); 	// initialisation des map
		}
		setPrecision(0);

	}

	/**
	 * Méthode appelée par le thread de capteur. Met à jour les obstacles de la recherche de chemin en les demandant à table
	 * On consulte pour cela l'attribut table qui a été modifié de l'extérieur.
	 */
	public void update()
	{		
		/* La table est modifiée. Il faut donc modifier la map.
		 */		

		synchronized(table) // Mutex sur la table, afin qu'elle ne change pas pendant qu'on met à jour le pathfinding
		{
			// Si le hash actuel est égal au hash du dernier update, on annule la copie car la map n'a pas changé.
			if(table.hashTable() == hashTableSaved[degree])
				return;

			if(table.codeTorches() != code_torches_actuel)
			{
				code_torches_actuel = table.codeTorches();
				try {
					distance_cache = (CacheHolder) DataSaver.charger("cache/distance-"+code_torches_actuel+".cache");
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			hashTableSaved[degree] = table.hashTable();

			// On recopie les obstacles fixes
			if(map_obstacles_fixes[degree][code_torches_actuel] == null)
				map_obstacles_fixes[degree][code_torches_actuel] = (Grid2DSpace)DataSaver.charger("cache/map-"+degree+"-"+code_torches_actuel+".cache");

			map_obstacles_fixes[degree][code_torches_actuel].clone(solver.espace);

			// Puis les obstacles temporaires
			ArrayList<ObstacleCirculaire> obs = table.getListObstacles();
			for(ObstacleCirculaire o: obs)
				solver.espace.appendObstacleTemporaire(o);
		}
	}

	// TODO: cas de l'anneau
	/**
	 * Calcul d'un itinéraire par HPA*
	 * @param depart
	 * @param arrivee
	 * @return
	 * @throws PathfindingException
	 */
	public ArrayList<Vec2> chemin(Vec2 depart, Vec2 arrivee) throws PathfindingException
	{
		/* Diviser pour régner!
		 * Puisqu'un chemin fait un moyenne 1000mm ou moins, en équilibrant les différentes étages on tombe sur:
		 * degré i: (précision macroscopique) * (précision microscopique)
		 * degré 0: 32*32
		 * degré 1: 16*32
		 * degré 2: 16*16
		 * degré 3: 18*6
		 * degré 4: 8*8
		 * degré 5 et plus: pas de HPA*
		 * Je (PF) pense qu'un HPA* sur plus d'étages serait plus une perte de performances qu'un gain.
		 */
		if(degree >= 5)
			return cheminAStar(depart, arrivee);
		
		// Le degré macro est celui utilisé pour diviser
		int degree_macro = 5-(degree+1)/2;

		Vec2 departGrid = solver.espace.conversionTable2Grid(depart); 
		Vec2 arriveeGrid = solver.espace.conversionTable2Grid(arrivee); 
		
		/* ---- A PARTIR D'ICI, TOUTES LES POSITIONS SONT DANS LES COORDONNÉES DE LA GRILLE */
		
		// Première recherche, précision faible
		ArrayList<Vec2> chemin = solvers[degree_macro].process(departGrid, arriveeGrid);
		// Lissage (très important, car diminue le nombre de recherches de chemin à l'étage inférieur)
		chemin = lissage(chemin, solvers[degree_macro].espace);
		
		System.out.println("HPA* "+chemin);

		// Seconde recherche
		ArrayList<Vec2> output = new ArrayList<Vec2>();

		/*
		 * Rappel: AStar(A, B) donne un itinéraire entre A et B, mais sans inclure A!
		 * Exemple d'itinéraire entre A et B: [D, F, G, B].
		 * C'est pratique pour le HPA*, comme ça on peut concaténer sans répétition.
		 */
		
		Vec2 depart_hpa;
		Vec2 arrivee_hpa;
		for(int i = 0; i < chemin.size(); i++)
		{
			if(i == 0)
				depart_hpa = depart;
			else
				depart_hpa = chemin.get(i-1);
			arrivee_hpa = chemin.get(i);
			
			// TODO vérifier si l'optimisation de canCrossLine est utile
			if(solver.espace.canCrossLine(depart_hpa, arrivee_hpa))
				output.add(arrivee_hpa);
			else
				output.addAll(solver.process(depart_hpa, arrivee_hpa));
		}

		output = lissage(chemin, solver.espace);

		/* Fin des coordonnées de la grille */
		for(Vec2 pos: output)
			pos = solver.espace.conversionGrid2Table(pos);

		return output;
	}
	
	/**
	 * Retourne l'itinéraire pour aller d'un point de départ à un point d'arrivée
	 * @param depart, exprimé en milimètres, avec comme origine le point en face du centre des mammouths
	 * @param arrivee système de coords IDEM
	 * @return l'itinéraire, exprimé comme des vecteurs de déplacement, et non des positions absolues, et en millimètres
	 * 			Si l'itinéraire est non trouvable, une exception est retournée.
	 * @throws PathfindingException 
	 */
	public ArrayList<Vec2> cheminAStar(Vec2 depart, Vec2 arrivee) throws PathfindingException
	{
		// calcule le chemin. Lève une exception en cas d'erreur.
		Vec2 departGrid = solver.espace.conversionTable2Grid(depart); 
		Vec2 arriveeGrid = solver.espace.conversionTable2Grid(arrivee); 
		ArrayList<Vec2> chemin = solver.process(departGrid, arriveeGrid);

		log.debug("Chemin avant lissage : " + chemin, this);

		chemin = lissage(chemin, solver.espace);
		ArrayList<Vec2> output = new ArrayList<Vec2>();
		for(Vec2 pos: chemin)
			output.add(solver.espace.conversionGrid2Table(pos));
		
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
			ArrayList<Vec2> result = chemin(depart, arrivee);
			// Le résultat ne contient pas le point de départ
			int out = (int) depart.distance(result.get(0));
			for (int i = 1; i < result.size(); ++i)
				out += result.get(i-1).distance(result.get(i));

			return out;
			// result est dans les coordonnées de la table, pas besoin de coefficient multiplicateur
		}
		else
		{
			return distance_cache.getDistance(depart, arrivee);
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
		
		xDelta = cheminFull.get(1).x - cheminFull.get(0).x;
		yDelta = cheminFull.get(1).y - cheminFull.get(0).y;
		for (int i = 2; i < cheminFull.size(); ++i)	
		{
			lastXDelta = xDelta;
			lastYDelta = yDelta;
			xDelta = cheminFull.get(i).x - cheminFull.get(i-1).x;
			yDelta = cheminFull.get(i).y - cheminFull.get(i-1).y;
			
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
	
	public void setPrecision(int precision) 
	{
		if(precision >= nb_precisions)
		{
			log.critical("Précision demandée ("+precision+") impossible. Précision utilisée: "+(nb_precisions-1), this);
			degree = nb_precisions-1;
		}
		else
			degree = precision;
		solver = solvers[degree];
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
	
}
