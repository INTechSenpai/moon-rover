package pathfinding;

import java.util.ArrayList;

import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Service de recherche de chemin
 * @author pf
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
