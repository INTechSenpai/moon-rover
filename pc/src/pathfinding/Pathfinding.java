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
	
	public ArrayList<Vec2> chemin(Vec2 depart, Vec2 arrivee)
	{
		return null;
	}
	
	public int distance(Vec2 depart, Vec2 arrivee)
	{
		return 0;
	}
	
	public void setUseCache(boolean use_cache)
	{
		
	}

	public boolean getUseCache()
	{
		return true;
	}
	
}
