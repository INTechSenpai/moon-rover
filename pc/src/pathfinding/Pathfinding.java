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
 * 
 *
 */

public class Pathfinding implements Service, Cloneable
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
	private Grid2DSpace[] solvers_grid;
	private Grid2DSpace solver_grid;

	/* Quatre Grid2DSpace qui sont la base, avec les obstacles fixes. On applique des pochoirs dessus.
	 * Quatre parce qu'il y a la table initiale, la table à laquelle il manque une torche fixe et la table sans torche fixe.
	 * Ces quatre cas permettront de n'ajouter au final que les obstacles mobiles.
	 */
	private static Grid2DSpace[][] map_obstacles_fixes = null;
	
	/* Le cache des distancesDispose de plusieurs solver A*, utilisé pour calculer un HPA*
	 * Sera rechargé en match (au plus 4 fois), car prend trop de mémoire sinon
	 */
	private static CacheHolder distance_cache;

	// précision du AStar
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
		solver = new AStar();
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
		solvers_grid = new Grid2DSpace[nb_precisions];
		for(int i = 0; i < nb_precisions; i++)
		{
			hashTableSaved[i] = -1;
			solvers_grid[i] = new Grid2DSpace(i);
			solvers[i] = new AStar(solvers_grid[i]);
			setPrecision(i);
			update(); 	// initialisation des map
		}
		setPrecision(4);	// Amplement suffisant pour la pluspars des usages

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

			map_obstacles_fixes[degree][code_torches_actuel].copy(solver_grid);

			// Puis les obstacles temporaires
			ArrayList<ObstacleCirculaire> obs = table.getListObstacles();
			for(ObstacleCirculaire o: obs)
				solver_grid.appendObstacleTemporaire(o);
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
		// AStar simple tant que le  HPAStar est pas fonctionnel
		return cheminAStar(depart, arrivee);
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
		Vec2 departGrid = solver_grid.conversionTable2Grid(depart); 
		Vec2 arriveeGrid = solver_grid.conversionTable2Grid(arrivee); 
		ArrayList<Vec2> chemin = solver.process(departGrid, arriveeGrid);

//		log.debug("Chemin avant lissage : " + chemin, this);

		chemin = solver_grid.lissage(chemin);
		ArrayList<Vec2> output = new ArrayList<Vec2>();
		for(Vec2 pos: chemin)
			output.add(solver_grid.conversionGrid2Table(pos));
		
	//	log.debug("Chemin : " + output, this);
		
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
		// On va pas se priver d'une telle optimisation
		if(solver.espace.canCrossLine(depart, arrivee))
			return (int)depart.distance(arrivee);
		
		if(depart.x > 1500 || depart.x < -1500 || depart.y > 2000 || depart.y < 0 || arrivee.x > 1500 || arrivee.x < -1500 || arrivee.y > 2000 || arrivee.y < 0)
			throw new PathfindingException();
		
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
		solver_grid = solvers_grid[degree];
	}
	

	public int getPrecision() 
	{
		return degree;
	}
	
	@Override
	public boolean equals(Object other)
	{
	    if(other == null)
	        return false;
	    else if(!(other instanceof Pathfinding))
	        return false;
	    return table.equals(((Pathfinding)other).table);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Pathfinding clone()
	{
		try {
			return (Pathfinding) super.clone();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	
}
